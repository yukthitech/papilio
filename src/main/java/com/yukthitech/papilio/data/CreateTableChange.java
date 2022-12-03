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
