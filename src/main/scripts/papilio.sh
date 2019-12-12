@echo off

#### Below are the command line options
#### 	--host host name
#### 	--port port
#### 	--database database name
#### 	--username username
#### 	--password password
#### 	--changelog master change log file to be executed

export PAPILIO_HOME=%~dp0

echo "Using Papilio Home: " ${PAPILIO_HOME}

java -Xms32m -Xmx512m -Dlog4j.configurationFile="file://${PAPILIO_HOME}/log4j2-main.xml" -cp "${PAPILIO_HOME}/lib/*:" com.yukthitech.papilio.Main --dbtype mongo %*

