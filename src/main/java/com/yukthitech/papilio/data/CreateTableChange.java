package com.yukthitech.papilio.data;

import org.apache.commons.lang3.StringUtils;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;

/**
 * Change which would create a table / collection.
 * @author akiran
 */
public class CreateTableChange extends AbstractOptionBasedChange implements IChange, Validateable
{
	/**
	 * Table or collection name.
	 */
	private String tableName;
	
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
