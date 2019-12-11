package com.yukthitech.papilio.data;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;
import com.yukthitech.papilio.common.JsonUtils;
import com.yukthitech.utils.exceptions.InvalidArgumentException;
import com.yukthitech.utils.exceptions.InvalidStateException;

/**
 * Column and value combination.
 * @author akiran
 */
public class ColumnValue implements Validateable
{
	/**
	 * Column name.
	 */
	private String name;
	
	/**
	 * Value for the column.
	 */
	private Object value;
	
	/**
	 * Instantiates a new column value.
	 */
	public ColumnValue()
	{}
	
	/**
	 * Instantiates a new column value.
	 *
	 * @param column the column
	 * @param value the value
	 */
	public ColumnValue(String column, Object value)
	{
		this.name = column;
		this.value = value;
	}

	/**
	 * Sets the column name.
	 *
	 * @param column the new column name
	 */
	public void setName(String column)
	{
		this.name = column;
	}
	
	/**
	 * Gets the column name.
	 *
	 * @return the column name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Gets the value for the column.
	 *
	 * @return the value for the column
	 */
	public Object getValue()
	{
		return value;
	}
	
	/**
	 * Sets the value for the column.
	 *
	 * @param value the new value for the column
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
	
	/**
	 * Sets the int value.
	 *
	 * @param value the new int value
	 */
	public void setIntValue(int value)
	{
		this.value = value;
	}
	
	/**
	 * Sets the boolean value.
	 *
	 * @param value the new boolean value
	 */
	public void setBooleanValue(boolean value)
	{
		this.value = value;
	}
	
	/**
	 * Sets the value from file.
	 *
	 * @param file the new value from file
	 */
	public void setValueFromFile(String file)
	{
		File parentFile = DatabaseChangeLogFactory.getCurrentFile().getParentFile();
		File fileObj = new File(parentFile, file);
		
		if(!fileObj.exists())
		{
			throw new InvalidArgumentException("Invalid/non-existing file specified: {}", file);
		}
		
		try
		{
			this.value = FileUtils.readFileToString(fileObj);
		}catch(Exception ex)
		{
			throw new InvalidStateException("Failed to load text content from file: {}", file);
		}
	}
	
	/**
	 * Sets the json value.
	 *
	 * @param json the new json value
	 */
	public void setJsonValue(String json)
	{
		this.value = JsonUtils.parseJson(json);
	}
	
	/* (non-Javadoc)
	 * @see com.yukthitech.ccg.xml.util.Validateable#validate()
	 */
	@Override
	public void validate() throws ValidateException
	{
		if(StringUtils.isBlank(name))
		{
			throw new ValidateException("Column name can not be empty");
		}
	}
}
