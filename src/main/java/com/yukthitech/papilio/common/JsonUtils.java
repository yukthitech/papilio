package com.yukthitech.papilio.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukthitech.utils.exceptions.InvalidArgumentException;

/**
 * Json related utils.
 * 
 * @author akiran
 */
public class JsonUtils
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
}
