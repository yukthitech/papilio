package com.yukthitech.papilio.mongo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.yukthitech.papilio.IDbSchemaVersioner;
import com.yukthitech.papilio.InvalidConfigurationException;
import com.yukthitech.papilio.common.JsonUtils;
import com.yukthitech.papilio.common.PapilioArguments;
import com.yukthitech.papilio.data.ColumnValue;
import com.yukthitech.papilio.data.CreateIndexChange;
import com.yukthitech.papilio.data.CreateTableChange;
import com.yukthitech.papilio.data.DeleteChange;
import com.yukthitech.papilio.data.InsertChange;
import com.yukthitech.papilio.data.QueryChange;
import com.yukthitech.papilio.data.UpdateChange;
import com.yukthitech.utils.exceptions.InvalidStateException;

/**
 * Db shcema versioner for mongodb.
 * @author akiran
 */
public class MongoDbSchemaVersioner implements IDbSchemaVersioner
{
	private static Logger logger = LogManager.getLogger(MongoDbSchemaVersioner.class);
	
	/**
	 * Mongo client connection.
	 */
	private MongoClient mongoClient;
	
	/**
	 * Database on which operations needs to be performed.
	 */
	private MongoDatabase database;
	
	@Override
	public void init(PapilioArguments args)
	{
		String user = args.getUserName();
		String password = args.getPassword();
		String database = args.getDbname();
		
		MongoCredential credential = (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) ? MongoCredential.createCredential(user, database, password.toCharArray()) : null;
		String host = args.getHost();
		int port = args.getPort();
		
		MongoClientOptions clientOptions = MongoClientOptions.builder()
				.writeConcern(WriteConcern.ACKNOWLEDGED)
				.build();
				
		if(credential != null)
		{
			this.mongoClient = new MongoClient(new ServerAddress(host, port), credential, clientOptions);
		}
		else
		{
			this.mongoClient = new MongoClient(new ServerAddress(host, port), clientOptions);
		}
		
		this.database = mongoClient.getDatabase(database);
		
		logger.debug("Connecting to {}:{} successfully", host, port);
	}
	
	private MongoCollection<Document> getCollection(String name)
	{
		try
		{
			return database.getCollection(name);
		}catch(IllegalArgumentException ex)
		{
			return null;
		}
	}
	
	private void setOptions(Object options, Map<String, Object> optionsMap)
	{
		Class<?> cls = options.getClass();
		
		for(Map.Entry<String, Object> entry : optionsMap.entrySet())
		{
			Object value = entry.getValue();
			
			if(value == null)
			{
				continue;
			}
			
			Method method = null;
			
			try
			{
				method = cls.getMethod(entry.getKey(), value.getClass());
			}catch(NoSuchMethodException ex)
			{
				throw new InvalidConfigurationException("In options type '{}' no option found with name '{}' of type: {}", cls.getName(), entry.getKey(), value.getClass().getName(), ex);
			}
			
			try
			{
				method.invoke(options, value);
			}catch(Exception ex)
			{
				throw new InvalidStateException("An error occurred while setting option: {}", entry.getKey(), ex);
			}
		}
	}
	
