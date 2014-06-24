The Aerospike Developer's Toolkit provides the developer with facilities that aid in developing applications that use Aerospike.

These are:
* New Aerospike project wizard
* Cluster Explorer
* User Defined Function (UDF) registration
* Aerospike Query Language (AQL)
	* AQL specific editor
	* Query execution
	* Code generation
	

# Installation
The update site for the this plugin is located at: https://github.com/aerospike/eclipse-tools/raw/master/aerospike-site 

Follow these instructions to [Add Update Site](http://help.eclipse.org/kepler/index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm) to you Eclipse environment.

# New Aerospike Project wizard

The new project wizard will generate a new Java project with a Maven nature and an Aerospike nature. Your Eclipse environment will need to have the Eclipse **Java development Toolkit (JDT)** and the **Maven (m2e)** plugins installed prior to running this wizard.

To generate a new Aerospike Java project

Select New Project

<img src="assets/eclipse_new_project.png" alt="New -> Project" width="50%" height="50%"/>

The new project dislog will be displayed

<img src="assets/eclipse_new_project_dialog_aerospike.png" alt="New Aerospike Project" width="50%" height="50%"/>

then Expand the Aerospike category, and select `New Aerospike Project`

<img src="assets/eclipse_new_project_dialog_aerospike.png" alt="New Aerospike Project" width="50%" height="50%"/>


The New Aerospike project wizard will start and display the Aerospike properties page

<img src="assets/eclipse_new_project_aerospike_properties.png" alt="Enter the Aerospike properties" width="50%" height="50%"/>

* **Project Name** - The name of the Eclipse project and the Maven project name.
* **Artifact ID** - Tne Maven artifact ID
* **Version** - The Maven version
* **Main Class* - The name of the Java main class
* **Author** - The project author in the Maven POM.
* **email** - Email address of the author in the Maven POM
* **Seed Node** - A node address in the Aerospike cluster. This will be stored in the projects persistent properties and used for connections to the Aerospike cluster
* **Port** - The port used by the seed node 

Enter the properties and click `Finish`

After the project has been generated, Right click on the project in the Explorer and update the Maven project. This will download the required Maven dependencies and rebuild the project.
 
<img src="assets/eclipse_update_maven.png" alt="Enter the Aerospike properties" width="50%" height="50%"/>
  
# Cluster Explorer

The cluster connection details, of seed node and port, are stored in persistent properties attached to the project. 

<img src="assets/eclipse_aerospike_properties.png" alt="Figure 1" width="50%" height="50%"/>
 
* **Seed Node** - A node address in the Aerospike cluster. This will be stored in the projects persistent properties and used for connections to the Aerospike cluster
* **Port** - The port used by the seed node 
* **UDF Directory** - The directory where the User Defined Function are located. This directory is relative to the project root. The local client will look for UDFs here.
* **Generation Directory** - The directory where the source code will be generated from AQL.


The cluster explorer adds Aerospike specific elements to the Explorer tree:

<img src="assets/eclipse_cluster_explorer.png" alt="Figure 2" width="50%" height="50%"/>


**Note:**
These extensions are not visible in the Java Package Explorer (JDT limitation)



# User Defined Functions (UDFs)

User Defined Function need to be registered with the cluster before they are available for use. During development, you may need to frequently register UDF packages with your development cluster as you make additions and modifications.

To do this, simply right-click, in the Explorer, on the Lua file containing the UDF package. Select the popup menu `Register UDF`

<img src="assets/eclipse_register_udf.png" alt="Figure 3" width="50%" height="50%"/>

The UDF package will be registered with the cluster configured in the `Properties` page.


# Aerospike Query Language
Aerospike Query Language (aql) is an SQL-like language that is specific to Aerospike, it is easy to learn because of its similarity to SQL 

## AQL Editor
The AQL editor provides color syntax highlighting of the language elements, plus error checking when the AQL file is saved.

## Query Execution
An AQL file can be directly executed on the cluster configured.
Right-click on the aql file and select `Execute AQL`. The output from the cluster will be displayed in the console view.

<img src="assets/eclipse_aql_menu.png" alt="Figure 4" width="50%" height="50%"/>


## Code Generation
You can translate the AQL statements int the semantic equivalent Java code. To generate a Java class, Right-click on `Generate Java`. A new class, with the same name as the AQL file, will be generated and stored in the `Generation` folder. The location of this folder is configured in the Aerospike properties.

This AQL code: 

<img src="assets/eclipse_aql_editor.png" alt="Figure 5" width="50%" height="50%"/>

Will generate this Java code:

<img src="assets/eclipse_exported_java.png" alt="iFigure 6" width="50%" height="50%"/>


The class is immediately runnable, and it can be a start to build on.

# Source Code

The source code is available on GitHub at: https://github.com/aerospike/eclipse-tools

```bash
git clone https://github.com/aerospike/eclipse-tools.git
```
##Build Instructions
Run the ANT script to build the Aerospike Feature.

```
ant -f build_feature.xml
``` 
the output will be in the `target` directory.


