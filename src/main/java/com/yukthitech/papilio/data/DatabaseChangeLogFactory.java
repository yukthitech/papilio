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
