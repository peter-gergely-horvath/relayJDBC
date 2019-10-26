# Relay JDBC - Access JDBC data sources through an intermediate proxy server

## Overview

Relay JDBC is a custom JDBC driver implementation that allows a target 
database to be accessed through various network transports different 
from that supported by the JDBC driver of the target database.

Instead of connecting to the database directly, using the JDBC driver 
of the target database, a client application uses the Relay JDBC 
driver to connect to the Relay JDBC server, which internally connects 
to the target database using the JDBC driver of the target database
and relays all communication between the client application and
the target database.

Both the client JDBC driver and the relay server are packaged into 
one JAR file allowing a very quick setup.

```
+--------------------+
| Client Application |
+--------------------+
          |      
+--------------------+
| Relay JDBC driver  |
+--------------------+
          |      
          |      
          |      
+--------------------+
| Relay JDBC Server  |
+--------------------+
          |      
+------------------------------+
| Target Database JDBC driver  |
+------------------------------+
          |      
+------------------------------+
|  Target Database Server      |
+------------------------------+
```

## Running the server

Start the JAR file from command line and specify transport and its configuration. 

1.  Create a configuration file similar to the following and save it to 
    `relayjdbc-config.xml` in the current working directory
```
<relayjdbc-configuration>
	<connection
		id="h2db"
		driver="org.h2.Driver"
		url="jdbc:h2:mem:test"
		user="sa"
       	password="">
    	<connection-pool
                   maxActive="4"
                   maxIdle="1"
                   maxWait="10000"
                   timeBetweenEvictionRunsMillis="5000"
                   minEvictableIdleTimeMillis="10000"/>
	</connection>
</relayjdbc-configuration>
```
2.  Start the JAR file as a single JAR application, specifying the transport used and its configuration. 
    For example, the following will start the HTTP transport with HTTP listening on port 8080 
    and HTTPS on port 8443: `java -jar relay-jdbc-*.jar http 8080`
    
3.  Add the RelayJDBC JAR and the actual delegate driver JARs to the client application and 
    use RelayJDBC connection URL to connect to the relay. For example:
    `jdbc:relayjdbc:http://localhost:8080#h2db`
    
NOTE: if there are more than one connections defined in the connection file, the URL should
reference the `id` attribute of the connection (here, in the sample: `id="h2db"`) at the end
of the connection URL (here, in the sample URL: `...#h2db`).        

## Transports and their configuration

### HTTP Transport 

This transport starts an embedded Servlet container that will listen to incoming HTTP requests.

#### Starting the relay server 

Start command: `http <http port>`

 * Command line argument: `http`
 * Configuration arguments
    * `http port`: the port to listen on for incoming HTTP requests (required)  
    * `https port`: the port to listen on for incoming HTTPs requests (optional)

Examples: 
1. `java -jar relay-jdbc-*.jar http 8080`
This will start the HTTP transport with HTTP listening on port 8080 and **NO** HTTP listener 
    

#### JDBC URL to connect with

For the HTTP transport, the following URL format is to be used:
 `jdbc:relayjdbc:http://<host name>:<port number>#<connection ID>`

Where the placeholders are:
 * `host name`: the host to connect to (required)  
 * `port number`: the port to connect to (required)
 * `connection ID`: the value of the ID attribute of the connection tag (optional)
       
 Sample URL: `jdbc:relayjdbc:http://localhost:8080#h2db`

#### Limitations of HTTP Transport 

At the moment, HTTP transport does not have any authentication or encryption built-in.
The Relay JDBC Server will simply pass authentication to the database server.
You will have to ensure that only authorized users can connect to the service by
implementing firewall settings.

### BASE64PIPE / SSHPIPE Transport 

BASE64PIPE transport starts a listener, that awaits incoming messages from the standard input
and responds to the standard output. All protocol messages are Base64 encoded, allowing
this transport to be used through text-only piping mechanisms. 

