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
package com.yukthitech.papilio.data;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.mongodb.client.MongoDatabase;
import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;
import com.yukthitech.papilio.common.PapilioUtils;
import com.yukthitech.utils.exceptions.InvalidStateException;

/**
 * Change to insert document.
 * @author akiran
 */
public class InsertChange implements IChange, Validateable
{
	/**
	 * Table name to which insertion should be done.
	 */
	private String tableName;
	
	/**
	 * List of column values.
	 */
	private List<ColumnValue> columnValues = new ArrayList<>();
	
	public InsertChange()
	{}
	
	/**
	 * Instantiates a new insert change.
	 *
	 * @param tableName the table name
	 * @param column the column
	 * @param value the value
	 */
	public InsertChange(String tableName, String column, Object value)
	{
		this.tableName = tableName;
		this.columnValues.add(new ColumnValue(column, value));
	}

	/**
	 * Gets the table name to which insertion should be done.
	 *
	 * @return the table name to which insertion should be done
	 */
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * Sets the table name to which insertion should be done.
	 *
	 * @param tableName the new table name to which insertion should be done
	 */
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	/**
	 * Gets the list of column values.
	 *
	 * @return the list of column values
	 */
	public List<ColumnValue> getColumnValues()
	{
		return columnValues;
	}

	/**
	 * Sets the list of column values.
	 *
	 * @param columnValues the new list of column values
	 */
	public void setColumnValues(List<ColumnValue> columnValues)
	{
		this.columnValues = columnValues;
	}
	
	/**
	 * Adds the column value.
	 *
	 * @param columnValue the column value
	 */
	public InsertChange addColumnValue(ColumnValue columnValue)
	{
		this.columnValues.add(columnValue);
		return this;
	}
	
	/**
	 * Loads specified json file as a map and every entry as column-value
	 * on to this change.
	 * @param jsonFile file to load.
	 * @return current instance
	 */
	@SuppressWarnings("unchecked")
	public InsertChange addColumnValueJson(String jsonFile)
	{
		File fileObj = new File(jsonFile);
		Object value = null;
		
		try
		{
			String fileContent = FileUtils.readFileToString(fileObj, Charset.forName("utf8"));
			value = PapilioUtils.parseJson(fileContent);
		}catch(Exception ex)
		{
			throw new InvalidStateException("Failed to load json content from file: {}", jsonFile, ex);
		}

		if(!(value instanceof Map))
		{
			throw new InvalidStateException("Json file {} resulted in non-map value for column-value-json", jsonFile);
		}
		
		Map<String, Object> map = (Map<String, Object>) value;
		
		for(Map.Entry<String, Object> mapEntry : map.entrySet())
		{
			addColumnValue(new ColumnValue(mapEntry.getKey(), mapEntry.getValue()));
		}
		
		return this;
	}
	
	/**
	 * Added for backward compatibility of checksum calculation.
	 * @return
	 */
	public Map<String, Object> getColumnMap()
	{
		return getColumnMap(null);
	}
	
	public Map<String, Object> getColumnMap(MongoDatabase database)
	{
		Map<String, Object> map = new HashMap<>();
		
		for(ColumnValue colVal : this.columnValues)
		{
			map.put(colVal.getName(), colVal.getValue(database));
		}
		
		return map;
	}
	
	@Override
	public void validate() throws ValidateException
	{
		if(StringUtils.isBlank(tableName))
		{
			throw new ValidateException("Table name can not ne empty.");
		}

		if(columnValues.isEmpty())
		{
			throw new ValidateException("Column values can not be empty.");
		}
}
}
