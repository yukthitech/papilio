package com.yukthitech.papilio.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;

/**
 * Change which would create a table / collection.
 * @author akiran
 */
public class CreateIndexChange extends AbstractOptionBasedChange implements IChange, Validateable
{
	/**
	 * Column to be indexed with details.
	 * @author akiran
	 */
	public static class IndexColumn implements Validateable
	{
		/**
		 * Name of the column.
		 */
		private String name;
		
		/**
		 * Ascending or descending order of index.
		 */
		private boolean ascending;
		
		/**
		 * Flag indicating if this column should be text index.
		 */
		private boolean textIndex;
		
		/**
		 * Instantiates a new index column.
		 */
		public IndexColumn()
		{}
		
		/**
		 * Instantiates a new index column.
		 *
		 * @param name the name
		 * @param ascending the ascending
		 * @param textIndex the text index
		 */
		public IndexColumn(String name, boolean ascending, boolean textIndex)
		{
			this.name = name;
			this.ascending = ascending;
			this.textIndex = textIndex;
		}

		/**
		 * Gets the name of the column.
		 *
		 * @return the name of the column
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Sets the name of the column.
		 *
		 * @param name the new name of the column
		 */
		public void setName(String name)
		{
			this.name = name;
		}

		/**
		 * Gets the ascending or descending order of index.
		 *
		 * @return the ascending or descending order of index
		 */
		public boolean isAscending()
		{
			return ascending;
		}

		/**
		 * Sets the ascending or descending order of index.
		 *
		 * @param ascending the new ascending or descending order of index
		 */
		public void setAscending(boolean ascending)
		{
			this.ascending = ascending;
		}

		/**
		 * Gets the flag indicating if this column should be text index.
		 *
		 * @return the flag indicating if this column should be text index
		 */
		public boolean isTextIndex()
		{
			return textIndex;
		}

		/**
		 * Sets the flag indicating if this column should be text index.
		 *
		 * @param textIndex the new flag indicating if this column should be text index
		 */
		public void setTextIndex(boolean textIndex)
		{
			this.textIndex = textIndex;
		}

		/* (non-Javadoc)
		 * @see com.yukthitech.ccg.xml.util.Validateable#validate()
		 */
		@Override
		public void validate() throws ValidateException
		{
			if(StringUtils.isBlank(name))
			{
				throw new ValidateException("Index-column name can not be empty.");
			}
		}
	}
	
	/**
	 * Name of the index.
	 */
	private String indexName;

	/**
	 * Table or collection name.
	 */
	private String tableName;
	
	/**
	 * Flag indicating if this is unique index.
	 */
	private boolean unique;
	
	/**
	 * List of index columns to be added to this index.
	 */
	private List<IndexColumn> indexColumns = new ArrayList<>();
	
	/**
	 * Instantiates a new creates the index change.
	 */
	public CreateIndexChange()
	{}
	
	/**
	 * Instantiates a new creates the index change. And adds speicified column as ASC column.
	 *
	 * @param indexName the index name
	 * @param tableName the table name
	 * @param unique the unique
	 * @param column the column
	 */
	public CreateIndexChange(String indexName, String tableName, boolean unique, String column)
	{
		this.indexName = indexName;
		this.tableName = tableName;
		this.unique = unique;
		
		addIndexColumn(new IndexColumn(column, true, false));
	}

	/**
	 * Gets the table or collection name.
	 *
	 * @return the table or collection name
	 */
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * Sets the table or collection name.
	 *
	 * @param tableName the new table or collection name
	 */
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	/**
	 * Gets the name of the index.
	 *
	 * @return the name of the index
	 */
	public String getIndexName()
	{
		return indexName;
	}

	/**
	 * Sets the name of the index.
	 *
	 * @param indexName the new name of the index
	 */
	public void setIndexName(String indexName)
	{
		this.indexName = indexName;
	}

	/**
	 * Gets the flag indicating if this is unique index.
	 *
	 * @return the flag indicating if this is unique index
	 */
	public boolean isUnique()
	{
		return unique;
	}

	/**
	 * Sets the flag indicating if this is unique index.
	 *
	 * @param unique the new flag indicating if this is unique index
	 */
	public void setUnique(boolean unique)
	{
		this.unique = unique;
	}

	/**
	 * Gets the list of index columns to be added to this index.
	 *
	 * @return the list of index columns to be added to this index
	 */
	public List<IndexColumn> getIndexColumns()
	{
		return indexColumns;
	}

	/**
	 * Sets the list of index columns to be added to this index.
	 *
	 * @param indexColumns the new list of index columns to be added to this index
	 */
	public void setIndexColumns(List<IndexColumn> indexColumns)
	{
		this.indexColumns = indexColumns;
	}
	
	/**
	 * Adds the index column.
	 *
	 * @param indexColumn the index column
	 */
	public void addIndexColumn(IndexColumn indexColumn)
	{
		indexColumns.add(indexColumn);
	}
	
	/**
	 * Validate.
	 *
	 * @throws ValidateException the validate exception
	 */
	/* (non-Javadoc)
	 * @see com.yukthitech.ccg.xml.util.Validateable#validate()
	 */
	@Override
	public void validate() throws ValidateException
	{
		if(StringUtils.isBlank(indexName))
		{
			throw new ValidateException("Index name can not be empty.");
		}
		
		if(StringUtils.isBlank(tableName))
		{
			throw new ValidateException("Table name can not be empty.");
		}
		
		if(indexColumns.isEmpty())
		{
			throw new ValidateException("No index columns specified.");
		}
	}
}
