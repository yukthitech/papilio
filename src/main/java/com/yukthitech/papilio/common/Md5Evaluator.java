package com.yukthitech.papilio.common;

import java.math.BigInteger;
import java.security.MessageDigest;

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
			String json = JsonUtils.toJson(object);

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
}
