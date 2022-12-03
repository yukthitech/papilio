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
