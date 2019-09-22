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
