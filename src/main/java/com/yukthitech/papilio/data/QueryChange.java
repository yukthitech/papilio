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
