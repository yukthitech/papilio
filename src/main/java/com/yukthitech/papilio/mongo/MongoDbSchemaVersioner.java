package com.yukthitech.papilio.mongo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCommandException;
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
import com.yukthitech.papilio.data.FindAndUpdateChange;
import com.yukthitech.papilio.data.InsertChange;
import com.yukthitech.papilio.data.QueryChange;
import com.yukthitech.papilio.data.UpdateChange;
import com.yukthitech.utils.CommonUtils;
import com.yukthitech.utils.ConvertUtils;
import com.yukthitech.utils.exceptions.InvalidArgumentException;
import com.yukthitech.utils.exceptions.InvalidStateException;
import com.yukthitech.utils.fmarker.FreeMarkerEngine;

/**
 * Db shcema versioner for mongodb.
 * @author akiran
 */
public class MongoDbSchemaVersioner implements IDbSchemaVersioner
{
	private static Logger logger = LogManager.getLogger(MongoDbSchemaVersioner.class);

	/**
	 * The Constant HOST_PORT.
	 */
	private static final Pattern HOST_PORT = Pattern.compile("([\\w\\.\\-]+)\\:(\\d+)");
	
	/**
	 * Used to parse query templates.
	 */
	private static FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
	
	/**
	 * Mongo client connection.
	 */
	private MongoClient mongoClient;
	
	/**
	 * Database on which operations needs to be performed.
	 */
	private MongoDatabase database;
	
	static
	{
		freeMarkerEngine.loadClass(MongoDbMethods.class);
	}
	
	@Override
	public void init(PapilioArguments args)
	{
		String user = args.getUserName();
		String password = args.getPassword();
		String database = args.getDbname();
		
		MongoCredential credential = (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) ? MongoCredential.createCredential(user, database, password.toCharArray()) : null;
		List<ServerAddress> mongoHosts = null;

		String host = args.getHost();
		int port = args.getPort();
		String replicas = args.getReplicas();
		
		if(StringUtils.isNotBlank(replicas))
		{
			mongoHosts = parse(replicas);
		}
		else if(StringUtils.isNotBlank(host) && port > 0)
		{
			replicas = host + ":" + port;
			mongoHosts = Arrays.asList(new ServerAddress(host, port));
		}
		else
		{
			throw new InvalidArgumentException("Both replicas and host details are not specified.");
		}
		
		MongoClientOptions clientOptions = MongoClientOptions.builder()
				.writeConcern(WriteConcern.ACKNOWLEDGED)
				.build();
				
		if(credential != null)
		{
			this.mongoClient = new MongoClient(mongoHosts, credential, clientOptions);
		}
		else
		{
			this.mongoClient = new MongoClient(mongoHosts, clientOptions);
		}
		
		this.database = mongoClient.getDatabase(database);
		MongoDbMethods.setDatabase(this.database);
		
		logger.debug("Connected to mongocluster {} successfully", replicas);
	}
	
	private List<ServerAddress> parse(String replicas)
	{
		String lst[] = replicas.trim().split("\\s*\\,\\s*");
		List<ServerAddress> serverAddresses = new ArrayList<>();
		
		for(String item : lst)
		{
			Matcher matcher = HOST_PORT.matcher(item);
			
			if(!matcher.matches())
			{
				throw new InvalidArgumentException("Invalid mongo host-port combination specified. It should be of format host:port. Specified Replicas: {}", replicas);
			}
			
			serverAddresses.add(new ServerAddress(matcher.group(1), Integer.parseInt(matcher.group(2))));
		}
		
		return serverAddresses;
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
				method = getSetter(cls, entry.getKey());
			}catch(NoSuchMethodException ex)
			{
				throw new InvalidConfigurationException("In options type '{}' no option found with name '{}' of type: {}", cls.getName(), entry.getKey(), value.getClass().getName(), ex);
			}
			
			value = ConvertUtils.convert(value, method.getParameterTypes()[0]);
			
