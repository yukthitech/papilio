/**
 * Copyright (c) 2022 "Yukthi Techsoft Pvt. Ltd." (http://yukthitech.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yukthitech.papilio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yukthitech.papilio.common.Md5Evaluator;
import com.yukthitech.papilio.common.PapilioArguments;
import com.yukthitech.papilio.data.ChangeSet;
import com.yukthitech.papilio.data.ColumnValue;
import com.yukthitech.papilio.data.CreateIndexChange;
import com.yukthitech.papilio.data.CreateTableChange;
import com.yukthitech.papilio.data.DatabaseChangeLog;
import com.yukthitech.papilio.data.DeleteChange;
import com.yukthitech.papilio.data.FindAndUpdateChange;
import com.yukthitech.papilio.data.IChange;
import com.yukthitech.papilio.data.InsertChange;
import com.yukthitech.papilio.data.QueryChange;
import com.yukthitech.papilio.data.ScriptChange;
import com.yukthitech.papilio.data.UpdateChange;

public class DbChangeLogExecutor
{
	private static Logger logger = LogManager.getLogger(DbChangeLogExecutor.class);
	
	/**
	 * Name of collection where executed changelogs are maintained.
	 */
	private static final String DBLOG_COLLECTION = "DATABASE_CHANGE_LOG";
	
	/**
	 * Name of collection which helps in locking before executing changeset.
	 */
	private static final String DBLOG_LOCK_COLLECTION = "DATABASE_CHANGE_LOG_LOCK";
	
	/**
	 * Change set id column of DBLOG_COLLECTION.
	 */
	private static final String FLD_CHANGE_SET_ID = "CHANGE_SET_ID";
	
	private static final String FLD_CHECKSUM = "CHECKSUM";
	
	/**
	 * System property to override max time (ms) to wait for lock. Default: 5 minutes.
	 */
	private static final String PROP_LOCK_WAIT_TIMEOUT = "papilio.lockWaitTimeout";
	
	/**
	 * System property to override retry interval (ms) while waiting for lock. Default: 1 second.
	 */
	private static final String PROP_LOCK_RETRY_INTERVAL = "papilio.lockRetryInterval";
	
	/**
	 * Default max wait for lock acquisition.
	 */
	private static final long DEFAULT_LOCK_WAIT_TIMEOUT_MS = 5 * 60 * 1000L;
	
	/**
	 * Default interval between lock acquisition retries.
	 */
	private static final long DEFAULT_LOCK_RETRY_INTERVAL_MS = 1000L;

	private DatabaseChangeLog databaseChangeLog;
	
	private IDbSchemaVersioner dbSchemaVersioner;
	
	private PapilioArguments args;
	
	private Map<Class<?>, Consumer<Object>> changeTypeToExecutors = new HashMap<>();
	
	private ChangeTracker changeTracker;

	public DbChangeLogExecutor(DatabaseChangeLog databaseChangeLog, IDbSchemaVersioner dbSchemaVersioner, PapilioArguments args, ChangeTracker changeTracker)
	{
		this.databaseChangeLog = databaseChangeLog;
		this.dbSchemaVersioner = dbSchemaVersioner;
		this.args = args;
		this.changeTracker = changeTracker;
		
		addExecutor(CreateIndexChange.class, dbSchemaVersioner::createIndex);
		addExecutor(CreateTableChange.class, dbSchemaVersioner::createTable);
		addExecutor(InsertChange.class, dbSchemaVersioner::insert);
		addExecutor(UpdateChange.class, dbSchemaVersioner::update);
		addExecutor(QueryChange.class, dbSchemaVersioner::executQuery);
		addExecutor(ScriptChange.class, dbSchemaVersioner::executScript);
		addExecutor(DeleteChange.class, dbSchemaVersioner::delete);
		addExecutor(FindAndUpdateChange.class, dbSchemaVersioner::findAndUpdate);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends IChange> void addExecutor(Class<T> changeType, Consumer<T> executor)
	{
		changeTypeToExecutors.put(changeType, (Consumer) executor);
	}
	
	public boolean execute()
	{
		logger.debug("Initalizing db versioner..");
		init();
		
		List<ChangeSet> changeSetLst = databaseChangeLog.getChangeSets(); 

		if(!lock())
		{
			changeTracker.erroredChangeset(null, ChangeTracker.LOCK_FAILED);
			dbSchemaVersioner.close();
			return false;
		}
		
		try
		{
			// Fetch after lock so concurrent executions see changes applied by the previous holder
			Map<String, String> curChangesetMap = dbSchemaVersioner.fetchCurrentChangeSet(DBLOG_COLLECTION, FLD_CHANGE_SET_ID, FLD_CHECKSUM);
			
			for(ChangeSet changeSet : changeSetLst)
			{
				if(!executeChangeSet(changeSet, curChangesetMap))
				{
					return false;
				}
			}
		}finally
		{
			unlock();
			dbSchemaVersioner.close();
		}
		
		return true;
	}
	
	private void init()
	{
		dbSchemaVersioner.init(args);
		
		boolean isDbLogPresent = dbSchemaVersioner.isTablePresent(DBLOG_COLLECTION);
		boolean isDbLockPresent = dbSchemaVersioner.isTablePresent(DBLOG_LOCK_COLLECTION);
		
		if(!isDbLogPresent)
		{
			logger.debug("As db-log table not found, createing new table: {}", DBLOG_COLLECTION);
			CreateTableChange createLogTable = new CreateTableChange(DBLOG_COLLECTION);
			createLogTable.setIgnoreIfExists(true);
			dbSchemaVersioner.createTable(createLogTable);
			dbSchemaVersioner.createIndex(new CreateIndexChange(DBLOG_COLLECTION + "_CSET_ID", DBLOG_COLLECTION, true, FLD_CHANGE_SET_ID));
		}
		
		if(!isDbLockPresent)
		{
			logger.debug("As db-log-lock table not found, createing new table: {}", DBLOG_LOCK_COLLECTION);
			
			CreateTableChange createLockTable = new CreateTableChange(DBLOG_LOCK_COLLECTION);
			createLockTable.setIgnoreIfExists(true);
			dbSchemaVersioner.createTable(createLockTable);
			dbSchemaVersioner.createIndex(new CreateIndexChange(DBLOG_LOCK_COLLECTION + "_NAME", DBLOG_LOCK_COLLECTION, true, "NAME"));
		}
	}
	
	private long getLongProperty(String name, long defaultValue)
	{
		String value = System.getProperty(name);
		
		if(value == null || value.trim().isEmpty())
		{
			return defaultValue;
		}
		
		try
		{
			return Long.parseLong(value.trim());
		}catch(NumberFormatException ex)
		{
			logger.warn("Invalid value for system property '{}': {}. Using default: {}", name, value, defaultValue);
			return defaultValue;
		}
	}
	
	private boolean lock()
	{
		InsertChange insert = new InsertChange(DBLOG_LOCK_COLLECTION, "NAME", "LOCK");
		long waitTimeoutMs = getLongProperty(PROP_LOCK_WAIT_TIMEOUT, DEFAULT_LOCK_WAIT_TIMEOUT_MS);
		long retryIntervalMs = getLongProperty(PROP_LOCK_RETRY_INTERVAL, DEFAULT_LOCK_RETRY_INTERVAL_MS);
		long startTime = System.currentTimeMillis();
		boolean waitingLogged = false;
		
		while(true)
		{
			try
			{
				dbSchemaVersioner.insert(insert);
				logger.info("Obtained db lock successfully..");
				return true;
			}catch(Exception ex)
			{
				long elapsed = System.currentTimeMillis() - startTime;
				
				if(elapsed >= waitTimeoutMs)
				{
					logger.error("Failed to obtain lock on the db after waiting {} ms. Please clean the entries from {} manually in case abrupt close last time.", 
							elapsed, DBLOG_LOCK_COLLECTION, ex);
					return false;
				}
				
				if(!waitingLogged)
				{
					logger.info("Db lock is currently held by another execution. Waiting for it to be released (timeout: {} ms, retry every: {} ms)..", 
							waitTimeoutMs, retryIntervalMs);
					waitingLogged = true;
				}
				else
				{
					logger.debug("Still waiting for db lock to be released. Elapsed: {} ms", elapsed);
				}
				
				try
				{
					Thread.sleep(retryIntervalMs);
				}catch(InterruptedException iex)
				{
					logger.error("Interrupted while waiting for db lock.", iex);
					return false;
				}
			}
		}
	}
	
	private void unlock()
	{
		dbSchemaVersioner.delete(new DeleteChange(DBLOG_LOCK_COLLECTION, "NAME", "LOCK"));
		logger.info("Released the db lock successfully.");
	}
	
	private boolean executeChangeSet(ChangeSet changeSet, Map<String, String> curChangesetMap)
	{
		boolean updateChecksum = "true".equalsIgnoreCase(System.getProperty("papilio.updateChecksum"));
		
		String newChecksum = Md5Evaluator.evaluateChecksum(changeSet);
		changeSet.setChecksum(newChecksum);
		
		String oldChecksum = curChangesetMap.get(changeSet.getId());
		
		if(oldChecksum != null)
		{
			if(!oldChecksum.equals(newChecksum))
			{
				String errMssg = String.format("Changeset '%s' is modified from last execution."
						+ "\n\tOld checksum: %s"
						+ "\n\tNew Checksum: %s", changeSet.getId(), oldChecksum, newChecksum);
				
				if(updateChecksum)
				{
					logger.debug(errMssg);
					logger.debug("Updating checksum for id: {}", changeSet.getId());
					
					UpdateChange updateChange = new UpdateChange();
					updateChange.setTableName(DBLOG_COLLECTION);
					updateChange.addCondition(new ColumnValue(FLD_CHANGE_SET_ID, changeSet.getId()));
					updateChange.addColumnValue(new ColumnValue(FLD_CHECKSUM, changeSet.getChecksum()));
		
					dbSchemaVersioner.update(updateChange);
					return true;
				}

				logger.error(errMssg);
				changeTracker.erroredChangeset(changeSet.getId(), errMssg);
				return false;
			}
			
			logger.trace("Skipping changeset as it was already executed: {}", changeSet.getId());
			changeTracker.skippingChangeset(changeSet.getId());
			return true;
		}
		
		logger.info("*****  Executing changeset: {}  *****", changeSet.getId());
		List<IChange> changes = changeSet.getChanges();
		
		for(IChange change : changes)
		{
			Consumer<Object> executor = changeTypeToExecutors.get(change.getClass());
			
			try
			{
				executor.accept(change);
			}catch(RuntimeException ex)
			{
				logger.error("An error occurred while executing changeset '{}'. Error: {}", changeSet.getId(), "" + ex);
				throw ex;
			}
		}
		
		logger.info("#####  End of changeset: {}  #####", changeSet.getId());
		
		InsertChange insertChange = new InsertChange();
		insertChange.setTableName(DBLOG_COLLECTION);
		insertChange.addColumnValue(new ColumnValue(FLD_CHANGE_SET_ID, changeSet.getId()))
			.addColumnValue(new ColumnValue("AUTHOR", changeSet.getAuthor()))
			.addColumnValue(new ColumnValue(FLD_CHECKSUM, changeSet.getChecksum()))
			.addColumnValue(new ColumnValue("FILE_NAME", changeSet.getFileName()));
		
		dbSchemaVersioner.insert(insertChange);
		
		changeTracker.executedChangeset(changeSet.getId());
		
		return true;
	}
}
