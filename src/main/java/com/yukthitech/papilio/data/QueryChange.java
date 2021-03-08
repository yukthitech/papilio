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

	public Boolean getTemplate()
	{
		return template;
	}

	public void setTemplate(Boolean template)
	{
		this.template = template;
	}
}
