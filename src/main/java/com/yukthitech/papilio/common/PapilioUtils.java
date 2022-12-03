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
package com.yukthitech.papilio.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukthitech.ccg.xml.DynamicBean;
import com.yukthitech.ccg.xml.DynamicBeanParserHandler;
import com.yukthitech.ccg.xml.XMLBeanParser;
import com.yukthitech.jexpr.JsonExprEngine;
import com.yukthitech.papilio.mongo.MongoDbMethods;
import com.yukthitech.utils.exceptions.InvalidArgumentException;
import com.yukthitech.utils.exceptions.InvalidStateException;
import com.yukthitech.utils.fmarker.FreeMarkerEngine;

/**
 * Json related utils.
 * 
 * @author akiran
 */
public class PapilioUtils
{
	/**
	 * Object mapper.
	 */
	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Used to parse query templates.
	 */
	private static FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
	
	/**
	 * For jel template processing.
	 */
	private static JsonExprEngine jsonExprEngine = new JsonExprEngine();

	static
	{
		freeMarkerEngine.loadClass(MongoDbMethods.class);
		jsonExprEngine.setFreeMarkerEngine(freeMarkerEngine);
	}

	public static Object parseJson(String json)
	{
		try
		{
			return objectMapper.readValue(json, Object.class);
		} catch(Exception ex)
		{
			throw new InvalidArgumentException("Failed to parse json: {}", json, ex);
		}
	}

	public static String toJson(Object object)
	{
		try
		{
			return objectMapper.writeValueAsString(object);
		} catch(Exception ex)
		{
			throw new InvalidArgumentException("An error occurred while converting object to json. Object: {}", object, ex);
		}
	}
	
	public static Map<String, Object> loadXml(File fileObj)
	{
		if(!fileObj.exists())
		{
			throw new InvalidArgumentException("Invalid/non-existing file specified: {}", fileObj.getPath());
		}
		
		try
		{
			FileInputStream fis = new FileInputStream(fileObj);
			
			DynamicBeanParserHandler handler = new DynamicBeanParserHandler();
			handler.setTypeConversationEnabled(true);
			handler.setExpressionEnabled(false);
			
			DynamicBean dynBean = (DynamicBean) XMLBeanParser.parse(fis, handler);
			Map<String, Object> value = (Map<String, Object>) dynBean.toSimpleMap();
			
			fis.close();
			
			return value;
		}catch(Exception ex)
		{
			throw new InvalidStateException("Failed to load xml content from file: {}", fileObj.getPath(), ex);
		}
	}
	
	public static String processTemplate(String name, String template, Object context)
	{
		return freeMarkerEngine.processTemplate(template, template, context);
	}
	
	public static String processJelTemplate(String jsonTemplate, Map<String, Object> context)
	{
		return jsonExprEngine.processJson(jsonTemplate, context);
	}
}
