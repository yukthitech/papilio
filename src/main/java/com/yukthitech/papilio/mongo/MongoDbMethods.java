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
