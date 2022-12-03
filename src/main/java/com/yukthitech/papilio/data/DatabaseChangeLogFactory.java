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
package com.yukthitech.papilio.data;

import java.io.File;
import java.io.FileInputStream;
import java.util.Stack;

import com.yukthitech.ccg.xml.DefaultParserHandler;
import com.yukthitech.ccg.xml.XMLBeanParser;
import com.yukthitech.utils.exceptions.InvalidStateException;

/**
 * Factory of database change logs.
 * @author akiran
 */
public class DatabaseChangeLogFactory
{
	private static ThreadLocal<Stack<File>> logFileStack = new ThreadLocal<>();
	
	/**
	 * Adds the specified file to the current thread file stack.
	 * @param file
	 */
	private static void pushFile(File file)
	{
		Stack<File> files = logFileStack.get();
		
		if(files == null)
		{
			files = new Stack<>();
			logFileStack.set(files);
		}
		
		files.push(file);
	}
	
	/**
	 * Removes specified file from current thread file stack.
	 * @param file
	 */
	private static void popFile(File file)
	{
		Stack<File> files = logFileStack.get();
		files.pop();
	}
	
	/**
	 * Fetches the latest file from the current thread file stack.
	 * @return
	 */
	public static File getCurrentFile()
	{
		Stack<File> files = logFileStack.get();
		
		if(files == null || files.isEmpty())
		{
			return null;
		}
		
		return files.peek();
	}
	
	/**
	 * Loads the database change log from specified file.
	 * @param file
	 * @return
	 */
	public static DatabaseChangeLog load(File file)
	{
		DatabaseChangeLog log = new DatabaseChangeLog(file);
		pushFile(file);

		try
		{
			FileInputStream fis = new FileInputStream(file);
			
			DefaultParserHandler defaultParserHandler = new DefaultParserHandler();
			defaultParserHandler.setExpressionEnabled(false);
			
			XMLBeanParser.parse(fis, log, defaultParserHandler);
			fis.close();
		}catch(Exception ex)
		{
			throw new InvalidStateException("An error occurred while loading changelog from file: {}", file.getPath(), ex);
		} finally
		{
			popFile(file);
		}
		
		return log;
	}
}
