package com.yukthitech.papilio.data;

import java.io.File;
import java.io.FileInputStream;

import com.yukthitech.ccg.xml.XMLBeanParser;
import com.yukthitech.utils.exceptions.InvalidStateException;

/**
 * Factory of database change logs.
 * @author akiran
 */
public class DatabaseChangeLogFactory
{
	/**
	 * Loads the database change log from specified file.
	 * @param file
	 * @return
	 */
	public static DatabaseChangeLog load(File file)
	{
		DatabaseChangeLog log = new DatabaseChangeLog(file.getName());

		try
		{
			FileInputStream fis = new FileInputStream(file);
			XMLBeanParser.parse(fis, log);
			fis.close();
		}catch(Exception ex)
		{
			throw new InvalidStateException("An error occurred while loading changelog from file: {}", file.getPath(), ex);
		}
		
		return log;
	}
}