			try
			{
				method.invoke(options, value);
			}catch(Exception ex)
			{
				throw new InvalidStateException("An error occurred while setting option: {}", entry.getKey(), ex);
			}
		}
	}
	
	private Method getSetter(Class<?> type, String name) throws NoSuchMethodException
	{
		Method methods[] = type.getMethods();
		
		for(Method method : methods)
		{
			if(method.getParameterCount() != 1)
			{
				continue;
			}
			
			if(method.getName().equals(name))
			{
				return method;
			}
		}
		
		throw new NoSuchMethodException("No setter method found with name: " + name);
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

		try
		{
			if(createCollectionOptions != null)
			{
				database.createCollection(tableChange.getTableName(), createCollectionOptions);
			}
			else
			{
				database.createCollection(tableChange.getTableName());
			}
		}catch(MongoCommandException ex)
		{
			if(Boolean.TRUE.equals(tableChange.getIgnoreIfExists()) && "NamespaceExists".equals(ex.getErrorCodeName()))
			{
				logger.warn("Ignoring error indicating collection already exist. Error: " + ex);
				return;
			}
			
			throw ex;
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
		
		if(createIndexOptions == null)
		{
			createIndexOptions = new IndexOptions();
		}
		
		createIndexOptions.name(indexChange.getIndexName());
		
		if(indexChange.isUnique())
		{
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
		
		collection.createIndex(Indexes.compoundIndex(indexCols), createIndexOptions);
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
		Document insertDoc = toDoc(change.getColumnMap(database));
		
		collection.insertOne(insertDoc);
	}
	
	private Bson toFilters(List<ColumnValue> conditions)
	{
		if(CollectionUtils.isEmpty(conditions))
		{
			return null;
		}
		
		List<Bson> condLst =  conditions.stream()
			.map(cond -> Filters.eq(cond.getName(), cond.getValue(database)))
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
			.map(colVal -> Updates.set(colVal.getName(), colVal.getValue(database)))
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
		String query = change.getQuery();
		
		if(Boolean.TRUE.equals(change.getTemplate()))
		{
			Object context = CommonUtils.toMap("change", change);
			query = freeMarkerEngine.processTemplate("query-template", change.getQuery(), context);
		}
		
		Map<String, Object> queryMap = null;
		
		try
		{
			queryMap = (Map) JsonUtils.parseJson(query);
		}catch(Exception ex)
		{
			throw new InvalidStateException("An error occurred while parsing input query as json. Query: {}", queryMap, ex);
		}
		
		logger.debug("Execuing query: {}", queryMap);
		
		Document res = database.runCommand(toDoc(queryMap));
		logger.debug("Query resulted in doc:\n{}", res.toJson());
	}
	
	@SuppressWarnings("unchecked")
	private List<Object> executeFinder(String queryStr)
	{
		Map<String, Object> query = null;
		
		try
		{
			query = (Map<String, Object>) JsonUtils.parseJson(queryStr);
		}catch(Exception ex)
		{
			throw new InvalidStateException("[Find-Update] An error occurred while parsing input query as json. Query: {}", query, ex);
		}
		
		logger.debug("[Find-Update] Execuing finder query: {}", query);
		
		try
		{
			Document res = database.runCommand(toDoc(query));
			return (List<Object>) PropertyUtils.getProperty(res, "cursor.firstBatch");
		}catch(Exception ex)
		{
			throw new InvalidStateException("[Find-Update] An error occurred while executing finder query. Query: {}", query, ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void executUpdate(String updateQuery)
	{
		Map<String, Object> query = null;
		
		try
		{
			query = (Map<String, Object>) JsonUtils.parseJson(updateQuery);
		}catch(Exception ex)
		{
			throw new InvalidStateException("[Find-Update] An error occurred while parsing update query as json. Query: {}", query, ex);
		}
		
		logger.debug("[Find-Update] Execuing update query: {}", query);
		
		Document res = database.runCommand(toDoc(query));
		logger.debug("[Find-Update] Query resulted in doc:\n{}", res.toJson());
	}

	@Override
	public void findAndUpdate(FindAndUpdateChange change)
	{
		List<Object> finderLst = executeFinder(change.getFindQuery());
		String updateTemplate = change.getUpdateQueryTemplate();
		
		logger.debug("Got {} objects by finder query", finderLst.size());
		
		int count = 0;
		
		for(Object object : finderLst)
		{
			//replace the _id property
			try
			{
				Object id = PropertyUtils.getProperty(object, "_id");
				
				if(id != null)
				{
					PropertyUtils.setProperty(object, "_id", id.toString());
				}
			}catch(Exception ex)
			{
				throw new InvalidStateException("An error occurred while converting id value", ex);
			}
			
			logger.debug("[Find-Update] Executing update-query for object [Index: {}]: {}", count, object);
			String updateQuery = freeMarkerEngine.processTemplate("update-query-template", updateTemplate, object);
			executUpdate(updateQuery);
			
			count++;
		}
	}

	@Override
	public void close()
	{
		logger.debug("Closing mongo connection..");
		mongoClient.close();
	}
}
