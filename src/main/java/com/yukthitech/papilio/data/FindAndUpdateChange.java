package com.yukthitech.papilio.data;

import org.apache.commons.lang3.StringUtils;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;

/**
 * Change to find document and execute update for each record/document.
 * 
 * @author akiran
 */
public class FindAndUpdateChange implements IChange, Validateable
{
	private String findQuery;

	private String updateQueryTemplate;

	public String getFindQuery()
	{
		return findQuery;
	}

	public void setFindQuery(String findQuery)
	{
		this.findQuery = findQuery;
	}

	public String getUpdateQueryTemplate()
	{
		return updateQueryTemplate;
	}

	public void setUpdateQueryTemplate(String updateQueryTemplate)
	{
		this.updateQueryTemplate = updateQueryTemplate;
	}

	@Override
	public void validate() throws ValidateException
	{
		if(StringUtils.isBlank(findQuery))
		{
			throw new ValidateException("No finder query specified.");
		}

		if(StringUtils.isBlank(updateQueryTemplate))
		{
			throw new ValidateException("No update query template specified.");
		}
	}
}
