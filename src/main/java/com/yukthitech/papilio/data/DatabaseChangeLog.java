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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;
import com.yukthitech.papilio.InvalidConfigurationException;
import com.yukthitech.utils.exceptions.InvalidArgumentException;

/**
 * Represents database change list.
 * @author akiran
 */
public class DatabaseChangeLog implements Validateable
{
	/**
	 * File from which this log is being loaded.
	 */
	private File file;
	
	/**
	 * List of changesets to be executed.
	 */
	private List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
	
	/**
	 * Tracker of change set ids.
	 */
	private Set<String> changesetIds = new HashSet<>();
	
	public DatabaseChangeLog(File file)
	{
		if(file == null)
		{
			throw new InvalidArgumentException("File cannot not be null or empty");
		}
		
		this.file = file;
	}

	/**
	 * Adds the file specified in include this log.
	 * @param include
	 */
	public void addInclude(Include include)
	{
		if(include == null)
		{
			throw new NullPointerException("Include can not be null");
		}
		
		DatabaseChangeLog logFromInclude = DatabaseChangeLogFactory.load( new File(file.getParentFile(), include.getPath()) );
		this.changeSets.addAll(logFromInclude.changeSets);
	}
	
	/**
	 * Adds the change set.
	 *
	 * @param changeSet the change set
	 */
	public void addChangeSet(ChangeSet changeSet)
	{
		if(changeSet == null)
		{
			throw new NullPointerException("Changeset can not be null");
		}
		
		if(changesetIds.contains(changeSet.getId()))
		{
			throw new InvalidConfigurationException("Duplicate changeset id encountered: {}", changeSet.getId());
		}
		
		changeSet.setFileName(file.getName());
		this.changeSets.add(changeSet);
		this.changesetIds.add(changeSet.getId());
	}
	
	/**
	 * Gets the list of changesets to be executed.
	 *
	 * @return the list of changesets to be executed
	 */
	public List<ChangeSet> getChangeSets()
	{
		return changeSets;
	}
	
	@Override
	public void validate() throws ValidateException
	{
		if(changeSets.isEmpty())
		{
			throw new ValidateException("Changesets is empty from file: " + file.getName());
		}
	}
}
