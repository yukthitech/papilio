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
