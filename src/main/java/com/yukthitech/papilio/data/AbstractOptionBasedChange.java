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
