package com.yukthitech.papilio.mongo;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.yukthitech.papilio.ChangeTracker;
import com.yukthitech.papilio.Main;
import com.yukthitech.utils.CommonUtils;

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
		MongoCollection<Document> testDocCol =  database.getCollection("TEST_DOC");
		
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

		if(testDocCol != null)
		{
			logger.debug("Dropping test doc table..");
			testDocCol.drop();
		}
	}

	/**
	 * Ensures basic working of papilio.
	 * 	creating collections
	 * 	creating indexes
	 * 	insert
	 * 	update
	 */
	@Test
	public void testBasicWorking()
	{
		ChangeTracker tracker = Main.execute(new String[] {
			"--host", "localhost",
			"--port", "27017",
			"--database", "weaver",
			"--changelog", "./src/test/resources/mongo/basic-working.xml",
			"--dbtype", "mongo"
		});
		
		Assert.assertEquals(tracker.getExitCode(), 0);
		Assert.assertEquals(tracker.getTotalCount(), 2);
		Assert.assertEquals(tracker.getExecutedCount(), 2);
		Assert.assertEquals(tracker.getSkipCount(), 0);
		
		//ensure db is updated with right records
		MongoCollection<Document> testCol =  database.getCollection("TEST_COL");
		Assert.assertEquals(testCol.countDocuments(), 2);
		
		FindIterable<Document> docs = testCol.find();
		Set<String> actual = new HashSet<>();
		
		for(Document doc : docs)
		{
			actual.add( doc.getString("name") + "-" + doc.getString("lastName"));
		}
		
		Assert.assertEquals(actual, CommonUtils.toSet("Kranthi-Kiran", "Pipsy-PipsyNew"));
	}
	
	/**
	 * Ensures already executed changesets are skipped.
	 */
	@Test(dependsOnMethods = "testBasicWorking")
	public void testReexecute()
	{
		ChangeTracker tracker = Main.execute(new String[] {
				"--host", "localhost",
				"--port", "27017",
				"--database", "weaver",
				"--changelog", "./src/test/resources/mongo/basic-working.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(tracker.getExitCode(), 0);
		Assert.assertEquals(tracker.getTotalCount(), 2);
		Assert.assertEquals(tracker.getExecutedCount(), 0);
		Assert.assertEquals(tracker.getSkipCount(), 2);
	}
	
	/**
	 * Ensure when content is changed, it throws different checksum error.
	 */
	@Test(dependsOnMethods = "testBasicWorking")
	public void testChecksumFinding()
	{
		ChangeTracker tracker = Main.execute(new String[] {
				"--host", "localhost",
				"--port", "27017",
				"--database", "weaver",
				"--changelog", "./src/test/resources/mongo/copy/basic-working.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(tracker.getExitCode(), -1);
		Assert.assertEquals(tracker.getTotalCount(), 2);
		Assert.assertEquals(tracker.getExecutedCount(), 0);
		Assert.assertEquals(tracker.getSkipCount(), 1);
		Assert.assertEquals(tracker.getErroredChangesetId(), "Adding records to test table");
		Assert.assertTrue(tracker.getErrorMessage().contains("Old checksum"));
	}

	/**
	 * Ensures the following
	 * 	including other changeset files in main file
	 * 	content loading from external files
	 * 	files being loaded are relative to current file referring to them.
	 */
	@Test(dependsOnMethods = "testBasicWorking")
	public void testExternalFileLoading()
	{
		ChangeTracker tracker = Main.execute(new String[] {
				"--host", "localhost",
				"--port", "27017",
				"--database", "weaver",
				"--changelog", "./src/test/resources/mongo/master.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(tracker.getExitCode(), 0);
		Assert.assertEquals(tracker.getTotalCount(), 4);
		Assert.assertEquals(tracker.getExecutedCount(), 2);
		Assert.assertEquals(tracker.getSkipCount(), 2);
		Assert.assertNull(tracker.getErroredChangesetId());

		//ensure db is updated with right records
		MongoCollection<Document> testCol =  database.getCollection("TEST_DOC");
		Assert.assertEquals(testCol.countDocuments(), 2);
		
		FindIterable<Document> docs = testCol.find();
		Set<String> actual = new HashSet<>();
		
		for(Document doc : docs)
		{
			actual.add( doc.getString("name") + "-" + doc.getString("content"));
		}
		
		Assert.assertEquals(actual, CommonUtils.toSet("file1-Some content from file1", "file2-Some content from file2"));
	}

	@AfterClass
	public void cleanup()
	{
		mongoClient.close();
	}
}
