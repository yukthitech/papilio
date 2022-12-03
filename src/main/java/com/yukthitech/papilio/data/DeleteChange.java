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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;

/**
 * Change to delete document.
 * @author akiran
 */
public class DeleteChange extends AbstractOptionBasedChange implements IChange, Validateable
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
