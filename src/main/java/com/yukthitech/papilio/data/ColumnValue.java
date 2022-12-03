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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import com.yukthitech.ccg.xml.util.ValidateException;
import com.yukthitech.ccg.xml.util.Validateable;
import com.yukthitech.papilio.common.PapilioUtils;
import com.yukthitech.papilio.mongo.MongoDbMethods;
import com.yukthitech.utils.exceptions.InvalidStateException;

/**
 * Column and value combination.
 * @author akiran
 */
public class ColumnValue implements Validateable
{
	/**
	 * The logger.
	 */
	private static Logger logger = LogManager.getLogger(ColumnValue.class);
	
	/**
	 * Used to subquery json.
	 */
	private static ObjectMapper objectMapper = new ObjectMapper();
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * Column name.
	 */
	private String name;
	
	/**
	 * Value for the column.
	 */
	private Object value;
	
	/**
	 * Subquery to be used to fetch the value for this column.
	 */
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	private String valueQuery;
	
	/**
	 * Property to be used to fetch the final value from the result of value query.
	 */
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	private String valueQueryPath;
	
	/**
	 * Flag indicating if value-query-path infers multiple values or single value.
	 */
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	private Boolean multiValued;
	
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
	 * Gets the value from query.
	 *
	 * @param database the database
	 * @return the value from query
	 */
	private Object getValueFromQuery(MongoDatabase database)
	{
		//database can be null, when old methods are called (which was added for backward compatibility)
		if(valueQuery == null || database == null)
		{
			return null;
		}
		
		try
		{
			Document bsonQueryDoc = Document.parse(valueQuery);
			String resultJson = database.runCommand(bsonQueryDoc).toJson();
			
			Object result = objectMapper.readValue(resultJson, Object.class);
			
			logger.debug("Got result of subquery as: {}", result);
			
			if(valueQueryPath != null)
			{
				if(Boolean.TRUE.equals(multiValued))
				{
					result = JXPathContext.newContext(result).selectNodes(valueQueryPath);
				}
				else
				{
					result = JXPathContext.newContext(result).getValue(valueQueryPath);
				}
				
				if(result instanceof ObjectId)
				{
					result = ((ObjectId) result).toString();
				}
			}
			
			return result;
			
		}catch(Exception ex)
		{
			throw new InvalidStateException("An error occurred while processing value-query for column '{}'. Value-query-path: {}, Query:\n{}", 
					name, valueQueryPath, valueQuery, ex);
		}
	}
	
	/**
	 * Gets the value for the column.
	 *
	 * @return the value for the column
	 */
	public Object getValue(MongoDatabase database)
	{
		if(value != null)
		{
			return value;
		}
		
		return getValueFromQuery(database);
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
	 * Gets the value for the column.
	 *
	 * @return the value for the column
	 */
	public Object getValue()
	{
		return value;
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
	 * Gets the subquery to be used to fetch the value for this column.
	 *
	 * @return the subquery to be used to fetch the value for this column
	 */
	public String getValueQuery()
	{
		return valueQuery;
	}

	/**
	 * Sets the subquery to be used to fetch the value for this column.
	 *
	 * @param valueQuery the new subquery to be used to fetch the value for this column
	 */
	public void setValueQuery(String valueQuery)
	{
		this.valueQuery = valueQuery;
	}

	/**
	 * Sets the property to be used to fetch the final value from the result of value query.
	 *
	 * @param valueQueryPath the new property to be used to fetch the final value from the result of value query
	 */
	public void setValueQueryPath(String valueQueryPath)
	{
		this.valueQueryPath = valueQueryPath;
	}
	
	/**
	 * Gets the property to be used to fetch the final value from the result of value query.
	 *
	 * @return the property to be used to fetch the final value from the result of value query
	 */
	public String getValueQueryPath()
	{
		return valueQueryPath;
	}

	/**
	 * Sets the flag indicating if value-query-path infers multiple values or single value.
	 *
	 * @param multiValued the new flag indicating if value-query-path infers multiple values or single value
	 */
	public void setMultiValued(boolean multiValued)
	{
		this.multiValued = multiValued;
	}
	
	/**
	 * Gets the flag indicating if value-query-path infers multiple values or single value.
	 *
	 * @return the flag indicating if value-query-path infers multiple values or single value
	 */
	public Boolean getMultiValued()
	{
		return multiValued;
	}

	/**
	 * Sets the value from file.
	 *
	 * @param file the new value from file
	 */
	public void setValueFromFile(String file)
	{
		this.value = MongoDbMethods.loadTextFile(file);
	}
	
	/**
	 * Loads the object from json file.
	 *
	 * @param file the new json from file
	 */
	public void setJsonFromFile(String file)
	{
		this.value = MongoDbMethods.loadJsonFile(file);
	}

	/**
	 * Loads the object from xml file.
	 *
	 * @param file the new xml from file
	 */
	public void setXmlFromFile(String file)
	{
		File parentFile = DatabaseChangeLogFactory.getCurrentFile().getParentFile();
		File fileObj = new File(parentFile, file);
		
		value = PapilioUtils.loadXml(fileObj);
	}

	/**
	 * Sets the json value.
	 *
	 * @param json the new json value
	 */
	public void setJsonValue(String json)
	{
		this.value = PapilioUtils.parseJson(json);
	}
	
	/**
	 * Sets the value parsed from specified jel-template json. 
	 * @param jelTemplate JEL json template
	 */
	public void setJelValue(String jelTemplate)
	{
		String finalJson = PapilioUtils.processJelTemplate(jelTemplate, new HashMap<String, Object>());
		this.value = PapilioUtils.parseJson(finalJson);
	}
	
	public void setDateValue(String value) throws Exception
	{
		Date date = DATE_FORMAT.parse(value);
		date = DateUtils.truncate(date, Calendar.DATE);
		
		this.value = date;
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
		if(StringUtils.isBlank(name))
		{
			throw new ValidateException("Column name can not be empty");
		}
	}
}
