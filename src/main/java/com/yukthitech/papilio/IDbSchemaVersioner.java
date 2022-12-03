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

import java.util.Map;

import com.yukthitech.papilio.common.PapilioArguments;
import com.yukthitech.papilio.data.CreateIndexChange;
import com.yukthitech.papilio.data.CreateTableChange;
import com.yukthitech.papilio.data.DeleteChange;
import com.yukthitech.papilio.data.FindAndUpdateChange;
import com.yukthitech.papilio.data.InsertChange;
import com.yukthitech.papilio.data.QueryChange;
import com.yukthitech.papilio.data.ScriptChange;
import com.yukthitech.papilio.data.UpdateChange;

/**
 * Base abstraction interface for db schema versioning.
 * @author akiran
 */
public interface IDbSchemaVersioner
{
	/**
	 * This method should take care of following aspects:
	 *   Create required connection
	 *   create db version related tables
	 * @param args arguments where db info is specified.
	 */
	public void init(PapilioArguments args);
	
	/**
	 * Fetches already executed changeset details from db. changest id as key and checksum as value.
	 * @return
	 */
	public Map<String, String> fetchCurrentChangeSet(String dbLogTableName, String idCol, String checkSumCol);
	
	/**
	 * Checks wether specified table is present in target db.
	 * @param tableName
	 * @return
	 */
	public boolean isTablePresent(String tableName);
	
	/**
	 * Creates a table or collection with specified details.
	 * @param change
	 */
	public void createTable(CreateTableChange change);
	
	/**
	 * Creates index with specified details.
	 * @param change
	 */
	public void createIndex(CreateIndexChange change);
	
	/**
	 * Inserts specified record or collection with specified details.
	 * @param change
	 * @return true if insertion was successful.
	 */
	public void insert(InsertChange change);
	
	/**
	 * Updates record / collection with specified details.
	 * @param change
	 */
	public void update(UpdateChange change);
	
	/**
	 * Deletes record / collection based on specified details.
	 * @param change
	 */
	public void delete(DeleteChange change);
	
	/**
	 * Executes specified query.
	 * @param change
	 */
	public void executQuery(QueryChange change);
	
	/**
	 * Executes the specified script.
	 * @param change
	 */
	public void executScript(ScriptChange change);
	
	/**
	 * Executes find and update operation.
	 * @param change
	 */
	public void findAndUpdate(FindAndUpdateChange change);
	
	/**
	 * Should close all the open resources.
	 */
	public void close();
}
