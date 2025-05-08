# papilio

Papilio is a schema versioning tool (currently targeted for Mongo only). The changes are specified in xml which are executed in sequential order.

Below is the command to apply changes from chagelog to the DB

* **com.yukthitech.papilio.Main --host <db-host> --port <db-port> --database <database> --changelog <path-of-root-file> --dbtype mongo**

As it can be seen from the command the application of changes starts from root file. And from root file generally other version files are referred. Below is an example of an root file:

> **Root change log Example:**
> ```xml
>	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
>	<databaseChangeLog>
>		<include path="basic-working.xml"/>
>		<include path="ext-files/ext-file-working.xml"/>	
>		<include path="script-exec.xml"/>
>	</databaseChangeLog>
> ```



	

