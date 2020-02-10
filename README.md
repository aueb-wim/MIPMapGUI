### MIPMap
Data integration and data exchange tool

### Prerequisites
-   Oracle Java 1.8 (does not work well with OpenJDK)
-   PostgreSQL 9.5

### Project
In order for MIPMap to be able to execute mappings for all dataset sizes without memory issues, it uses an auxiliary database to store intermediate tables. Therefore, it need to be configured in the MIPMapGUI/mipmapgui/src/conf/postgresdb.properties file with the URI of a postgres instance, the user credentials and the name this auxiliary database we wish to have.

This project consists of NetBeans Modules. Open the project with Netbeans IDE and run the "mipmapgui" module.

For installing MIPMap, use the installers attached in the Releases.
