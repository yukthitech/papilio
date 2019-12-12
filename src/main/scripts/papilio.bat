@echo off

REM Below are the command line options
REM 	--host host name
REM 	--port port
REM 	--database database name
REM 	--username username
REM 	--password password
REM 	--changelog master change log file to be executed

set PAPILIO_HOME=%~dp0

echo "Using Papilio Home: " %PAPILIO_HOME%

java -Xms32m -Xmx512m -Dlog4j.configurationFile="file://%PAPILIO_HOME%/log4j2-main.xml" -cp "%PAPILIO_HOME%\lib\*;" com.yukthitech.papilio.Main --dbtype mongo %*

