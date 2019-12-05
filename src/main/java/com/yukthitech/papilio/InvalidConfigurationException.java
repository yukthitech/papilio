package com.yukthitech.papilio;

import com.yukthitech.utils.exceptions.UtilsException;

/**
 * Exception to be thrown when invalid configuration is encountered.
 * @author akiran
 */
public class InvalidConfigurationException extends UtilsException
{
	private static final long serialVersionUID = 1L;

	public InvalidConfigurationException(String message, Object... args)
	{
		super(message, args);
	}
}
