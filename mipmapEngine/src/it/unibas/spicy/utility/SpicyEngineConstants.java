/*
    Copyright (C) 2007-2011  Database Group - Universita' della Basilicata
    Giansalvatore Mecca - giansalvatore.mecca@unibas.it
    Salvatore Raunich - salrau@gmail.com

    This file is part of ++Spicy - a Schema Mapping and Data Exchange Tool
    
    ++Spicy is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    ++Spicy is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ++Spicy.  If not, see <http://www.gnu.org/licenses/>.
 */
 
package it.unibas.spicy.utility;


public class SpicyEngineConstants {
    

    public static final int LINES_BASED_MAPPING_TASK = 0;
    public static final int TGD_BASED_MAPPING_TASK = 1;

    public static final String INDENT = "     ";

    public static final double TRUE = 1.0;
    public static final double FALSE = 0.0;

    public static final String TYPE_MOCK = "mock";
    public static final String TYPE_META_INSTANCE = "meta instance";
    public static final String TYPE_ALGEBRA_RESULT = "algebra result";
    public static final String TYPE_XML = "XML";
    public static final String TYPE_CSV = "CSV";
    public static final String TYPE_SQL = "SQL";
    public static final String TYPE_RELATIONAL = "Relational";
    
    public static final String PROVIDER_TYPE_CHAINING = "CHAINING";
    public static final String PROVIDER_TYPE_CONSTANT = "CONSTANT";
    public static final String PROVIDER_TYPE_MERGE = "MERGE";
    
    public final static String SOURCE_SCHEMA_NAME = "source";
    public final static String TARGET_SCHEMA_NAME = "target";
    public final static String WORK_SCHEMA_NAME = "work";

    public static final String DATASOURCE_ROOT_LABEL = "dataSource";
    public static final String MERGE_ROOT_LABEL = "merge";
    public static final String SEPARATOR = "_";

    public static final String XML_SCHEMA_FILE = "XSD file";
    public static final String XML_INSTANCE_FILE_LIST = "XML instance file list";

    public static final String CSV_DB_NAME = "CSV database name";    
    public static final String CSV_TABLE_FILE_LIST = "CSV table file list";
    public static final String CSV_INSTANCES_INFO_LIST ="CSV instance file list";
    public static final String INSTANCE_PATH_LIST = "CSV instance table file list";
    
    public static final String SQL_DB_NAME = "SQL database name";
    public static final String SQL_FILE_PATH = "SQL file";
    
    public static final String ACCESS_CONFIGURATION = "access configuration";
    public static final String LOADED_INSTANCES_FLAG = "loaded instances";
    public static final String TYPE_OBJECT = "object";
    public static final String CLASSPATH_FOLDER = "classpath folder";
    public static final String OBJECT_MODEL_FACTORY = "object model factory";
    public static final String SOURCE_INSTANCE_POSITION = "source instance position";

    public static final String SOURCEVALUE_DATE_FUNCTION = "date()";
    public static final String POSTGRES_DATE_FUNCTION = "current_date";
    public static final String SOURCEVALUE_DATETIME_FUNCTION = "datetime()";
    public static final String POSTGRES_CURRENT_YEAR_FUNCTION = "extract(year from current_date)";
    public static final String POSTGRES_DATETIME_FUNCTION = "date_trunc('second',localtimestamp)";
    public static final String SOURCEVALUE_NEWID_FUNCTION = "newId()";
    public static final String POSTGRES_NEWID_FUNCTION = "nextval('idsequence')";
    public static final String POSTGRES_CURRENTID_FUNCTION = "currval('idsequence')";

    public static final String MORE_COMPACT = "more compact";
    public static final String MORE_INFORMATIVE = "more informative";

    public static final String PREMISE = "PREMISE";
    public static final String COVERAGE = "COVERAGE";
    public static final String SOURCE_REWRITING = "SOURCE REWRITING";
    public static final String REW_C = "REW_C";
    public static final String REW_I = "REW_I";
    public static final String SUBSUMES = " subsumes ";

    public static final String ALGEBRA_SEPARATOR = "§#§";
    
    //giannisk
    public static final String COMMA_REPLACEMENT = "#COMMA_CHAR#";
    public static final String DOUBLE_QUOTES = "#DOUBLE_QUOTES#";
    
    public static final String SEMI_COLON_DELIMITER = "Semi-colon";
    public static final String COLON_DELIMITER = "Colon";
    public static final String TAB_DELIMITER = "Tab";
    public static final String DOUBLE_QUOTES_OPTION ="Double quotes";
    public static final String SINGLE_QUOTES_OPTION ="Single quotes";
        
    public static final String LEAF = "LEAF";

    public static final String INCL_COL_NAMES = "true";
    public static final String NOT_INCL_COL_NAMES = "false";
    
    public static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public static final String DERBY_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    
    public static final String MYSQL_DRIVER_NAME = "MySQL Connector Java";
    
    public static String ACCESS_CONFIGURATION_LOGIN;
    public static String ACCESS_CONFIGURATION_PASS;
    public static String ACCESS_CONFIGURATION_DRIVER;
    public static String ACCESS_CONFIGURATION_URI;
    public static String MAPPING_TASK_DB_NAME;
    
    public static final String PRIMARY_KEY_CONSTR_NOTICE = "Primary key constraint violated for table ";
    
    public static void setDatabaseParameters(String driver, String uri, String username, String password, String mapTaskDbName){
        ACCESS_CONFIGURATION_DRIVER = driver;
        ACCESS_CONFIGURATION_URI = uri;
        ACCESS_CONFIGURATION_LOGIN = username;
        ACCESS_CONFIGURATION_PASS = password;
        MAPPING_TASK_DB_NAME = mapTaskDbName;
    }
}
