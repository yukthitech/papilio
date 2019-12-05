package com.yukthitech.papilio.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;

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
	
	public Map<String, Object> getColumnMap()
	{
		Map<String, Object> map = new HashMap<>();
		
		for(ColumnValue colVal : this.columnValues)
		{
			map.put(colVal.getName(), colVal.getValue());
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
