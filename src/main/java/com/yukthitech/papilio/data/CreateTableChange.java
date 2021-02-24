package com.yukthitech.papilio.data;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
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
	 * If set to true, and if an error occurs indicating collection already exist, it will be ignored.
	 */
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	private Boolean ignoreIfExists;
	
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
	 * Checks if is if set to true, and if an error occurs indicating collection already exist, it will be ignored.
	 *
	 * @return the if set to true, and if an error occurs indicating collection already exist, it will be ignored
	 */
	public Boolean getIgnoreIfExists()
	{
		return ignoreIfExists;
	}

	/**
	 * Sets the if set to true, and if an error occurs indicating collection already exist, it will be ignored.
	 *
	 * @param ignoreIfExists the new if set to true, and if an error occurs indicating collection already exist, it will be ignored
	 */
	public void setIgnoreIfExists(Boolean ignoreIfExists)
	{
		this.ignoreIfExists = ignoreIfExists;
	}

	/**
	 * Validate.
	 *
	 * @throws ValidateException the validate exception
	 */
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
