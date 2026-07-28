package com.yukthitech.papilio.mongo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.yukthitech.papilio.ChangeTracker;
import com.yukthitech.papilio.Main;
import com.yukthitech.utils.CommonUtils;

/**
 * Tests the usage with mongodb.
 * @author akiran
 */
public class TestMongoDb
{
	private static Logger logger = LogManager.getLogger(TestMongoDb.class);
	
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
		prop.load(TestMongoDb.class.getResourceAsStream("/mongodb.properties"));
		
		String database = prop.getProperty("database");
		String host = prop.getProperty("host");
		int port = Integer.parseInt( prop.getProperty("port") );
		
		MongoClientSettings clientSettings = MongoClientSettings.builder()
				.applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(host, port))))
				.writeConcern(WriteConcern.ACKNOWLEDGED)
				.build();
			
		this.mongoClient = MongoClients.create(clientSettings);
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
		MongoCollection<Document> concurrentLockCol =  database.getCollection("CONCURRENT_LOCK_COL");
		
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

		if(concurrentLockCol != null)
		{
			logger.debug("Dropping concurrent lock test table..");
			concurrentLockCol.drop();
		}
	}
	
	private String[] mongoArgs(String changelog)
	{
		return new String[] {
			"--host", "localhost",
			"--port", "27017",
			"--database", "test",
			"--changelog", changelog,
			"--dbtype", "mongo",
		};
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
			"--dbtype", "mongo",
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
		Assert.assertEquals(tracker.getTotalCount(), 8);
		Assert.assertEquals(tracker.getExecutedCount(), 4);
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

	@Test(dependsOnMethods = "testBasicWorking")
	public void testJsonPathSubquery()
	{
		ChangeTracker tracker = Main.execute(new String[] {
				"--host", "localhost",
				"--port", "27017",
				"--database", "test",
				"--changelog", "./src/test/resources/mongo/jsonpath-subquery.xml",
				"--dbtype", "mongo"
			});
		
		Assert.assertEquals(tracker.getExitCode(), 0);
		Assert.assertEquals(tracker.getTotalCount(), 1);
		Assert.assertEquals(tracker.getExecutedCount(), 1);
		Assert.assertEquals(tracker.getSkipCount(), 0);

		MongoCollection<Document> testCol = database.getCollection("TEST_COL");
		Document kranthiDoc = testCol.find(new org.bson.Document("name", "Kranthi")).first();
		Assert.assertNotNull(kranthiDoc);
		String expectedId = kranthiDoc.getObjectId("_id").toString();

		MongoCollection<Document> testCol2 = database.getCollection("TEST_COL2");
		Document jsonPathDoc = testCol2.find(new org.bson.Document("name", "KranthiJsonPath")).first();
		Assert.assertNotNull(jsonPathDoc);
		Assert.assertEquals(jsonPathDoc.getString("testColId"), expectedId);
	}

	/**
	 * Ensures a second execution waits when lock is held, then continues after release
	 * instead of failing immediately with LOCK_FAILED.
	 */
	@Test(dependsOnMethods = "testBasicWorking")
	public void testLockWaitUntilReleased() throws Exception
	{
		MongoCollection<Document> lockCol = database.getCollection("DATABASE_CHANGE_LOG_LOCK");
		lockCol.deleteMany(new Document());
		lockCol.insertOne(new Document("NAME", "LOCK"));

		System.setProperty("papilio.lockRetryInterval", "200");
		System.setProperty("papilio.lockWaitTimeout", "30000");

		AtomicReference<ChangeTracker> trackerRef = new AtomicReference<>();
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(1);

		try
		{
			Thread waiter = new Thread(() -> {
				started.countDown();
				trackerRef.set(Main.execute(mongoArgs("./src/test/resources/mongo/concurrent-lock.xml")));
				finished.countDown();
			}, "papilio-lock-waiter");

			waiter.start();
			Assert.assertTrue(started.await(5, TimeUnit.SECONDS));

			// Give the waiter time to hit the held lock and enter retry loop
			Thread.sleep(1500);

			Assert.assertEquals(finished.getCount(), 1, "Execution should still be waiting for lock");

			lockCol.deleteMany(new Document("NAME", "LOCK"));
			Assert.assertTrue(finished.await(30, TimeUnit.SECONDS), "Execution did not finish after lock release");

			ChangeTracker tracker = trackerRef.get();
			Assert.assertNotNull(tracker);
			Assert.assertEquals(tracker.getExitCode(), 0);
			Assert.assertNotEquals(tracker.getErrorMessage(), ChangeTracker.LOCK_FAILED);
			Assert.assertNull(tracker.getErroredChangesetId());
			Assert.assertEquals(tracker.getExecutedCount(), 2);
			Assert.assertEquals(database.getCollection("CONCURRENT_LOCK_COL").countDocuments(), 1);
		} finally
		{
			System.clearProperty("papilio.lockRetryInterval");
			System.clearProperty("papilio.lockWaitTimeout");
			lockCol.deleteMany(new Document("NAME", "LOCK"));
		}
	}

	/**
	 * Ensures two concurrent papilio executions serialize via the lock table:
	 * one executes changesets and the other waits, then skips already-applied ones.
	 */
	@Test(dependsOnMethods = "testLockWaitUntilReleased")
	public void testConcurrentExecutionsWaitForLock() throws Exception
	{
		database.getCollection("CONCURRENT_LOCK_COL").drop();
		database.getCollection("DATABASE_CHANGE_LOG").deleteMany(
				new Document("CHANGE_SET_ID", new Document("$in", Arrays.asList(
						"Creation of concurrent lock test table",
						"Adding concurrent lock test record"))));
		database.getCollection("DATABASE_CHANGE_LOG_LOCK").deleteMany(new Document());

		System.setProperty("papilio.lockRetryInterval", "200");
		System.setProperty("papilio.lockWaitTimeout", "60000");

		String[] args = mongoArgs("./src/test/resources/mongo/concurrent-lock.xml");
		ExecutorService executor = Executors.newFixedThreadPool(2);

		try
		{
			Future<ChangeTracker> first = executor.submit(() -> Main.execute(args));
			Future<ChangeTracker> second = executor.submit(() -> Main.execute(args));

			ChangeTracker tracker1 = first.get(60, TimeUnit.SECONDS);
			ChangeTracker tracker2 = second.get(60, TimeUnit.SECONDS);

			Assert.assertEquals(tracker1.getExitCode(), 0, "First concurrent execution failed: " + tracker1.getErrorMessage());
			Assert.assertEquals(tracker2.getExitCode(), 0, "Second concurrent execution failed: " + tracker2.getErrorMessage());
			Assert.assertNotEquals(tracker1.getErrorMessage(), ChangeTracker.LOCK_FAILED);
			Assert.assertNotEquals(tracker2.getErrorMessage(), ChangeTracker.LOCK_FAILED);

			// Across both runs, each changeset is executed once and skipped once
			Assert.assertEquals(tracker1.getExecutedCount() + tracker2.getExecutedCount(), 2);
			Assert.assertEquals(tracker1.getSkipCount() + tracker2.getSkipCount(), 2);

			Assert.assertEquals(database.getCollection("CONCURRENT_LOCK_COL").countDocuments(), 1);
			Assert.assertEquals(database.getCollection("DATABASE_CHANGE_LOG_LOCK").countDocuments(), 0);
		} finally
		{
			executor.shutdownNow();
			System.clearProperty("papilio.lockRetryInterval");
			System.clearProperty("papilio.lockWaitTimeout");
		}
	}

	@AfterClass
	public void cleanup()
	{
		mongoClient.close();
	}
}
