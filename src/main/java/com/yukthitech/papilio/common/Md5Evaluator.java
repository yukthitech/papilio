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
package com.yukthitech.papilio.common;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.yukthitech.utils.exceptions.InvalidStateException;

/**
 * Utility class to calculate md5 checksum.
 * 
 * @author akiran
 */
public class Md5Evaluator
{
	public static String evaluateChecksum(Object object)
	{
		try
		{
			String json = PapilioUtils.toJson(object);
			json = replaceWhitespaces(json);

			// Static getInstance method is called with hashing MD5
			MessageDigest md = MessageDigest.getInstance("MD5");

			// digest() method is called to calculate message digest
			// of an input digest() return array of byte
			byte[] messageDigest = md.digest(json.getBytes());

			// Convert byte array into signum representation
			BigInteger no = new BigInteger(1, messageDigest);

			// Convert message digest into hex value
			String hashtext = no.toString(16);

			while(hashtext.length() < 32)
			{
				hashtext = "0" + hashtext;
			}

			return hashtext;
		} catch(Exception ex)
		{
			throw new InvalidStateException("An error occurred while evaluating MD5 checking of object: {}", object, ex);
		}
	}
	
	/**
	 * Converts the json into object. And replaces whitespaces in object
	 * recursively.
	 * @param json
	 * @return
	 */
	private static String replaceWhitespaces(String json)
	{
		Object jsonObj = PapilioUtils.parseJson(json);
		Object converted = replaceWhitespacesOfObject(jsonObj);
		
		return PapilioUtils.toJson(converted);
	}
	
	/**
	 * Replaces whitespaces in the object recursively.
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Object replaceWhitespacesOfObject(Object obj)
	{
		if(obj instanceof Map)
		{
			Map<Object, Object> newMap = new LinkedHashMap<Object, Object>();
			Map<Object, Object> curMap = (Map<Object, Object>) obj;
			
			curMap.forEach((key, val) -> 
			{
				newMap.put(key, replaceWhitespacesOfObject(val));
			});
			
			return newMap;
		}
		
		if(obj instanceof List)
		{
			List<Object> newLst = new ArrayList<Object>();
			List<Object> curLst = (List<Object>) obj;
			
			curLst.forEach(val -> 
			{
				newLst.add(replaceWhitespacesOfObject(val));
			});
			
			return newLst;
		}
		
		if(obj instanceof String)
		{
			String str = (String) obj;
			return str.replaceAll("\\s+", " ");
		}
		
		return obj;
	}
}
