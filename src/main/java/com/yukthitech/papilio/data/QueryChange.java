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

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Change which would execute specified query directly.
 * @author akiran
 */
public class QueryChange implements IChange
{
	/**
	 * Query to execute.
	 */
	private String query;
	
	/**
	 * Flag indicating if the query should be processed as template or not.
	 */
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	private Boolean template;

	/**
	 * Flag indicating if the query should be processed as JEL template or not.
	 */
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	private Boolean jelTemplate;

	/**
	 * Instantiates a new query change.
	 *
	 * @param query the query
	 */
	public QueryChange(String query)
	{
		this.query = query;
	}
	
	/**
	 * Sets the query to execute.
	 *
	 * @param query the new query to execute
	 */
	public void setQuery(String query)
	{
		this.query = query;
	}
	
	/**
	 * Gets the query to execute.
	 *
	 * @return the query to execute
	 */
	public String getQuery()
	{
		return query;
	}

	/**
	 * Gets the flag indicating if the query should be processed as template or
	 * not.
	 *
	 * @return the flag indicating if the query should be processed as template
	 *         or not
	 */
	public Boolean getTemplate()
	{
		return template;
	}

	/**
	 * Sets the flag indicating if the query should be processed as template or
	 * not.
	 *
	 * @param template
	 *            the new flag indicating if the query should be processed as
	 *            template or not
	 */
	public void setTemplate(Boolean template)
	{
		this.template = template;
	}

	/**
	 * Gets the flag indicating if the query should be processed as JEL template
	 * or not.
	 *
	 * @return the flag indicating if the query should be processed as JEL
	 *         template or not
	 */
	public Boolean getJelTemplate()
	{
		return jelTemplate;
	}

	/**
	 * Sets the flag indicating if the query should be processed as JEL template
	 * or not.
	 *
	 * @param jelTemplate
	 *            the new flag indicating if the query should be processed as
	 *            JEL template or not
	 */
	public void setJelTemplate(Boolean jelTemplate)
	{
		this.jelTemplate = jelTemplate;
	}
}
