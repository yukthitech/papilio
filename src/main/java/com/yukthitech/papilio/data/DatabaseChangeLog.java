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
