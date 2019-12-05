package com.yukthitech.papilio.data;

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
}
