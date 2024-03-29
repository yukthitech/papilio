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
package com.yukthitech.papilio;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static Logger logger = LogManager.getLogger(Main.class);
	
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
	
	private static boolean executeChangeLog(String verClsName, String verFilePath, PapilioArguments args, ChangeTracker changeTracker)
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
			changeTracker.setTotalCount(databaseChangeLog.getChangeSets().size());
		}catch(Exception ex)
		{
			System.err.println("An error occurred while loading change file: " + changeFile);
			ex.printStackTrace();
			System.exit(-1);
		}
		
		DbChangeLogExecutor dbChangeLogExecutor = new DbChangeLogExecutor(databaseChangeLog, dbSchemaVersioner, args, changeTracker);
		boolean res = dbChangeLogExecutor.execute();
		
		return res;
	}
	
	public static ChangeTracker execute(String[] args)
	{
		PapilioArguments argumentBean = loadArguments(args);
		Properties mappingProp = loadMappingProperties();
		
		String dbType = argumentBean.getDbType();
		
		logger.debug("Using database type: {}", dbType);
		String versionCls = mappingProp.getProperty(dbType);
		
		ChangeTracker changeTracker = new ChangeTracker();
		
		if(versionCls == null)
		{
			System.err.println("Invalid db type specified: " + dbType);
			changeTracker.setExitCode(-1);
			return changeTracker;
		}

		try
		{
			String logo = IOUtils.toString(Main.class.getResourceAsStream("/logo.txt"), Charset.defaultCharset());
			System.out.println(logo);
		}catch(Exception ex)
		{
			//ignore
		}

		boolean res = executeChangeLog(versionCls, argumentBean.getChangeLogFile(), argumentBean, changeTracker);
		changeTracker.setExitCode(res ? 0 : -1);
		return changeTracker;
	}

	public static void main(String[] args)
	{
		ChangeTracker tracker = execute(args);
		System.exit(tracker.getExitCode());
	}
}
