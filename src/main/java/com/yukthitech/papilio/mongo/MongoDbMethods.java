package com.yukthitech.papilio.mongo;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.yukthitech.utils.CommonUtils;
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
}
