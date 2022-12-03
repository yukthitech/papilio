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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;
import com.yukthitech.utils.exceptions.InvalidArgumentException;

/**
 * Represents a set of changes to be performed.
 * @author akiran
 */
public class ChangeSet implements Validateable
{
	/**
	 * Unique name or short description about this changed.
	 */
	private String id;
	
	/**
	 * Author of this change.
	 */
	private String author;
	
	/**
	 * File name in which changeset is specified.
	 */
	private String fileName;
	
	/**
	 * Calculate checksum of the changeset.
	 */
	private String checksum;
	
	/**
	 * List of changes that needs to be performed as part of this changeset.
	 */
	private List<IChange> changes = new ArrayList<>();

	/**
	 * Gets the unique name or short description about this changed.
	 *
	 * @return the unique name or short description about this changed
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Sets the unique name or short description about this changed.
	 *
	 * @param id the new unique name or short description about this changed
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	
	/**
	 * Gets the file name in which changeset is specified.
	 *
	 * @return the file name in which changeset is specified
	 */
	public String getFileName()
	{
		return fileName;
	}
	
	/**
	 * Sets the file name in which changeset is specified.
	 *
	 * @param fileName the new file name in which changeset is specified
	 */
	void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	/**
	 * Gets the author of this change.
	 *
	 * @return the author of this change
	 */
	public String getAuthor()
	{
		return author;
	}

	/**
	 * Sets the author of this change.
	 *
	 * @param author the new author of this change
	 */
	public void setAuthor(String author)
	{
		this.author = author;
	}

	/**
	 * Gets the list of changes that needs to be performed as part of this changeset.
	 *
	 * @return the list of changes that needs to be performed as part of this changeset
	 */
	public List<IChange> getChanges()
	{
		return changes;
	}

	/**
	 * Sets the list of changes that needs to be performed as part of this changeset.
	 *
	 * @param changes the new list of changes that needs to be performed as part of this changeset
	 */
	public void setChanges(List<IChange> changes)
	{
		this.changes = changes;
	}
	
	/**
	 * Adds the change.
	 *
	 * @param change the change
	 */
	public void addChange(IChange change)
	{
		if(change == null)
		{
			throw new NullPointerException("Change can not be null");
		}
		
		this.changes.add(change);
	}
	
	/**
	 * Adds the create index.
	 *
	 * @param change the change
	 */
	public void addCreateIndex(CreateIndexChange change)
	{
		this.addChange(change);
	}
	
	/**
	 * Adds the create table.
	 *
	 * @param change the change
	 */
	public void addCreateTable(CreateTableChange change)
	{
		this.addChange(change);
	}

	
	/**
	 * Adds the insert.
	 *
	 * @param change the change
	 */
	public void addInsert(InsertChange change)
	{
		this.addChange(change);
	}
	
	/**
	 * Adds the update.
	 *
	 * @param change the change
	 */
	public void addUpdate(UpdateChange change)
	{
		this.addChange(change);
	}

	/**
	 * Adds the delete.
	 *
	 * @param change the change
	 */
	public void addDelete(DeleteChange change)
	{
		this.addChange(change);
	}
	
	/**
	 * Adds the find and update.
	 *
	 * @param findAndUpdate the find and update
	 */
	public void addFindAndUpdate(FindAndUpdateChange findAndUpdate)
	{
		this.addChange(findAndUpdate);
	}

	/**
	 * Adds the query.
	 *
	 * @param query the query
	 */
	public void addQuery(String query)
	{
		if(StringUtils.isBlank(query))
		{
			throw new InvalidArgumentException("Query can not be empty");
		}
		
		this.addChange(new QueryChange(query));
	}
	
	public void addScript(String script)
	{
		if(StringUtils.isBlank(script))
		{
			throw new InvalidArgumentException("Script can not be empty");
		}
		
		this.addChange(new ScriptChange(script));
	}
	
	public void addExecScriptFile(String file)
	{
		if(StringUtils.isBlank(file))
		{
			throw new InvalidArgumentException("Script-file can not be empty");
		}

		File parentFile = DatabaseChangeLogFactory.getCurrentFile().getParentFile();
		File fileObj = new File(parentFile, file);

		try
		{
			String script = FileUtils.readFileToString(fileObj, Charset.defaultCharset());
			this.addChange(new ScriptChange(script));			
		}catch(Exception ex)
		{
			throw new InvalidArgumentException("Failed to read input file: {}", fileObj.getAbsolutePath());
		}
	}
	
	public void addQueryTemplate(String query)
	{
		if(StringUtils.isBlank(query))
		{
			throw new InvalidArgumentException("Query can not be empty");
		}
		
		QueryChange change = new QueryChange(query);
		change.setTemplate(true);
		
		this.addChange(change);
	}

	public void addQueryJelTemplate(String query)
	{
		if(StringUtils.isBlank(query))
		{
			throw new InvalidArgumentException("Query can not be empty");
		}
		
		QueryChange change = new QueryChange(query);
		change.setJelTemplate(true);
		
		this.addChange(change);
	}

	/**
	 * Gets the calculate checksum of the changeset.
	 *
	 * @return the calculate checksum of the changeset
	 */
	public String getChecksum()
	{
		return checksum;
	}

	/**
	 * Sets the calculate checksum of the changeset.
	 *
	 * @param checksum the new calculate checksum of the changeset
	 */
	public void setChecksum(String checksum)
	{
		this.checksum = checksum;
	}

	/* (non-Javadoc)
	 * @see com.yukthitech.ccg.xml.util.Validateable#validate()
	 */
	@Override
	public void validate() throws ValidateException
	{
		if(StringUtils.isEmpty(id))
		{
			throw new ValidateException("Id can not be null");
		}

		if(StringUtils.isEmpty(author))
		{
			throw new ValidateException("Author can not be null");
		}
		
		if(changes.isEmpty())
		{
			throw new ValidateException("No changes are specified under changeset");
		}
	}
}