	@Override
	public boolean isTablePresent(String tableName)
	{
		logger.debug("Checking for presence of collection: {}", tableName);
		
		for(String col : database.listCollectionNames())
		{
			if(tableName.equals(col))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Map<String, String> fetchCurrentChangeSet(String dbLogCollection, String idCol, String checkSumCol)
	{
		logger.debug("Fetching current changeset..");
		
		MongoCollection<Document> collection = getCollection(dbLogCollection);
		Map<String, String> resMap = new HashMap<>();

		collection.find().forEach(new Consumer<Document>()
		{
			@Override
			public void accept(Document doc)
			{
				resMap.put(doc.getString(idCol), doc.getString(checkSumCol));
			}
		});
		
		logger.debug("Number of current changset entries found to be: {}", resMap.size());
		return resMap;
	}

	@Override
	public void createTable(CreateTableChange tableChange)
	{
		logger.debug("Creating collection: {}", tableChange.getTableName());
		
		CreateCollectionOptions createCollectionOptions = null;
		
		if(tableChange.getOptions() != null)
		{
			createCollectionOptions = new CreateCollectionOptions();
			Map<String, Object> options = tableChange.getOptions();
			
			setOptions(createCollectionOptions, options);
		}
				
		if(createCollectionOptions != null)
		{
			database.createCollection(tableChange.getTableName(), createCollectionOptions);
		}
		else
		{
			database.createCollection(tableChange.getTableName());
		}
	}

	@Override
	public void createIndex(CreateIndexChange indexChange)
	{
		logger.debug("Creating index '{}' on collection: {}", indexChange.getIndexName(), indexChange.getTableName());
		
		MongoCollection<Document> collection = getCollection(indexChange.getTableName());
		
		IndexOptions createIndexOptions = null;
		
		if(indexChange.getOptions() != null)
		{
			Map<String, Object> options = indexChange.getOptions();
			createIndexOptions = new IndexOptions();
			
			setOptions(createIndexOptions, options);
		}
		
		if(indexChange.isUnique())
		{
			if(createIndexOptions == null)
			{
				createIndexOptions = new IndexOptions();
			}
			
			createIndexOptions.unique(true);
		}
		
		List<Bson> indexCols = new ArrayList<Bson>();
		
		for(CreateIndexChange.IndexColumn col : indexChange.getIndexColumns())
		{
			if(col.isTextIndex())
			{
				indexCols.add(Indexes.text(col.getName()));
				continue;
			}
			
			if(col.isAscending())
			{
				indexCols.add(Indexes.ascending(col.getName()));
				continue;
			}
			
			indexCols.add(Indexes.descending(col.getName()));
		}
		
		if(createIndexOptions != null)
		{
			collection.createIndex(Indexes.compoundIndex(indexCols), createIndexOptions);
		}
		else
		{
			collection.createIndex(Indexes.compoundIndex(indexCols));
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Document toDoc(Map<String, Object> map)
	{
		if(map == null || map.isEmpty())
		{
			return null;
		}
		
		Document doc = new Document();
		
		for(Map.Entry<String, Object> entry : map.entrySet())
		{
			Object value = entry.getValue();
			
			if(value instanceof Map)
			{
				value = toDoc((Map) value);
			}
			
			doc.append(entry.getKey(), value);
		}
		
		return doc;
	}

	@Override
	public void insert(InsertChange change)
	{
		logger.debug("Inserting document into collection: {}", change.getTableName());
		
		MongoCollection<Document> collection = getCollection(change.getTableName());
		Document insertDoc = toDoc(change.getColumnMap());
		
		collection.insertOne(insertDoc);
	}
	
	private Bson toFilters(List<ColumnValue> conditions)
	{
		if(CollectionUtils.isEmpty(conditions))
		{
			return null;
		}
		
		List<Bson> condLst =  conditions.stream()
			.map(cond -> Filters.eq(cond.getName(), cond.getValue()))
			.collect(Collectors.toList());
		
		return Filters.and(condLst);
	}

	@Override
	public void update(UpdateChange change)
	{
		logger.debug("Updating document in collection: {}", change.getTableName());
		
		MongoCollection<Document> collection = getCollection(change.getTableName());
		
		List<Bson> updateFields = change.getColumnValues()
			.stream()
			.map(colVal -> Updates.set(colVal.getName(), colVal.getValue()))
			.collect(Collectors.toList());
		
		Bson updates = Updates.combine(updateFields);
		Bson filters = toFilters(change.getConditions());

		UpdateResult updateResult = null;
		
		if(MapUtils.isNotEmpty(change.getOptions()))
		{
			UpdateOptions options = new UpdateOptions();
			setOptions(options, change.getOptions());
			
			updateResult = collection.updateMany(filters, updates, options);
		}
		else
		{
			updateResult = collection.updateMany(filters, updates);
		}
		
		logger.debug("With update [Matched Count: {}, Updated Count: {}]", updateResult.getMatchedCount(), updateResult.getModifiedCount());
	}
	
	@Override
	public void delete(DeleteChange change)
	{
		logger.debug("Deleting document(s) from collection: {}", change.getTableName());
		
		DeleteResult res = null;
		
		try
		{
			MongoCollection<Document> collection = getCollection(change.getTableName());
			Bson filters = toFilters(change.getConditions());
			
			if(MapUtils.isNotEmpty(change.getOptions()))
			{
				DeleteOptions options = new DeleteOptions();
				setOptions(options, change.getOptions());
				
				res = collection.deleteMany(filters, options);
			}
			else
			{
				res = collection.deleteMany(filters);	
			}
		}catch(Throwable ex)
		{
			ex.printStackTrace();
		}
		
		logger.debug("Number of records deleted: {}", res.getDeletedCount());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void executQuery(QueryChange change)
	{
		Map<String, Object> query = null;
		
		try
		{
			query = (Map) JsonUtils.parseJson(change.getQuery());
		}catch(Exception ex)
		{
			throw new InvalidStateException("An error occurred while parsing input query as json. Query: {}", query, ex);
		}
		
		logger.debug("Execuing query: {}", query);
		
		Document res = database.runCommand(toDoc(query));
		logger.debug("Query resulted in doc:\n{}", res.toJson());
	}

	@Override
	public void close()
	{
		logger.debug("Closing mongo connection..");
		mongoClient.close();
	}
}
