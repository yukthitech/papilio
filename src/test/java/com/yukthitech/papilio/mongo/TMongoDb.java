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
		MongoCollection<Document> testCol2 =  database.getCollection("TEST_COL2");
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

		if(testCol2 != null)
		{
			logger.debug("Dropping test doc table..");
			testCol2.drop();
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
			"--database", "test",
			"--changelog", "./src/test/resources/mongo/basic-working.xml",
			"--dbtype", "mongo"
		});
		
		Assert.assertEquals(tracker.getExitCode(), 0);
		Assert.assertEquals(tracker.getTotalCount(), 4);
		Assert.assertEquals(tracker.getExecutedCount(), 4);
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
		
		Assert.assertEquals(actual, CommonUtils.toSet("Kranthi-Kiran", "Pipsy-PipsyPostDel"));
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
				"--database", "test",
				"--changelog", "./src/test/resources/mongo/basic-working.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(tracker.getExitCode(), 0);
		Assert.assertEquals(tracker.getTotalCount(), 4);
		Assert.assertEquals(tracker.getExecutedCount(), 0);
		Assert.assertEquals(tracker.getSkipCount(), 4);
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
				"--database", "test",
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
				"--database", "test",
				"--changelog", "./src/test/resources/mongo/master.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(tracker.getExitCode(), 0);
		Assert.assertEquals(tracker.getTotalCount(), 6);
		Assert.assertEquals(tracker.getExecutedCount(), 2);
		Assert.assertEquals(tracker.getSkipCount(), 4);
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
	
	private String getChecksum(String id)
	{
		MongoCollection<Document> changeLogCol =  database.getCollection("DATABASE_CHANGE_LOG");
		FindIterable<Document> docs = changeLogCol.find(new org.bson.Document("CHANGE_SET_ID", id));
		return (String) docs.first().get("CHECKSUM");
	}

	/**
	 * Ensures white-spaces is not having any affect on checksum
	 */
	@Test(dependsOnMethods = "testBasicWorking")
	public void testCheksumForWhitespaces()
	{
		ChangeTracker tracker = Main.execute(new String[] {
				"--host", "localhost",
				"--port", "27017",
				"--database", "test",
				"--changelog", "./src/test/resources/mongo/checksum/change1/checksum.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(tracker.getExitCode(), 0);
		Assert.assertEquals(tracker.getTotalCount(), 2);
		Assert.assertEquals(tracker.getExecutedCount(), 2);
		Assert.assertEquals(tracker.getSkipCount(), 0);
		Assert.assertNull(tracker.getErroredChangesetId());
		
		//when content is change only in terms of whitespaces there should not be any problem
		tracker = Main.execute(new String[] {
				"--host", "localhost",
				"--port", "27017",
				"--database", "test",
				"--changelog", "./src/test/resources/mongo/checksum/change2/checksum.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(tracker.getExitCode(), 0);
		Assert.assertEquals(tracker.getTotalCount(), 2);
		Assert.assertEquals(tracker.getExecutedCount(), 0);
		Assert.assertEquals(tracker.getSkipCount(), 2);
		Assert.assertNull(tracker.getErroredChangesetId());
	}

	/**
	 * Ensures checksum is getting changed with content.
	 * Along with that make sure with system-property checksum is getting updated.
	 */
	@Test(dependsOnMethods = "testCheksumForWhitespaces")
	public void testCheksumChanges()
	{
		ChangeTracker tracker = Main.execute(new String[] {
				"--host", "localhost",
				"--port", "27017",
				"--database", "test",
				"--changelog", "./src/test/resources/mongo/checksum/change3/checksum.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(tracker.getExitCode(), -1);
		Assert.assertEquals(tracker.getTotalCount(), 2);
		Assert.assertEquals(tracker.getExecutedCount(), 0);
		Assert.assertEquals(tracker.getSkipCount(), 0);
		Assert.assertEquals(tracker.getErroredChangesetId(), "Adding record for checksum test-1");
		
		String prechecksum = getChecksum("Adding record for checksum test-1");

		//with system property ensure there are no errors
		System.setProperty("papilio.updateChecksum", "true");

		try
		{
			tracker = Main.execute(new String[] {
					"--host", "localhost",
					"--port", "27017",
					"--database", "test",
					"--changelog", "./src/test/resources/mongo/checksum/change3/checksum.xml",
					"--dbtype", "mongo"
				});
			
			Assert.assertEquals(tracker.getExitCode(), 0);
			Assert.assertEquals(tracker.getTotalCount(), 2);
			Assert.assertEquals(tracker.getExecutedCount(), 0);
			Assert.assertEquals(tracker.getSkipCount(), 0);
			Assert.assertNull(tracker.getErroredChangesetId());
	
			String postchecksum = getChecksum("Adding record for checksum test-1");
			Assert.assertNotNull(prechecksum);
			Assert.assertNotNull(postchecksum);
			Assert.assertNotEquals(prechecksum, postchecksum);
		} finally
		{
			//revert the system prop for future test cases
			System.setProperty("papilio.updateChecksum", "false");
		}
		
	}

	@Test(dependsOnMethods = "testBasicWorking")
	public void testFindAndUpdate()
	{
		ChangeTracker tracker = Main.execute(new String[] {
				"--host", "localhost",
				"--port", "27017",
				"--database", "test",
				"--changelog", "./src/test/resources/mongo/find-n-update.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(tracker.getExitCode(), 0);
		Assert.assertEquals(tracker.getTotalCount(), 1);
		Assert.assertEquals(tracker.getExecutedCount(), 1);
		Assert.assertEquals(tracker.getSkipCount(), 0);

		//ensure db is updated with right records
		MongoCollection<Document> testCol =  database.getCollection("TEST_COL");
		
		FindIterable<Document> docs = testCol.find();
		int count = 0;
		
		for(Document doc : docs)
		{
			Assert.assertEquals(doc.getString("lowerName"), 
					doc.getString("name").toLowerCase());
			count ++;
		}
		
		Assert.assertTrue(count > 0);
	}

	@AfterClass
	public void cleanup()
	{
		mongoClient.close();
	}
}
