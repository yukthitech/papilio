package com.yukthitech.papilio.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;

/**
 * Change to delete document.
 * @author akiran
 */
public class DeleteChange implements IChange, Validateable
{
	/**
	 * Table name from which deletion should be done.
	 */
	private String tableName;
	
	/**
	 * Conditions based on which delete should happen.
	 */
	private List<ColumnValue> conditions = new ArrayList<>();
	
	/**
	 * Instantiates a new delete change.
	 */
	public DeleteChange()
	{}
	
	/**
	 * Instantiates a new delete change.
	 *
	 * @param tableName the table name
	 * @param column the column
	 * @param value the value
	 */
	public DeleteChange(String tableName, String column, Object value)
	{
		this.tableName = tableName;
		this.conditions.add(new ColumnValue(column, value));
	}

	/**
	 * Gets the table name from which deletion should be done.
	 *
	 * @return the table name from which deletion should be done
	 */
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * Sets the table name from which deletion should be done.
	 *
	 * @param tableName the new table name from which deletion should be done
	 */
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	/**
	 * Gets the conditions based on which delete should happen.
	 *
	 * @return the conditions based on which delete should happen
	 */
	public List<ColumnValue> getConditions()
	{
		return conditions;
	}

	/**
	 * Sets the conditions based on which delete should happen.
	 *
	 * @param conditions the new conditions based on which delete should happen
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

	/* (non-Javadoc)
	 * @see com.yukthitech.ccg.xml.util.Validateable#validate()
	 */
	@Override
	public void validate() throws ValidateException
	{
		if(StringUtils.isBlank(tableName))
		{
			throw new ValidateException("Table name can not be empty");
		}
	}
}
