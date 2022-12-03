/*
 * The MIT License (MIT)
 * Copyright (c) 2022 "Yukthi Techsoft Pvt. Ltd." (http://yukthitech.com)

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
