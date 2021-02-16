package com.yukthitech.papilio.data;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;
import com.yukthitech.papilio.common.JsonUtils;
import com.yukthitech.utils.exceptions.InvalidStateException;

/**
 * Change to insert document.
 * @author akiran
 */
public class UpdateChange extends AbstractOptionBasedChange implements IChange, Validateable
{
	/**
	 * Table name to which insertion should be done.
	 */
	private String tableName;
	
	/**
	 * List of column values.
	 */
	private List<ColumnValue> columnValues = new ArrayList<>();
	
	/**
	 * Conditions based on which update should happen.
	 */
	private List<ColumnValue> conditions = new ArrayList<>();

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
	public void addColumnValue(ColumnValue columnValue)
	{
		if(columnValue == null)
		{
			throw new NullPointerException("Column-value can not be null");
		}
		
		if(this.columnValues == null)
		{
			this.columnValues = new ArrayList<>();
		}
		
		this.columnValues.add(columnValue);
	}

	
	/**
	 * Loads specified json file as a map and every entry as column-value
	 * on to this change.
	 * @param jsonFile file to load.
	 * @return current instance
	 */
	@SuppressWarnings("unchecked")
	public void addColumnValueJson(String jsonFile)
	{
		File fileObj = new File(jsonFile);
		Object value = null;
		
		try
		{
			String fileContent = FileUtils.readFileToString(fileObj, Charset.forName("utf8"));
			value = JsonUtils.parseJson(fileContent);
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
	}

	/**
	 * Gets the conditions based on which update should happen.
	 *
	 * @return the conditions based on which update should happen
	 */
	public List<ColumnValue> getConditions()
	{
		return conditions;
	}

	/**
	 * Sets the conditions based on which update should happen.
	 *
	 * @param conditions the new conditions based on which update should happen
	 */
	public void setConditions(List<ColumnValue> conditions)
	{
		this.conditions = conditions;
	}
	
	/**
	 * Adds the condition.
	 *
	 * @param condition the condition
	 */
	public void addCondition(ColumnValue condition)
	{
		if(condition == null)
		{
			throw new NullPointerException("Condition can not be null");
		}
		
		if(this.conditions == null)
		{
			this.conditions = new ArrayList<>();
		}
		
		this.conditions.add(condition);
	}
	
	@Override
	public void validate() throws ValidateException
	{
		if(StringUtils.isBlank(tableName))
		{
			throw new ValidateException("Table name can not be empty");
		}

		if(this.columnValues.isEmpty())
		{
			throw new ValidateException("No column-values specified for update.");
		}
	}
}
