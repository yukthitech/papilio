package com.yukthitech.papilio;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import com.yukthitech.papilio.common.PapilioArguments;
import com.yukthitech.papilio.data.DatabaseChangeLog;
import com.yukthitech.papilio.data.DatabaseChangeLogFactory;
import com.yukthitech.utils.cli.CommandLineOptions;
import com.yukthitech.utils.cli.MissingArgumentException;
import com.yukthitech.utils.cli.OptionsFactory;

/**
 * Main class which would do the db versioing.
 * @author akiran
 */
public class Main
{
	private static final String COMMAND_SYNTAX = String.format("java %s args...", Main.class.getName());
	
	/**
	 * Parses and load command line arguments into {@link PapilioArguments}
	 * @param args
	 * @return
	 */
	private static PapilioArguments loadArguments(String args[])
	{
		CommandLineOptions commandLineOptions = OptionsFactory.buildCommandLineOptions(PapilioArguments.class);
		PapilioArguments basicArguments = null;
		
		try
		{
			basicArguments = (PapilioArguments) commandLineOptions.parseBean(args);
		}catch(MissingArgumentException ex)
		{
			System.err.println("Error: " + ex.getMessage());
			System.err.println(commandLineOptions.fetchHelpInfo(COMMAND_SYNTAX));
			
			System.exit(-1);
		}catch(Exception ex)
		{
			ex.printStackTrace();

			System.err.println("Error: " + ex.getMessage());
			System.err.println(commandLineOptions.fetchHelpInfo(COMMAND_SYNTAX));
			
			System.exit(-1);
		}
		
		return basicArguments;
	}
	
	/**
	 * Loads version mapping as properties.
	 * @return
	 */
	private static Properties loadMappingProperties()
	{
		try
		{
			Properties prop = new Properties();
			InputStream is = Main.class.getResourceAsStream("/version-mapping.properties");
			prop.load(is);
			is.close();
			
			return prop;
		}catch(Exception ex)
		{
			System.err.println("An error occurred while loading version-mapping properties. Error: " + ex);
			System.exit(-1);
		}
		
		return null;
	}
	
	private static boolean executeChangeLog(String verClsName, String verFilePath, PapilioArguments args, IErrorListener errorListener)
	{
		IDbSchemaVersioner dbSchemaVersioner = null;
		
		try
		{
			Class<?> cls = Class.forName(verClsName);
			dbSchemaVersioner = (IDbSchemaVersioner) cls.newInstance();
		}catch(Exception ex)
		{
			System.err.println("An error occurred while creating db-schema-version instance: " + verClsName + "\nError: " + ex);
			System.exit(-1);
		}
		
		File changeFile = new File(verFilePath);
		
		if(!changeFile.exists())
		{
			System.err.println("Specified db-change-log file does not exist: " + verFilePath);
			System.exit(-1);
		}
		
		DatabaseChangeLog databaseChangeLog = null;
		
		try
		{
			databaseChangeLog = DatabaseChangeLogFactory.load(changeFile);
		}catch(Exception ex)
		{
			System.err.println("An error occurred while loading change file: " + changeFile + "\nError: " + ex);
			System.exit(-1);
		}
		
		DbChangeLogExecutor dbChangeLogExecutor = new DbChangeLogExecutor(databaseChangeLog, dbSchemaVersioner, args, errorListener);
		boolean res = dbChangeLogExecutor.execute();
		
		return res;
	}
	
	private static IErrorListener buildErrorListener(String listenerName)
	{
		if(listenerName == null)
		{
			return new DummyErrorListener();
		}
		
		try
		{
			Class<?> cls = Class.forName(listenerName);
			return (IErrorListener) cls.newInstance();
		}catch(Exception ex)
		{
			throw new IllegalStateException("Failed to create error listener of type: " + listenerName, ex);
		}
	}
	
	public static int execute(String[] args)
	{
		PapilioArguments argumentBean = loadArguments(args);
		Properties mappingProp = loadMappingProperties();
		
		String dbType = argumentBean.getDbType();
		String versionCls = mappingProp.getProperty(dbType);
		
		if(versionCls == null)
		{
			System.err.println("Invalid db type specified: " + dbType);
			return -1;
		}
		
		IErrorListener errorListener = buildErrorListener(argumentBean.getErrorListener());
		
		boolean res = executeChangeLog(versionCls, argumentBean.getChangeLogFile(), argumentBean, errorListener);
		return res ? 0 : -1;
	}

	public static void main(String[] args)
	{
		int res = execute(args);
		System.exit(res);
	}
}
