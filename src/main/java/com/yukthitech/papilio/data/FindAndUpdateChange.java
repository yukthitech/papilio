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
