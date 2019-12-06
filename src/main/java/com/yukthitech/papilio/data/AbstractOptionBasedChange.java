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
