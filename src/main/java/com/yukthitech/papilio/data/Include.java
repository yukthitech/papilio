package com.yukthitech.papilio.data;

/**
 * Used to include other file in the main change log.
 * @author akiran
 */
public class Include
{
	/**
	 * Path of the file to include.
	 */
	private String path;

	/**
	 * Gets the path of the file to include.
	 *
	 * @return the path of the file to include
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Sets the path of the file to include.
	 *
	 * @param path the new path of the file to include
	 */
	public void setPath(String path)
	{
		this.path = path;
	}
}
