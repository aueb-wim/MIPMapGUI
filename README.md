### MIPMap
Data integration and data exchange tool

### Dependencies
-   Java 1.8
-   PostgreSQL 9.5

### Project
In order for MIPMap to be able to execute mappings for all dataset sizes without memory issues, it uses an auxiliary database to store intermediate tables. Therefore, it need to be configured in the MIPMapGUI/mipmapgui/src/conf/postgresdb.properties file with the URI of a postgres instance, the user credentials and the name this auxiliary database we wish to have.

To learn how to use MIPMap, check MIPMap Tutorial docx file.

### Setup
For installing MIPMap, use the installers attached in the Releases. Check MIPMap Installation pdf file.

### Development
This project consists of NetBeans Modules. For a developer-oriented guide, check MIPMap Report docx file.

Open the project with Netbeans IDE. Upon making changes, build the project and run after_build.sh (a few jar's need to be c-p'd so as MIPMap to import all of its modules).

