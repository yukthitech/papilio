package com.yukthitech.papilio.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukthitech.ccg.xml.DynamicBean;
import com.yukthitech.ccg.xml.DynamicBeanParserHandler;
import com.yukthitech.ccg.xml.XMLBeanParser;
import com.yukthitech.utils.exceptions.InvalidArgumentException;
import com.yukthitech.utils.exceptions.InvalidStateException;

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
}
