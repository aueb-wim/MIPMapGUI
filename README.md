### MIPMap
Data integration and data exchange tool

### Prerequisites
-   Java 1.8
-   PostgreSQL 9.5

### Project
In order for MIPMap to be able to execute mappings for all dataset sizes without memory issues, it uses an auxiliary database to store intermediate tables. Therefore, it need to be configured in the MIPMapGUI/mipmapgui/src/conf/postgresdb.properties file with the URI of a postgres instance, the user credentials and the name this auxiliary database we wish to have.

Open the project with Netbeans. Run the "mipmapgui" module.


