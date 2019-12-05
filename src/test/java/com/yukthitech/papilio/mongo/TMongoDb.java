package com.yukthitech.papilio.mongo;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.yukthitech.papilio.Main;

/**
 * Tests the usage with mongodb.
 * @author akiran
 */
public class TMongoDb
{
	private static Logger logger = LogManager.getLogger(TMongoDb.class);
	
	/**
	 * Mongo client connection.
	 */
	private MongoClient mongoClient;
	
	/**
	 * Database on which operations needs to be performed.
	 */
	private MongoDatabase database;

	@BeforeClass
	public void setup() throws Exception
	{
		Properties prop = new Properties();
		prop.load(TMongoDb.class.getResourceAsStream("/mongodb.properties"));
		
		String database = prop.getProperty("database");
		String host = prop.getProperty("host");
		int port = Integer.parseInt( prop.getProperty("port") );
		
		MongoClientOptions clientOptions = MongoClientOptions.builder()
				.writeConcern(WriteConcern.ACKNOWLEDGED)
				.build();
			
		this.mongoClient = new MongoClient(new ServerAddress(host, port), clientOptions);
		this.database = mongoClient.getDatabase(database);
		
		cleandb();
	}
	
	private void cleandb()
	{
		MongoCollection<Document> changeLogCol =  database.getCollection("DATABASE_CHANGE_LOG");
		MongoCollection<Document> lockCol =  database.getCollection("DATABASE_CHANGE_LOG_LOCK");
		MongoCollection<Document> testCol =  database.getCollection("TEST_COL");
		
		if(changeLogCol != null)
		{
			logger.debug("Dropping change log table..");
			changeLogCol.drop();
		}

		if(lockCol != null)
		{
			logger.debug("Dropping change log lock table..");
			lockCol.drop();
		}

		if(testCol != null)
		{
			logger.debug("Dropping test table..");
			testCol.drop();
		}
	}

	@Test
	public void testBasicWorking()
	{
		int res = Main.execute(new String[] {
			"--host", "localhost",
			"--port", "27017",
			"--database", "weaver",
			"--changelog", "./src/test/resources/mongo/basic-working.xml",
			"--dbtype", "mongo"
		});
		
		Assert.assertEquals(res, 0);
	}
	
	@Test(dependsOnMethods = "testBasicWorking")
	public void testReexecute()
	{
		int res = Main.execute(new String[] {
				"--host", "localhost",
				"--port", "27017",
				"--database", "weaver",
				"--changelog", "./src/test/resources/mongo/basic-working.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(res, 0);
	}
	
	@AfterClass
	public void cleanup()
	{
		mongoClient.close();
	}
}
