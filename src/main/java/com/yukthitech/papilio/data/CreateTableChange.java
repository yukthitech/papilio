package com.yukthitech.papilio.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;

/**
 * Change which would create a table / collection.
 * @author akiran
 */
public class CreateTableChange implements IChange, Validateable
{
	/**
	 * Table or collection name.
	 */
	private String tableName;
	
	/**
	 * Helps in specifying custom options which creating table or collection.
	 */
	private Map<String, Object> options;
	
	/**
	 * Instantiates a new creates the table change.
	 */
	public CreateTableChange()
	{}
	
	/**
	 * Instantiates a new creates the table change.
	 *
	 * @param tableName the table name
	 */
	public CreateTableChange(String tableName)
	{
		this.tableName = tableName;
	}

	/**
	 * Sets the table or collection name.
	 *
	 * @param tableName the new table or collection name
	 */
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}
	
	/**
	 * Gets the table or collection name.
	 *
	 * @return the table or collection name
	 */
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * Gets the helps in specifying custom options which creating table or collection.
	 *
	 * @return the helps in specifying custom options which creating table or collection
	 */
	public Map<String, Object> getOptions()
	{
		return options;
	}

	/**
	 * Sets the helps in specifying custom options which creating table or collection.
	 *
	 * @param options the new helps in specifying custom options which creating table or collection
	 */
	public void setOptions(Map<String, Object> options)
	{
		this.options = options;
	}
	
	/**
	 * Adds specified option to this change.
	 * @param name name of option
	 * @param value value of option.
	 */
	private void addOption(String name, Object value)
	{
		if(this.options == null)
		{
			this.options = new HashMap<>();
		}
		
		this.options.put(name, value);
	}
	
	/**
	 * Adds specified String option to this change.
	 * @param name name of option
	 * @param value value of option.
	 */
	public void addStringOption(String name, String value)
	{
		this.addOption(name, value);
	}
	
	/**
	 * Adds specified int option to this change.
	 * @param name name of option
	 * @param value value of option.
	 */
	public void addIntOption(String name, int value)
	{
		this.addOption(name, value);
	}
	
	/**
	 * Adds the long option.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void addLongOption(String name, long value)
	{
		this.addOption(name, value);
	}

	/**
	 * Adds specified boolean option to this change.
	 * @param name name of option
	 * @param value value of option.
	 */
	public void addBooleanOption(String name, boolean value)
	{
		this.addOption(name, value);
	}
	
	/* (non-Javadoc)
	 * @see com.yukthitech.ccg.xml.util.Validateable#validate()
	 */
	@Override
	public void validate() throws ValidateException
	{
		if(StringUtils.isBlank(tableName))
		{
			throw new ValidateException("Table name can not be empty.");
		}
	}
}
