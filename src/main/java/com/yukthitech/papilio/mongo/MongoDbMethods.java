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
package com.yukthitech.papilio.mongo;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.yukthitech.papilio.common.PapilioUtils;
import com.yukthitech.papilio.data.DatabaseChangeLogFactory;
import com.yukthitech.utils.CommonUtils;
import com.yukthitech.utils.exceptions.InvalidArgumentException;
import com.yukthitech.utils.exceptions.InvalidStateException;
import com.yukthitech.utils.fmarker.annotaion.FreeMarkerMethod;

/**
 * Freemarker methods to support dynamic data in mongo queries.
 * @author akiran
 */
public class MongoDbMethods
{
	private static Logger logger = LogManager.getLogger(MongoDbMethods.class);
	
	/**
	 * Current mongo client.
	 */
	private static MongoDatabase database;

	public static void setDatabase(MongoDatabase database)
	{
		MongoDbMethods.database = database;
	}

	/**
	 * Fetches id of document with specified field-value combination in specified collection.
	 *
	 * @param collection the collection to check
	 * @param field condition field
	 * @param value condition value
	 * @return matching id if any
	 */
	@FreeMarkerMethod
	public static String fetchId(String collection, String field, String value)
	{
		Map<String, Object> conditions = CommonUtils.toMap(field, value);
		
		logger.debug("Fetching document-id from collection '{}' with conditions: {}", collection, conditions);
		
		FindIterable<Document> docs = database.getCollection(collection).find(new Document(conditions));
		Document doc = docs.first();
		
		if(doc == null)
		{
			logger.warn("No document found in collection '{}' with conditions: {}", collection, conditions);
			return null;
		}
		
		ObjectId id = (ObjectId) doc.get("_id");
		logger.debug("Got the id as: {}", id);
		return id.toString();
	}
	
	/**
	 * Load string content from file.
	 *
	 * @param file
	 *            the file
	 * @return string from file
	 */
	@FreeMarkerMethod
	public static String loadTextFile(String file)
	{
		File parentFile = DatabaseChangeLogFactory.getCurrentFile().getParentFile();
		File fileObj = new File(parentFile, file);
		
		if(!fileObj.exists())
		{
			throw new InvalidArgumentException("Invalid/non-existing file specified: {}", file);
		}
		
		try
		{
			return FileUtils.readFileToString(fileObj, Charset.forName("utf8"));
		}catch(Exception ex)
		{
			throw new InvalidStateException("Failed to load text content from file: {}", file);
		}
	}
	
	/**
	 * Loads the objects from specified json file.
	 *
	 * @param file
	 *            the file
	 * @return the object
	 */
	@FreeMarkerMethod
	public static Object loadJsonFile(String file)
	{
		File parentFile = DatabaseChangeLogFactory.getCurrentFile().getParentFile();
		File fileObj = new File(parentFile, file);
		
		if(!fileObj.exists())
		{
			throw new InvalidArgumentException("Invalid/non-existing file specified: {}", file);
		}
		
		try
		{
			String fileContent = FileUtils.readFileToString(fileObj, Charset.forName("utf8"));
			return PapilioUtils.parseJson(fileContent);
		}catch(Exception ex)
		{
			throw new InvalidStateException("Failed to load json content from file: {}", file);
		}
	}
}
