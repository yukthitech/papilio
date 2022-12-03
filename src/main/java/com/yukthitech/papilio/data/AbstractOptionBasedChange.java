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

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for specifying custom options for a change.
 */
public abstract class AbstractOptionBasedChange
{
	/**
	 * Helps in specifying custom options for a change.
	 */
	private Map<String, Object> options;

	/**
	 * Adds specified option to this change.
	 * @param name name of option
	 * @param value value of option.
	 */
	private void addOption(String name, Object value)
	{
		if(this.options == null)
		{
			this.options = new HashMap<>();
		}
		
		this.options.put(name, value);
	}
	
	/**
	 * Adds specified String option to this change.
	 * @param name name of option
	 * @param value value of option.
	 */
	public void addStringOption(String name, String value)
	{
		this.addOption(name, value);
	}
	
	/**
	 * Adds specified int option to this change.
	 * @param name name of option
	 * @param value value of option.
	 */
	public void addIntOption(String name, int value)
	{
		this.addOption(name, value);
	}
	
	/**
	 * Adds the long option.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void addLongOption(String name, long value)
	{
		this.addOption(name, value);
	}

	/**
	 * Adds specified boolean option to this change.
	 * @param name name of option
	 * @param value value of option.
	 */
	public void addBooleanOption(String name, boolean value)
	{
		this.addOption(name, value);
	}

	/**
	 * Gets the helps in specifying custom options for a change.
	 *
	 * @return the helps in specifying custom options for a change
	 */
	public Map<String, Object> getOptions()
	{
		return options;
	}

	/**
	 * Sets the helps in specifying custom options for a change.
	 *
	 * @param options the new helps in specifying custom options for a change
	 */
	public void setOptions(Map<String, Object> options)
	{
		this.options = options;
	}
}
