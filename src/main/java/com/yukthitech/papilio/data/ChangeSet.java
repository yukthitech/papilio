package com.yukthitech.papilio.data;

import java.util.ArrayList;
import java.util.List;

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

