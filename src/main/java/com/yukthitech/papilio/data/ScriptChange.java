package com.yukthitech.papilio.data;

/**
 * Change which would execute specified script.
 * @author akiran
 */
public class ScriptChange implements IChange
{
	/**
	 * Script to execute.
	 */
	private String script;
	
	public ScriptChange(String script)
	{
		this.script = script;
	}

	/**
	 * Gets the script to execute.
	 *
	 * @return the script to execute
	 */
	public String getScript()
	{
		return script;
	}

	/**
	 * Sets the script to execute.
	 *
	 * @param script
	 *            the new script to execute
	 */
	public void setScript(String script)
	{
		this.script = script;
	}
}
