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

import com.yukthitech.utils.cli.CliArgument;

/**
 * Command line arguments bean expected from command line.
 * 
 * @author akiran
 */
public class PapilioArguments
{
	/**
	 * Db type. Default: mongo.
	 */
	@CliArgument(name = "dt", longName = "dbtype", description = "Db type. Default: mongo", required = false)
	private String dbType = "mongo";

	/**
	 * Db server host.
	 */
	@CliArgument(name = "h", longName = "host", description = "Db server host", required = false)
	private String host;

	/**
	 * Db server port.
	 */
	@CliArgument(name = "p", longName = "port", description = "Db server port", required = false)
	private Integer port;

	/**
	 * Db replicas to be used.
	 */
	@CliArgument(name = "r", longName = "replicas", description = "Db replicas", required = false)
	private String replicas;

	/**
	 * Default database to be used.
	 */
	@CliArgument(name = "db", longName = "database", description = "Default database to be used", required = false)
	private String dbname;

	/**
	 * Username to be used for authentication.
	 */
	@CliArgument(name = "un", longName = "username", description = "Username to be used for authentication", required = false)
	private String userName;

	/**
	 * Password to be used for authentication.
	 */
	@CliArgument(name = "pwd", longName = "password", description = "Password to be used for authentication", required = false)
	private String password;

	/**
	 * If true, then it will indicate the db connectivity should happen via SSL. Default: false.
	 */
	@CliArgument(name = "ssl", longName = "enable-ssl", 
			description = "If true, then it will indicate the db connectivity should happen via SSL. Default: false", required = false)
	private boolean enableSsl = false;

	/**
	 * Change log file to be processed.
	 */
	@CliArgument(name = "cl", longName = "changelog", description = "Change log file to be processed.", required = true)
	private String changeLogFile;

	/**
	 * Gets the db server host.
	 *
	 * @return the db server host
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Sets the db server host.
	 *
	 * @param host
	 *            the new db server host
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * Gets the db server port.
	 *
	 * @return the db server port
	 */
	public Integer getPort()
	{
		return port;
	}

	/**
	 * Sets the db server port.
	 *
	 * @param port
	 *            the new db server port
	 */
	public void setPort(Integer port)
	{
		this.port = port;
	}

	/**
	 * Gets the default database to be used.
	 *
	 * @return the default database to be used
	 */
	public String getDbname()
	{
		return dbname;
	}

	/**
	 * Sets the default database to be used.
	 *
	 * @param dbname
	 *            the new default database to be used
	 */
	public void setDbname(String dbname)
	{
		this.dbname = dbname;
	}

	/**
	 * Gets the username to be used for authentication.
	 *
	 * @return the username to be used for authentication
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * Sets the username to be used for authentication.
	 *
	 * @param userName
	 *            the new username to be used for authentication
	 */
	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	/**
	 * Gets the password to be used for authentication.
	 *
	 * @return the password to be used for authentication
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * Sets the password to be used for authentication.
	 *
	 * @param password
	 *            the new password to be used for authentication
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * Gets the db type. Default: mongo.
	 *
	 * @return the db type
	 */
	public String getDbType()
	{
		return dbType;
	}

	/**
	 * Sets the db type. Default: mongo.
	 *
	 * @param dbType
	 *            the new db type
	 */
	public void setDbType(String dbType)
	{
		this.dbType = dbType;
	}

	/**
	 * Gets the change log file to be processed.
	 *
	 * @return the change log file to be processed
	 */
	public String getChangeLogFile()
	{
		return changeLogFile;
	}

	/**
	 * Sets the change log file to be processed.
	 *
	 * @param changeLogFile
	 *            the new change log file to be processed
	 */
	public void setChangeLogFile(String changeLogFile)
	{
		this.changeLogFile = changeLogFile;
	}

	/**
	 * Gets the db replicas to be used.
	 *
	 * @return the db replicas to be used
	 */
	public String getReplicas()
	{
		return replicas;
	}

	/**
	 * Sets the db replicas to be used.
	 *
	 * @param replicas the new db replicas to be used
	 */
	public void setReplicas(String replicas)
	{
		this.replicas = replicas;
	}

	/**
	 * Checks if is if true, then it will indicate the db connectivity should
	 * happen via SSL. Default: false.
	 *
	 * @return the if true, then it will indicate the db connectivity should
	 *         happen via SSL
	 */
	public boolean isEnableSsl()
	{
		return enableSsl;
	}

	/**
	 * Sets the if true, then it will indicate the db connectivity should happen
	 * via SSL. Default: false.
	 *
	 * @param enableSsl
	 *            the new if true, then it will indicate the db connectivity
	 *            should happen via SSL
	 */
	public void setEnableSsl(boolean enableSsl)
	{
		this.enableSsl = enableSsl;
	}
}
