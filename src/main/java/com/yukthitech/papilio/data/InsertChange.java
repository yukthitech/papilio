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
