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
import com.yukthitech.papilio.data.IChange;
import com.yukthitech.papilio.data.InsertChange;
import com.yukthitech.papilio.data.QueryChange;
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
		addExecutor(DeleteChange.class, dbSchemaVersioner::delete);
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
		
		Map<String, String> curChangesetMap = dbSchemaVersioner.fetchCurrentChangeSet(DBLOG_COLLECTION, FLD_CHANGE_SET_ID, FLD_CHECKSUM);
		List<ChangeSet> changeSetLst = databaseChangeLog.getChangeSets(); 

		if(!lock())
		{
			changeTracker.erroredChangeset(null, ChangeTracker.LOCK_FAILED);
			return false;
		}
		
		try
		{
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
			dbSchemaVersioner.createTable(new CreateTableChange(DBLOG_COLLECTION));
			dbSchemaVersioner.createIndex(new CreateIndexChange(DBLOG_COLLECTION + "_CSET_ID", DBLOG_COLLECTION, true, FLD_CHANGE_SET_ID));
		}
		
		if(!isDbLockPresent)
		{
			logger.debug("As db-log-lock table not found, createing new table: {}", DBLOG_LOCK_COLLECTION);
			
			dbSchemaVersioner.createTable(new CreateTableChange(DBLOG_LOCK_COLLECTION));
			dbSchemaVersioner.createIndex(new CreateIndexChange(DBLOG_LOCK_COLLECTION + "_NAME", DBLOG_LOCK_COLLECTION, true, "NAME"));
		}
	}
	
	private boolean lock()
	{
		InsertChange insert = new InsertChange(DBLOG_LOCK_COLLECTION, "NAME", "LOCK");
		
		try
		{
			dbSchemaVersioner.insert(insert);
			logger.info("Obtained db lock successfully..");
		}catch(Exception ex)
		{
			logger.error("Failed to obtained lock on the db. Please clean the entries from {} manually in case abrupt close last time.", DBLOG_LOCK_COLLECTION, ex);
			return false;
		}
		
		return true;
	}
	
	private void unlock()
	{
		dbSchemaVersioner.delete(new DeleteChange(DBLOG_LOCK_COLLECTION, "NAME", "LOCK"));
		logger.info("Released the db lock successfully.");
	}
	
	private boolean executeChangeSet(ChangeSet changeSet, Map<String, String> curChangesetMap)
	{
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
