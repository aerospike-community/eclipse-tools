#Aerospike Eclipse feature set

This feature set provides the following


 - Connect a project to an Aerospike Cluster
 	- Aerospike Cluster properties on a Project
 - Aerospike Cluster details in the Project Explorer view 
 - Register User Defined Functions (UDFs) directly from the Project Explorer
 - Aerospike Query Language Editor
 	- Color syntax highlighting
 	- Syntax checking
 	- AQL execution on the connected Cluster
 	- Generation of Java (future C#, C) application from AQL
 	
##Installation
Add the following URL to your software update sites

https://github.com/aerospike/eclipse-tools/raw/master/aerospike-site


##Build Instructions
Run the ANT script to build the Aerospike Feature.

```
ant -f build_feature.xml
``` 
the output will be in the `target` directory.


