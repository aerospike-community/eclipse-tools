This Aerospike Toolkit makes it easier for Java developers using Eclipse IDE in incorporating Aerospike functionality into their applications.

The toolkit currently includes:

* Aerospike Java Project wizard
* Cluster Explorer
* User Defined Function (UDF) registration
* Aerospike Query Language (AQL)
	* AQL specific editor
	* Query execution
	* Code generation
	
# Installation

**Eclipse Java EE IDE for Web Developers:**

* Click `Help >> Install New Software...`
* For 'Work with' enter:  https://github.com/aerospike/eclipse-tools/raw/master/aerospike-site
* Check `Aerospike` checkbox
* Click `Next` and complete installation process - this step requires you to restart Eclipse 

**Eclipse Workbench Developers:**

Follow these instructions to [Add Update Site](http://help.eclipse.org/kepler/index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm) to your  environment.

# Aerospike Java Project wizard

Prerequisites:

* Java Development Toolkit (JDT)
* Maven

This wizard will help you generate a starter Java project that uses Aerospike.To generate a new Aerospike Java project:

Click `File >> New >> Project`

<img src="assets/eclipse_new_project.png" alt="New -> Project" width="50%" height="50%"/>

The New Project dialog will be displayed

<img src="assets/eclipse_new_project_dialog_aerospike.png" alt="New Aerospike Project" width="50%" height="50%"/>

Expand `Aerospike` category, select `New Aerospike Project` and click Next

<img src="assets/eclipse_new_project_dialog_aerospike.png" alt="New Aerospike Project" width="50%" height="50%"/>

The New Aerospike project wizard will be displayed with Aerospike properties to be filled

<img src="assets/eclipse_new_project_aerospike_properties.png" alt="Enter the Aerospike properties" width="50%" height="50%"/>

* **Project Name** - Name of your Eclipse project -- this will also be set as the Maven project name
* **Artifact ID** - Maven artifact ID
* **Version** - Maven version
* **Main Class** - Name of the main Java class
* **Author** - Project author in the Maven POM
* **email** - Email address of the author in the Maven POM
* **Seed Node** - A node address in the Aerospike cluster. This will be stored in the projects persistent properties and used for connections to the Aerospike cluster
* **Port** - The port used by the seed node 

Click `Finish`

After the project has been generated, right-click on the project in the Explorer and update the Maven project. This will download the required Maven dependencies and rebuild the project.

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