While theoretically, a number of client transports could use this connection type,
in practice, only the `sshpipe` client connection type is used: that is, the 
Relay JDBC driver establishes a SSH connection to a remote system, starts 
a Relay JDBC Server with BASE64PIPE transport then connects to the server through 
standard I/O pipes. 
      

#### Starting the relay server 

Start command: `base64pipe <configuration file> [properties file]`

 * Command line argument: `base64pipe`
 * Configuration arguments
    * `configuration file`: the RelayJDBC configuration file (required)  
    * `properties file`: the RelayJDBC properties file (optional)

Examples: 
1. `java -jar relay-jdbc-*.jar base64pipe config.xml`
This will start the BASE64PIPE transport using the `config.xml` configuration file
    
2. `java -jar relay-jdbc-*.jar base64pipe config.xml settings.properties`
This will start the BASE64PIPE transport using the `config.xml` configuration file and `settings.properties` file

#### JDBC URL to connect with

For the BASE64PIPE transport, the following URL format is to be used:
 `jdbc:relayjdbc:sshpipe://<host name>:<port number>`

Where the placeholders are:
    * `host name`: the host to connect to (required)  
    * `port number`: the port to connect to (required)
    
 Sample URL: `jdbc:relayjdbc:http://localhost:2222`

##### Mandatory properties to be passed 

The following properties must be passed to the connection establish call
(e.g. `java.sql.DriverManager.getConnection(java.lang.String, java.util.Properties)`)
when using this connection type:
 * `ssh.username`: the username for the SSH authentication
 * `ssh.password`: the password for the SSH authentication
 * `ssh.remoteCommand`: the remote command to execute, which will start the BASE64PIPE transport server
    
NOTE: `ssh.remoteCommand` is the actual OS command that starts the Relay JDBC Server on the remote machine.
You probably want create a shell script to perform these steps. Remember: if you use this transport mechanism,
you will have to disable logging to the console as that would conflict with the messages emitted to the standard
output pipe. (Otherwise the client will attempt -- and fail to -- un-marshall the console log messages emitted 
by the remote server)

   
##### Optional properties to be passed

The following properties can be passed to the connection establish call
(e.g. `java.sql.DriverManager.getConnection(java.lang.String, java.util.Properties)`)
when using this connection type:
    
 * `ssh.strictHostKeyChecking`: enforce strict host key matching: `yes`/`no` - default is `no`


#### Limitations of BASE64PIPE / SSHPIPE Transport 

##### Special log configuration requirement for BASE64PIPE

IMPORTANT NOTICE: Transport `BASE64PIPE` works by piping protocol message data through 
the standard input/output. If `BASE64PIPE` is used, console logging has to be disabled 
(e.g. redirect all logs to a file) 

##### Single connection configuration per file

Unlike other transports, BASE64PIPE / SSHPIPE Transport does not support having multiple connection
configurations in one configuration file. You have to use the the default connection type and cannot
specify a connection Id in the Relay JDBC driver JDBC URL.  

## Log configuration 

RelayJDBC uses log4j for logging and expects a log4j configuration file to be present in the current working
directory called either `relay-jdbc.log4j.xml` (XML format) or `relay-jdbc.log4j.properties` (properties format).

Sample `relay-jdbc.log4j.properties`
```
# Root logger option
log4j.rootLogger=WARN, file

# Direct log messages to a log file
log4j.appender.file=org.apache.log4j.RollingFileAppender

log4j.appender.file.File=relay-jdbc.log
log4j.appender.file.MaxFileSize=2MB
log4j.appender.file.MaxBackupIndex=2
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
```

IMPORTANT NOTICE: Transport `BASE64PIPE` works by piping protocol message data through 
the standard input/output. If `BASE64PIPE` is used, console logging have to be disabled 
(e.g. redirect all logs to a file) 


# Download

Relay JDBC is available in Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/com.github.peter-gergely-horvath/relay-jdbc.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.peter-gergely-horvath%22%20AND%20a:%22relay-jdbc%22)
