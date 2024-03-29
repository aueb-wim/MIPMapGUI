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
package it.unibas.spicy.persistence;

import it.unibas.spicy.utility.SpicyEngineConstants;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.model.correspondence.ConstantValue;
import it.unibas.spicy.model.correspondence.DateFunction;
import it.unibas.spicy.model.correspondence.DatetimeFunction;
import it.unibas.spicy.model.correspondence.GetIdFromDb;
import it.unibas.spicy.model.correspondence.ISourceValue;
import it.unibas.spicy.model.correspondence.NewIdFunction;
import it.unibas.spicy.model.correspondence.ValueCorrespondence;
import it.unibas.spicy.model.datasource.Duplication;
import it.unibas.spicy.model.datasource.FunctionalDependency;
import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicy.model.datasource.JoinCondition;
import it.unibas.spicy.model.datasource.SelectionCondition;
import it.unibas.spicy.model.datasource.operators.MarkNodesToExclude;
import it.unibas.spicy.model.expressions.Expression;
import it.unibas.spicy.model.mapping.proxies.ChainingDataSourceProxy;
import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicy.model.mapping.proxies.MergeDataSourceProxy;
import it.unibas.spicy.model.paths.PathExpression;
import it.unibas.spicy.model.paths.operators.GeneratePathExpression;
import it.unibas.spicy.persistence.csv.DAOCsv;
import it.unibas.spicy.persistence.object.DAOObject;
import it.unibas.spicy.persistence.relational.DAORelational;
import it.unibas.spicy.persistence.relational.DBFragmentDescription;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.persistence.sql.DAOSql;
import it.unibas.spicy.persistence.xml.DAOXmlUtility;
import it.unibas.spicy.persistence.xml.DAOXsd;
import it.unibas.spicy.persistence.xml.operators.TransformFilePaths;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class DAOMappingTaskLines {

    private static Log logger = LogFactory.getLog(DAOMappingTaskLines.class);
    private DAOXmlUtility daoUtility = new DAOXmlUtility();
    private DAORelational daoRelational = new DAORelational();
    private DAOXsd daoXSD = new DAOXsd();
    private DAOCsv daoCSV = new DAOCsv();
    private DAOSql daoSQL = new DAOSql();
    private DAOObject daoObject = new DAOObject();
    private TransformFilePaths filePathTransformator = new TransformFilePaths();
    private GeneratePathExpression pathGenerator = new GeneratePathExpression();

    /*  /////////////////////////////////////////////////////////
     *                        LOAD
     *  ///////////////////////////////////////////////////////// */
    public MappingTask loadMappingTask(int scenarioNo, String filePath, boolean web) throws DAOException {
        try {
            Document document = daoUtility.buildDOM(filePath);
            Element rootElement = document.getRootElement();
            Element sourceElement = rootElement.getChild("source");
            IDataSourceProxy sourceProxy = loadDataSourceProxy(sourceElement, filePath, true, scenarioNo, web);
            if (logger.isDebugEnabled()) logger.debug("Loaded source proxy: " + sourceProxy);
            Element targetElement = rootElement.getChild("target");
            IDataSourceProxy targetProxy = loadDataSourceProxy(targetElement, filePath, false, scenarioNo, web);
            if (logger.isDebugEnabled()) logger.debug("Loaded target proxy: " + targetProxy);
            Element correspondencesElement = rootElement.getChild("correspondences");
            List<ValueCorrespondence> valueCorrespondences = loadValueCorrespondences(correspondencesElement);
            MappingTask mappingTask = new MappingTask(sourceProxy, targetProxy, valueCorrespondences);
            mappingTask.setFileName(filePath);
            mappingTask.setModified(false);
            mappingTask.setToBeSaved(false);
            Element configElement = document.getRootElement().getChild("config");
            loadConfig(mappingTask, configElement);
            return mappingTask;
        } catch (Throwable ex) {
            logger.error(ex);
//            ex.printStackTrace();
            String message = "Unable to load mapping task from file : " + filePath;
            if (ex.getMessage() != null && !ex.getMessage().equals("NULL")) {
                message += "\n" + ex.getMessage();
            }
            throw new DAOException(message);
        }
    }

    private void loadConfig(MappingTask mappingTask, Element configElement) {
        if (configElement == null) {
            return;
        }
        String rewriteSubsumptionsString = configElement.getChildText("rewriteSubsumptions");
        if (rewriteSubsumptionsString != null) {
            boolean value = Boolean.valueOf(rewriteSubsumptionsString);
            mappingTask.getConfig().setRewriteSubsumptions(value);
        }
        String rewriteCoveragesString = configElement.getChildText("rewriteCoverages");
        if (rewriteCoveragesString != null) {
            boolean value = Boolean.valueOf(rewriteCoveragesString);
            mappingTask.getConfig().setRewriteCoverages(value);
        }
        String rewriteSelfJoinsString = configElement.getChildText("rewriteSelfJoins");
        if (rewriteSelfJoinsString != null) {
            boolean value = Boolean.valueOf(rewriteSelfJoinsString);
            mappingTask.getConfig().setRewriteSelfJoins(value);
        }
        String rewriteEGDsString = configElement.getChildText("rewriteEGDs");
        if (rewriteEGDsString != null) {
            boolean value = Boolean.valueOf(rewriteEGDsString);
            mappingTask.getConfig().setRewriteEGDs(value);
        }
        String sortStrategyString = configElement.getChildText("sortStrategy");
        if (sortStrategyString != null) {
            int value = Integer.valueOf(sortStrategyString);
            mappingTask.getConfig().setSortStrategy(value);
        }
        String skolemTableStrategyString = configElement.getChildText("skolemTableStrategy");
        if (skolemTableStrategyString != null) {
            int value = Integer.valueOf(skolemTableStrategyString);
            mappingTask.getConfig().setSkolemTableStrategy(value);
        }
        String useLocalSkolemString = configElement.getChildText("useLocalSkolem");
        if (rewriteEGDsString != null) {
            boolean value = Boolean.valueOf(useLocalSkolemString);
            mappingTask.getConfig().setUseLocalSkolem(value);
        }
    }

    private IDataSourceProxy loadDataSourceProxy(Element dataSourceElement, String mappingTaskFilePath, boolean source, int scenarioNo, boolean web) 
            throws DAOException, SQLException {
        IDataSourceProxy result = null;
        List<String> listOfInclusionPaths = loadInclusionPathString(dataSourceElement.getChild("inclusions"));
        List<String> listOfExclusionPaths = loadExclusionPathString(dataSourceElement.getChild("exclusions"));
        Element typeElement = dataSourceElement.getChild("type");
        if (typeElement.getTextTrim().equalsIgnoreCase(SpicyEngineConstants.TYPE_XML)) {
            result = loadXMLSource(dataSourceElement.getChild("xml"), mappingTaskFilePath);
        } else if (typeElement.getTextTrim().equalsIgnoreCase(SpicyEngineConstants.TYPE_RELATIONAL)) {
            result = loadRelationalSource(dataSourceElement.getChild("relational"), listOfInclusionPaths, listOfExclusionPaths, source, scenarioNo, web);
        //giannisk
        } else if (typeElement.getTextTrim().equalsIgnoreCase(SpicyEngineConstants.TYPE_CSV)) {
            result = loadCSVSource(dataSourceElement.getChild("csv"), mappingTaskFilePath, source, scenarioNo, web);
        } else if (typeElement.getTextTrim().equalsIgnoreCase(SpicyEngineConstants.TYPE_SQL)) {
            result = loadSQLSource(dataSourceElement.getChild("sql"), mappingTaskFilePath, source, scenarioNo, web);
        } else if (typeElement.getTextTrim().equalsIgnoreCase(SpicyEngineConstants.TYPE_OBJECT)) {
            result = loadObjectSource(dataSourceElement.getChild("objectModel"), listOfInclusionPaths, listOfExclusionPaths);
        } else if (typeElement.getTextTrim().equalsIgnoreCase(SpicyEngineConstants.TYPE_MOCK)) {
            return null;
        } else if (typeElement.getTextTrim().equalsIgnoreCase(SpicyEngineConstants.PROVIDER_TYPE_CHAINING)) {
            Element mappingTaskElement = dataSourceElement.getChild("mappingTask");
            String previousStepRelativeFilePath = mappingTaskElement.getTextTrim();
            String previousStepFilePath = filePathTransformator.expand(mappingTaskFilePath, previousStepRelativeFilePath);
            MappingTask previousStep = this.loadMappingTask(scenarioNo, previousStepFilePath, web);
            result = new ChainingDataSourceProxy(previousStep, previousStepFilePath);
        } else if (typeElement.getTextTrim().equalsIgnoreCase(SpicyEngineConstants.PROVIDER_TYPE_MERGE)) {
            result = loadMergeProvider(dataSourceElement, mappingTaskFilePath, source, scenarioNo, web);
        } else {
            throw new DAOException("Unable to load data source provider with type " + typeElement.getTextTrim());
        }
        setInclusionsExclusion(result, listOfInclusionPaths, listOfExclusionPaths);
        // duplications
        Element duplicationsElement = dataSourceElement.getChild("duplications");
        if (duplicationsElement != null) {
            List<PathExpression> duplications = loadDuplications(duplicationsElement);
            for (PathExpression duplication : duplications) {
                result.addDuplication(duplication);
            }
        }
        // functional dependencies
        Element functionalDependenciesElement = dataSourceElement.getChild("functionalDependencies");
        if (functionalDependenciesElement != null) {
            List<FunctionalDependency> functionalDependencies = loadFunctionalDependencies(functionalDependenciesElement);
            for (FunctionalDependency functionalDependency : functionalDependencies) {
                result.addFunctionalDependency(functionalDependency);
            }
        }
        // selection conditions
        Element selectionConditionsElement = dataSourceElement.getChild("selectionConditions");
        if (selectionConditionsElement != null) {
            List<SelectionCondition> selectionConditions = loadSelectionConditions(selectionConditionsElement, result.getIntermediateSchema());
            for (SelectionCondition selectionCondition : selectionConditions) {
                result.addSelectionCondition(selectionCondition);
            }
        }
        // join conditions
        Element joinConditionsElement = dataSourceElement.getChild("joinConditions");
        if (joinConditionsElement != null) {
            List<JoinCondition> joinConditions = loadJoinConditions(joinConditionsElement);
            for (JoinCondition joinCondition : joinConditions) {
                result.addJoinCondition(joinCondition);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private IDataSourceProxy loadMergeProvider(Element dataSourceElement, String mappingTaskFilePath, boolean source, int scenarioNo, boolean web) throws DAOException, SQLException {
        List<IDataSourceProxy> proxies = new ArrayList<IDataSourceProxy>();
        List<Element> listSourceProxyElement = dataSourceElement.getChildren("source");
        for (Element sourceProxyElement : listSourceProxyElement) {
            IDataSourceProxy sourceProxy = loadDataSourceProxy(sourceProxyElement, mappingTaskFilePath, source, scenarioNo, web);
            proxies.add(sourceProxy);
        }
        return new MergeDataSourceProxy(proxies);
    }

    private void setInclusionsExclusion(IDataSourceProxy dataSource, List<String> listOfInclusionPaths, List<String> listOfExclusionPaths) {
        MarkNodesToExclude nodeExcluder = new MarkNodesToExclude();
        for (String inclusion : listOfInclusionPaths) {
            dataSource.addInclusion(pathGenerator.generatePathFromString(inclusion));
        }
        for (String exclusion : listOfExclusionPaths) {
            PathExpression exclusionPathExpression = pathGenerator.generatePathFromString(exclusion);
            dataSource.addExclusion(exclusionPathExpression);
        }
        nodeExcluder.excludeNodes(dataSource);
    }

    @SuppressWarnings("unchecked")
    private List<String> loadInclusionPathString(Element inclusionsElement) {
        List<String> inclusionStringList = new ArrayList<String>();
        List<Element> inclusionElementList = inclusionsElement.getChildren("inclusion");
        for (Element inclusionElement : inclusionElementList) {
            inclusionStringList.add(inclusionElement.getTextTrim());
        }
        return inclusionStringList;
    }

    @SuppressWarnings("unchecked")
    private List<String> loadExclusionPathString(Element exclusionsElement) {
        List<String> exclusionStringList = new ArrayList<String>();
        List<Element> exclusionElementList = exclusionsElement.getChildren("exclusion");
        for (Element exclusionElement : exclusionElementList) {
            exclusionStringList.add(exclusionElement.getTextTrim());
        }
        return exclusionStringList;
    }

    @SuppressWarnings("unchecked")
    private List<PathExpression> loadDuplications(Element duplicationsElement) {
        List<PathExpression> result = new ArrayList<PathExpression>();
        List<Element> duplications = duplicationsElement.getChildren("duplication");
        for (Element duplication : duplications) {
            String setPathString = duplication.getTextTrim();
            result.add(generatePathExpression(setPathString));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<FunctionalDependency> loadFunctionalDependencies(Element functionalDependenciesElement) {
        List<FunctionalDependency> result = new ArrayList<FunctionalDependency>();
        List<Element> functionalDependenciesElementList = functionalDependenciesElement.getChildren("functionalDependency");
        for (Element functionalDependencyElement : functionalDependenciesElementList) {
            result.add(loadFunctionalDependency(functionalDependencyElement));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private FunctionalDependency loadFunctionalDependency(Element functionalDependencyElement) {
        List<Element> leftPathsElementList = functionalDependencyElement.getChildren("leftPath");
        List<PathExpression> leftPaths = new ArrayList<PathExpression>();
        for (Element leftPathElement : leftPathsElementList) {
            String leftPath = leftPathElement.getTextTrim();
            PathExpression fromPathExpression = generatePathExpression(leftPath);
            leftPaths.add(fromPathExpression);
        }
        List<Element> rightPathsElementList = functionalDependencyElement.getChildren("rightPath");
        List<PathExpression> rightPaths = new ArrayList<PathExpression>();
        for (Element rightPathElement : rightPathsElementList) {
            String rightPath = rightPathElement.getTextTrim();
            PathExpression fromPathExpression = generatePathExpression(rightPath);
            rightPaths.add(fromPathExpression);
        }
        FunctionalDependency functionalDependency = new FunctionalDependency(leftPaths, rightPaths);
        return functionalDependency;
    }

    @SuppressWarnings("unchecked")
    private List<JoinCondition> loadJoinConditions(Element joinConditionsElement) {
        List<JoinCondition> result = new ArrayList<JoinCondition>();
        List<Element> joinConditionElementList = joinConditionsElement.getChildren("joinCondition");
        for (Element joinConditionElement : joinConditionElementList) {
            result.add(loadJoinCondition(joinConditionElement));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private JoinCondition loadJoinCondition(Element joinConditionElement) {
        List<Element> joinElementList = joinConditionElement.getChildren("join");
        List<PathExpression> fromPaths = new ArrayList<PathExpression>();
        List<PathExpression> toPaths = new ArrayList<PathExpression>();
        for (Element joinElement : joinElementList) {
            String fromPath = joinElement.getChildTextTrim("from");
            String toPath = joinElement.getChildTextTrim("to");
            PathExpression fromPathExpression = generatePathExpression(fromPath);
            PathExpression toPathExpression = generatePathExpression(toPath);
            fromPaths.add(fromPathExpression);
            toPaths.add(toPathExpression);
        }
        JoinCondition joinCondition = new JoinCondition(fromPaths, toPaths);
        Element foreignKeyElement = joinConditionElement.getChild("foreignKey");
        boolean foreignKey = false;
        if (foreignKeyElement != null && foreignKeyElement.getTextTrim() != null) {
            foreignKey = Boolean.parseBoolean(foreignKeyElement.getTextTrim());
        }
        joinCondition.setMonodirectional(foreignKey);
        Element mandatoryElement = joinConditionElement.getChild("mandatory");
        boolean mandatory = false;
        if (mandatoryElement != null && mandatoryElement.getTextTrim() != null) {
            mandatory = Boolean.parseBoolean(mandatoryElement.getTextTrim());
        }
        joinCondition.setMandatory(mandatory);
        Element matchStringElement = joinConditionElement.getChild("matchString");
        boolean matchString = false;
        if (matchStringElement != null && matchStringElement.getTextTrim() != null) {
            matchString = Boolean.parseBoolean(matchStringElement.getTextTrim());
        }
        joinCondition.setMatchString(matchString);
        return joinCondition;
    }

    @SuppressWarnings("unchecked")
    private List<SelectionCondition> loadSelectionConditions(Element selectionConditionsElement, INode schema) {
        List<SelectionCondition> result = new ArrayList<SelectionCondition>();
        List<Element> selectionConditionsElementList = selectionConditionsElement.getChildren("selectionCondition");
        for (Element selectionConditionElement : selectionConditionsElementList) {
            List<Element> setPathElementList = selectionConditionElement.getChildren("setPath");
            List<PathExpression> setPaths = new ArrayList<PathExpression>();
            for (Element setPathElement : setPathElementList) {
                String setPathString = setPathElement.getTextTrim();
                PathExpression setPathExpression = generatePathExpression(setPathString);
                setPaths.add(setPathExpression);
            }
            String conditionString = selectionConditionElement.getChildTextTrim("condition");
            Expression condition = new Expression(conditionString);
            SelectionCondition selectionCondition = new SelectionCondition(setPaths, condition, schema);
            result.add(selectionCondition);
        }
        return result;
    }

    private IDataSourceProxy loadRelationalSource(Element sourceTargetRelationalElement, List<String> listOfInclusionPaths, 
            List<String> listOfExclusionPaths, boolean source, int scenarioNo, boolean web) throws DAOException, SQLException {
        Element driverElement = sourceTargetRelationalElement.getChild("driver");
        Element uriElement = sourceTargetRelationalElement.getChild("uri");
        Element schemaNameElement = sourceTargetRelationalElement.getChild("schema");
        Element loginElement = sourceTargetRelationalElement.getChild("login");
        Element passwordElement = sourceTargetRelationalElement.getChild("password");
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(driverElement.getTextTrim());
        accessConfiguration.setUri(uriElement.getTextTrim());
        if (schemaNameElement != null) {
            accessConfiguration.setSchemaName(schemaNameElement.getTextTrim());
        }
        accessConfiguration.setLogin(loginElement.getTextTrim());
        accessConfiguration.setPassword(passwordElement.getTextTrim());
        DBFragmentDescription dataDescription = new DBFragmentDescription();
        for (String inclusion : listOfInclusionPaths) {
            dataDescription.addInclusionPath(inclusion);
        }
        for (String exclusion : listOfExclusionPaths) {
            dataDescription.addExclusionPath(exclusion);
        }
        IConnectionFactory dataSourceDB = new SimpleDbConnectionFactory();
        IDataSourceProxy dataSource;
        if (web){
            dataSource = daoRelational.loadSchemaForWeb(scenarioNo, accessConfiguration, dataDescription, dataSourceDB, source);
        }
        else {
            dataSource = daoRelational.loadSchema(scenarioNo, accessConfiguration, dataDescription, dataSourceDB, source);
            daoRelational.loadInstanceSample(accessConfiguration, dataSource, dataDescription, dataSourceDB, null, false);
        }
        return dataSource;
    }

    @SuppressWarnings("unchecked")
    private IDataSourceProxy loadXMLSource(Element sourceTargetXmlElement, String mappingTaskFilePath) throws DAOException {
        Element sourceXmlSchemaElement = sourceTargetXmlElement.getChild("xml-schema");
        String xmlSchemaRelativeFilePath = sourceXmlSchemaElement.getTextTrim();
        String xmlSchemaAbsolutePath = filePathTransformator.expand(mappingTaskFilePath, xmlSchemaRelativeFilePath);
        IDataSourceProxy dataSource = daoXSD.loadSchema(xmlSchemaAbsolutePath);
        Element sourceXmlInstancesElement = sourceTargetXmlElement.getChild("xml-instances");
        List<Element> sourceXmlInstanceElementList = sourceXmlInstancesElement.getChildren("xml-instance");
        for (Element sourceXmlInstanceElement : sourceXmlInstanceElementList) {
            String xmlInstanceRelativeFilePath = sourceXmlInstanceElement.getTextTrim();
            String xmlInstanceAbsoluteFilePath = filePathTransformator.expand(mappingTaskFilePath, xmlInstanceRelativeFilePath);
            daoXSD.loadInstance(dataSource, xmlInstanceAbsoluteFilePath);
        }
        return dataSource;
    }
    
    //giannisk
    @SuppressWarnings("unchecked")
    private IDataSourceProxy loadCSVSource(Element sourceTargetCsvElement, String mappingTaskFilePath, boolean source, int scenarioNo, boolean web) 
            throws DAOException, SQLException {
        HashSet<String> csvFullTableAbsoluteFilePath = new HashSet<String>();        
        HashMap<String,ArrayList<Object>> csvFullInstAbsoluteFilePath = new HashMap<String,ArrayList<Object>>();
        Element sourceCsvDatabaseElement = sourceTargetCsvElement.getChild("csv-db-name");
        String csvDatabaseName = sourceCsvDatabaseElement.getTextTrim();       
        Element sourceCsvTablesElement = sourceTargetCsvElement.getChild("csv-tables");
        List<Element> sourceCsvTableElementList = sourceCsvTablesElement.getChildren("csv-table");
        for (Element sourceCsvTableElement : sourceCsvTableElementList) {
            Element sourceCsvFileElement = sourceCsvTableElement.getChild("schema");
            String csvTableRelativeFilePath = sourceCsvFileElement.getTextTrim();
            //create the full path for all files that contain schema tables
            String csvTableAbsoluteFilePath = filePathTransformator.expand(mappingTaskFilePath, csvTableRelativeFilePath);
            csvFullTableAbsoluteFilePath.add(csvTableAbsoluteFilePath);
            //getting the filename from file's full path
            File userFile = new File(csvTableAbsoluteFilePath);
            String tableName = userFile.getName();
            //exclude filename extension
            if (tableName.indexOf(".") > 0) {
                tableName = tableName.substring(0, tableName.lastIndexOf("."));
            } 
            //create the full path for all files that contain instances too
            Element sourceCsvInstancesElement = sourceCsvTableElement.getChild("instances");
            List<Element> sourceCsvInstanceElementList = sourceCsvInstancesElement.getChildren("instance");
            for (Element sourceCsvInstanceElement : sourceCsvInstanceElementList) {
                ArrayList<Object> valSet = new ArrayList<Object>();
                Element sourceCsvInstPathElement = sourceCsvInstanceElement.getChild("path");
                String csvInstRelativeFilePath = sourceCsvInstPathElement.getTextTrim();
                String csvInstAbsoluteFilePath = filePathTransformator.expand(mappingTaskFilePath, csvInstRelativeFilePath);
                Element sourceCsvInstColNamesElement = sourceCsvInstanceElement.getChild("column-names");
                boolean csvInstColNames = Boolean.valueOf(sourceCsvInstColNamesElement.getTextTrim());
                valSet.add(tableName);
                valSet.add(csvInstColNames);
                valSet.add(false);
                csvFullInstAbsoluteFilePath.put(csvInstAbsoluteFilePath,valSet);
            }
        }
        IDataSourceProxy dataSource;
        if (web){
            dataSource = daoCSV.loadSchemaForWeb(scenarioNo, csvFullTableAbsoluteFilePath, csvDatabaseName);
        }
        else{
            dataSource = daoCSV.loadSchema(scenarioNo, csvFullTableAbsoluteFilePath, csvDatabaseName, source, csvFullInstAbsoluteFilePath);
            if (!csvFullInstAbsoluteFilePath.isEmpty()){
                daoCSV.loadInstanceSample(dataSource, csvFullInstAbsoluteFilePath, csvDatabaseName);
            }
        }
        return dataSource;
    }
    
    //giannisk
    @SuppressWarnings("unchecked")
    private IDataSourceProxy loadSQLSource(Element sourceTargetSqlElement, String mappingTaskFilePath, boolean source, int scenarioNo, boolean web) 
            throws DAOException, SQLException {
        Element databaseElement = sourceTargetSqlElement.getChild("sql-db-name");
        String sqlDatabaseName = databaseElement.getTextTrim();
        Element sourceSqlFileElement = sourceTargetSqlElement.getChild("sql-file");
        String sqlRelativeFilePath = sourceSqlFileElement.getTextTrim();
        String sqlAbsoluteFilePath = filePathTransformator.expand(mappingTaskFilePath, sqlRelativeFilePath);
        //delete the starting "\" character in Windows
        //if the file path starts with "\" followed by a character followed by a ":" then delete the first "\" 
        sqlAbsoluteFilePath = sqlAbsoluteFilePath.replaceFirst("^/(.:)", "$1");
        IDataSourceProxy dataSource;
        if (web){
            dataSource = daoSQL.loadSchemaForWeb(scenarioNo, sqlDatabaseName, sqlAbsoluteFilePath, source);
        }
        else{
            dataSource = daoSQL.loadSchema(scenarioNo, sqlDatabaseName, sqlAbsoluteFilePath, source);
            daoSQL.loadInstanceSample(dataSource, sqlDatabaseName, sqlAbsoluteFilePath);
        }
        return dataSource;
    }

    private IDataSourceProxy loadObjectSource(Element objectModelElement, List<String> listOfInclusionPaths, List<String> listOfExclusionPaths) throws DAOException {
        Element classPathFolderElement = objectModelElement.getChild("classPathFolder");
        Element objectModelFactoryElement = objectModelElement.getChild("objectModelFactory");
        IDataSourceProxy dataSource = null;
        try {
            String classPathFolder = classPathFolderElement.getTextTrim();
            String objectModelFactoryName = objectModelFactoryElement.getTextTrim();
            dataSource = daoObject.generateDataSource(classPathFolder, objectModelFactoryName);
        } catch (Exception ex) {
            logger.error(ex);
            throw new DAOException(ex.getMessage());
        }
        return dataSource;
    }

    @SuppressWarnings("unchecked")
    private List<ValueCorrespondence> loadValueCorrespondences(Element correspondencesElement) throws SQLException, DAOException {
        List<ValueCorrespondence> valueCorrespondenceList = new ArrayList<ValueCorrespondence>();
        List<Element> valueCorrespondenceElementList = correspondencesElement.getChildren("correspondence");
        for (Element valueCorrespondence : valueCorrespondenceElementList) {
            valueCorrespondenceList.add(loadValueCorrespondence(valueCorrespondence));
        }
        return valueCorrespondenceList;
    }

    @SuppressWarnings("unchecked")
    private ValueCorrespondence loadValueCorrespondence(Element correspondenceElement) throws SQLException, DAOException {
        Element sourcePathsElement = correspondenceElement.getChild("source-paths");
        List<PathExpression> sourcePaths = null;
        if (sourcePathsElement != null && sourcePathsElement.getChild("source-path") != null) {
            sourcePaths = new ArrayList<PathExpression>();
            List<Element> sourcePathElementList = sourcePathsElement.getChildren("source-path");
            for (Element sourcePathElement : sourcePathElementList) {
                String pathDescription = sourcePathElement.getTextTrim();
                PathExpression pathExpression = generatePathExpression(pathDescription);
                sourcePaths.add(pathExpression);
            }
        }
        Element sourceValueElement = correspondenceElement.getChild("source-value");
        ISourceValue sourceValue = null;
        if (sourceValueElement != null) {
            String sourceValueString = sourceValueElement.getTextTrim();
            if (sourceValueString.equalsIgnoreCase(SpicyEngineConstants.SOURCEVALUE_DATE_FUNCTION)) {
                sourceValue = new DateFunction();
            } else if (sourceValueString.split("_")[0].equalsIgnoreCase(SpicyEngineConstants.SOURCEVALUE_NEWID_FUNCTION)) {
                sourceValue = new NewIdFunction();
                sourceValue.setType("constant");
                sourceValue.setSequence(sourceValueElement.getChild("sequence").getTextTrim());
                SpicyEngineConstants.OFFSET_MAPPING.put(sourceValueElement.getChild("sequence").getTextTrim(), 
                        sourceValueElement.getChild("offset").getTextTrim());
            } else if (sourceValueString.split("_")[0].equalsIgnoreCase(SpicyEngineConstants.SOURCEVALUE_NEWID_FUNCTION_GET_ID)) {
                sourceValue = new NewIdFunction();
                sourceValue.setType("getId()");
                String sequence = sourceValueElement.getChild("sequence").getTextTrim();
                sourceValue.setSequence(sequence);
                getOffSetFromDB(sourceValueElement.getChild("relational"), sequence);
            } else if (sourceValueString.equalsIgnoreCase(SpicyEngineConstants.SOURCEVALUE_DATETIME_FUNCTION)) {
                sourceValue = new DatetimeFunction();
                sourceValue.setType("datetime");
            }
            else {
                sourceValue = new ConstantValue(sourceValueString);
                sourceValue.setType("string");
            }
        }
        
        Element targetPathElement = correspondenceElement.getChild("target-path");
        PathExpression targetPathExpression = null;
        if (targetPathElement != null && targetPathElement.getTextTrim() != null) {
            String pathDescription = targetPathElement.getTextTrim();
            targetPathExpression = generatePathExpression(pathDescription);
        }
        Element transformationFunctionElement = correspondenceElement.getChild("transformation-function");
        Expression transformationFunctionExpression = null;
        if (transformationFunctionElement != null && transformationFunctionElement.getTextTrim() != null) {
            String textFunction = transformationFunctionElement.getTextTrim();
            transformationFunctionExpression = new Expression(DAOXmlUtility.cleanXmlString(textFunction));
        }
        Element confidenceElement = correspondenceElement.getChild("confidence");
        double confidence = 0;
        if (confidenceElement != null && confidenceElement.getTextTrim() != null) {
            confidence = Double.parseDouble(confidenceElement.getTextTrim());

        }
        ValueCorrespondence valueCorrespondence = new ValueCorrespondence(sourcePaths, sourceValue, targetPathExpression, transformationFunctionExpression, confidence);
        return valueCorrespondence;
    }

    private void getOffSetFromDB(Element elementGetId, String sequence) throws DAOException, SQLException{
        Element driverElement = elementGetId.getChild("driver");
        Element uriElement = elementGetId.getChild("uri");
        Element schemaNameElement = elementGetId.getChild("schema");
        Element loginElement = elementGetId.getChild("login");
        Element passwordElement = elementGetId.getChild("password");
        Element tableElement = elementGetId.getChild("table");
        Element columnElement = elementGetId.getChild("column");
        Element functionElement = elementGetId.getChild("function");
        
        String schema = "";
        if (schemaNameElement != null) {
            schema = schemaNameElement.getTextTrim();
        }
        
        GetIdFromDb newIdFromDb = new GetIdFromDb(driverElement.getTextTrim(), uriElement.getTextTrim(), schema, loginElement.getTextTrim(), 
                passwordElement.getTextTrim(), tableElement.getTextTrim(), columnElement.getTextTrim(), functionElement.getTextTrim());
        SpicyEngineConstants.GET_ID_FROM_DB.put(sequence, newIdFromDb);
        
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(newIdFromDb.getDriver());
        accessConfiguration.setUri(newIdFromDb.getUri());
        if (schemaNameElement != null) {
            accessConfiguration.setSchemaName(newIdFromDb.getSchema());
        }
        accessConfiguration.setLogin(newIdFromDb.getLogin());
        accessConfiguration.setPassword(newIdFromDb.getPassword());
        IConnectionFactory connectionFactory = new SimpleDbConnectionFactory();
        Connection connection = connectionFactory.getConnection(accessConfiguration);
        Statement statement = connection.createStatement();
        
        if (newIdFromDb.getFunction().equalsIgnoreCase("max")){
            statement.execute("SELECT MAX(\""+ newIdFromDb.getColumn() +"\") FROM \"" + newIdFromDb.getTable() + "\";");
            ResultSet rs = statement.getResultSet();
            if (rs.next()) {
                SpicyEngineConstants.OFFSET_MAPPING.put(sequence, String.valueOf(rs.getInt(1)));
            } else {
                SpicyEngineConstants.OFFSET_MAPPING.put(sequence, "0");
            }
        }
    }
    
    private PathExpression generatePathExpression(String pathDescription) {
        return pathGenerator.generatePathFromString(pathDescription);
    }

    /*  /////////////////////////////////////////////////////////
     *                        SAVE
     *  ///////////////////////////////////////////////////////// */
    public void saveMappingTask(MappingTask mappingTask, String filePath) throws DAOException {
        try {
            Document document = new Document();
            //ROOT ELEMENT
            Element rootElement = createRootElement(mappingTask, document);
            createConfigElement(mappingTask, rootElement);
            //source
            Element sourceElement = new Element("source");            
            if (mappingTask.getSourceProxy().getProviderType().equalsIgnoreCase(SpicyEngineConstants.PROVIDER_TYPE_CONSTANT)) {
                createDataSourceElement(mappingTask.getSourceProxy(), sourceElement, filePath);
            } else if (mappingTask.getSourceProxy().getProviderType().equalsIgnoreCase(SpicyEngineConstants.PROVIDER_TYPE_CHAINING)) {
                createChainingProviderElement((ChainingDataSourceProxy) mappingTask.getSourceProxy(), sourceElement, filePath);
            } else {
                throw new DAOException("Unable to save mapping task provider type: " + mappingTask.getSourceProxy().getProviderType());
            }            
            rootElement.addContent(sourceElement);            
            //target
            Element targetElement = new Element("target");            
            createDataSourceElement(mappingTask.getTargetProxy(), targetElement, filePath);
            rootElement.addContent(targetElement);
            //VALUECORRESPONDENCE
            setValueCorrespondences(mappingTask.getValueCorrespondences(), rootElement);
            //SALVATAGGIO SU FILE            
            daoUtility.saveDOM(document, filePath);
            mappingTask.setToBeSaved(false);
        } catch (Throwable t) {
            logger.error(t);
            throw new DAOException(t.getMessage());
        }
    }

    private Element createRootElement(MappingTask mappingTask, Document document) {
        Element rootElement = new Element("mappingtask");
        document.setRootElement(rootElement);
        return rootElement;
    }

    private void createConfigElement(MappingTask mappingTask, Element rootElement) {
        Element configElement = new Element("config");
        Element rewriteSubsumptionsElement = new Element("rewriteSubsumptions");
        rewriteSubsumptionsElement.setText(mappingTask.getConfig().rewriteSubsumptions() + "");
        configElement.addContent(rewriteSubsumptionsElement);
        Element rewriteCoveragesElement = new Element("rewriteCoverages");
        rewriteCoveragesElement.setText(mappingTask.getConfig().rewriteCoverages() + "");
        configElement.addContent(rewriteCoveragesElement);
        Element rewriteSelfJoinsElement = new Element("rewriteSelfJoins");
        rewriteSelfJoinsElement.setText(mappingTask.getConfig().rewriteSelfJoins() + "");
        configElement.addContent(rewriteSelfJoinsElement);
        Element rewriteEGDsElement = new Element("rewriteEGDs");
        rewriteEGDsElement.setText(mappingTask.getConfig().rewriteEGDs() + "");
        configElement.addContent(rewriteEGDsElement);
        Element sortStrategyElement = new Element("sortStrategy");
        sortStrategyElement.setText(mappingTask.getConfig().getSortStrategy() + "");
        configElement.addContent(sortStrategyElement);
        Element skolemTableStrategyElement = new Element("skolemTableStrategy");
        skolemTableStrategyElement.setText(mappingTask.getConfig().getSkolemTableStrategy() + "");
        configElement.addContent(skolemTableStrategyElement);
        Element useLocalSkolemElement = new Element("useLocalSkolem");
        useLocalSkolemElement.setText(mappingTask.getConfig().useLocalSkolem() + "");
        configElement.addContent(useLocalSkolemElement);
        rootElement.addContent(configElement);
    }

    private void createChainingProviderElement(ChainingDataSourceProxy sourceProvider, Element sourceTargetElement, String fileName) {
        //type
        Element typeElement = new Element("type");
        typeElement.setText(sourceProvider.getProviderType());
        sourceTargetElement.addContent(typeElement);
        //mappingTask
        Element mappingTaskElement = new Element("mappingTask");
        String relativeSchemaPath = filePathTransformator.relativize(fileName, sourceProvider.getMappingTaskFilePath());
        mappingTaskElement.setText(relativeSchemaPath);
//        mappingTaskElement.setText(sourceProvider.getMappingTaskFilePath());
        sourceTargetElement.addContent(mappingTaskElement);
        //INCLUSION
        Element inclusions = createInclusion(sourceProvider.getInclusions());
        sourceTargetElement.addContent(inclusions);
        //EXCLUSION
        Element exclusions = createExclusion(sourceProvider.getExclusions());
        sourceTargetElement.addContent(exclusions);
        //DUPLICATIONS
        Element duplications = createDuplications(sourceProvider.getDuplications());
        sourceTargetElement.addContent(duplications);
        //FUNCTIONAL DEPENDENCIES
        Element functionalDependencies = createFunctionalDependencies(sourceProvider.getFunctionalDependencies());
        sourceTargetElement.addContent(functionalDependencies);
        //SELECTION CONDITIONS
        Element selectionPaths = createSelectionPaths(sourceProvider.getSelectionConditions());
        sourceTargetElement.addContent(selectionPaths);
        //JOIN CONDITIONS
        Element joinConditions = createjoinConditions(sourceProvider.getJoinConditions());
        sourceTargetElement.addContent(joinConditions);
    }

    private void createDataSourceElement(IDataSourceProxy datasource, Element sourceTargetElement, String fileName) {
        //type
        Element typeElement = new Element("type");
        typeElement.setText(datasource.getType());
        sourceTargetElement.addContent(typeElement);
        //CSV vs SQL vs RELATIONAL vs XML vs OBJECT
        Element sourceTargetDetailsElement = null;
        if (datasource.getType().equalsIgnoreCase(SpicyEngineConstants.TYPE_RELATIONAL)) {
            sourceTargetDetailsElement = createRelationalDetail(datasource.getAnnotations());
        } else if (datasource.getType().equalsIgnoreCase(SpicyEngineConstants.TYPE_XML)) {
            sourceTargetDetailsElement = createXMLDetail(datasource.getAnnotations(), fileName);
        } else if (datasource.getType().equalsIgnoreCase(SpicyEngineConstants.TYPE_OBJECT)) {
            sourceTargetDetailsElement = createObjectDetail(datasource.getAnnotations());
            //giannisk
        } else if (datasource.getType().equalsIgnoreCase(SpicyEngineConstants.TYPE_CSV)) {
            sourceTargetDetailsElement = createCSVDetail(datasource.getAnnotations(), fileName);
        } else if (datasource.getType().equalsIgnoreCase(SpicyEngineConstants.TYPE_SQL)) {
            sourceTargetDetailsElement = createSQLDetail(datasource.getAnnotations(), fileName);
        } else if (datasource.getType().equalsIgnoreCase(SpicyEngineConstants.TYPE_MOCK)) {
            sourceTargetDetailsElement = createMockDetail(datasource.getAnnotations());
        }
        sourceTargetElement.addContent(sourceTargetDetailsElement);
        //INCLUSION
        Element inclusions = createInclusion(datasource.getInclusions());
        sourceTargetElement.addContent(inclusions);
        //EXCLUSION
        Element exclusions = createExclusion(datasource.getExclusions());
        sourceTargetElement.addContent(exclusions);
        //DUPLICATIONS
        Element duplications = createDuplications(datasource.getDuplications());
        sourceTargetElement.addContent(duplications);
        //FUNCTIONAL DEPENDENCIES
        Element functionalDependencies = createFunctionalDependencies(datasource.getFunctionalDependencies());
        sourceTargetElement.addContent(functionalDependencies);
        //SELECTION CONDITIONS
        Element selectionPaths = createSelectionPaths(datasource.getSelectionConditions());
        sourceTargetElement.addContent(selectionPaths);
        //JOIN CONDITIONS
        Element joinConditions = createjoinConditions(datasource.getJoinConditions());
        sourceTargetElement.addContent(joinConditions);
    }

    private Element createInclusion(List<PathExpression> inclusionsList) {
        Element inclusions = new Element("inclusions");
        for (PathExpression inclusionPath : inclusionsList) {
            Element inclusion = new Element("inclusion");
            inclusion.setText(inclusionPath.toString());
            inclusions.addContent(inclusion);
        }
        return inclusions;
    }

    private Element createExclusion(List<PathExpression> exclusionsList) {
        Element exclusions = new Element("exclusions");
        for (PathExpression exnclusionPath : exclusionsList) {
            Element exclusion = new Element("exclusion");
            exclusion.setText(exnclusionPath.toString());
            exclusions.addContent(exclusion);
        }
        return exclusions;
    }

    private Element createDuplications(List<Duplication> duplicationsList) {
        Element duplications = new Element("duplications");
        for (Duplication duplication : duplicationsList) {
            Element duplicationElement = new Element("duplication");
            duplicationElement.setText(duplication.getOriginalPath().toString());
            duplications.addContent(duplicationElement);
        }
        return duplications;
    }

    private Element createFunctionalDependencies(List<FunctionalDependency> functionalDependencies) {
        Element functionalDependencyElement = new Element("functionalDependencies");
        for (FunctionalDependency functionalDependency : functionalDependencies) {
            functionalDependencyElement.addContent(createFunctionalDependency(functionalDependency));
        }
        return functionalDependencyElement;
    }

    private Element createFunctionalDependency(FunctionalDependency functionalDependency) {
        Element functionalDependencyElement = new Element("functionalDependency");
        for (PathExpression leftPath : functionalDependency.getLeftPaths()) {
            Element leftPathElement = new Element("leftPath");
            leftPathElement.setText(leftPath.toString());
            functionalDependencyElement.addContent(leftPathElement);
        }
        for (PathExpression rightPath : functionalDependency.getRightPaths()) {
            Element rightPathElement = new Element("rightPath");
            rightPathElement.setText(rightPath.toString());
            functionalDependencyElement.addContent(rightPathElement);
        }
        return functionalDependencyElement;
    }

    private Element createjoinConditions(List<JoinCondition> joinConditions) {
        Element joinConditionElement = new Element("joinConditions");
        for (JoinCondition joinCondition : joinConditions) {
//            if (!joinCondition.isForeignKey()) {
            joinConditionElement.addContent(createjoinCondition(joinCondition));
        } //        }
        return joinConditionElement;
    }

    private Element createjoinCondition(JoinCondition joinCondition) {
        Element joinConditionElement = new Element("joinCondition");
        for (int i = 0; i
                < joinCondition.getFromPaths().size(); i++) {
            PathExpression fromPathExpression = joinCondition.getFromPaths().get(i);
            PathExpression toPathExpression = joinCondition.getToPaths().get(i);
            Element join = new Element("join");
            Element fromPath = new Element("from");
            fromPath.setText(fromPathExpression.toString());
            Element toPath = new Element("to");
            toPath.setText(toPathExpression.toString());
            join.addContent(fromPath);
            join.addContent(toPath);
            joinConditionElement.addContent(join);
        }
        Element foreignKeyElement = new Element("foreignKey");
        foreignKeyElement.setText(Boolean.toString(joinCondition.isMonodirectional()));
        Element mandatoryElement = new Element("mandatory");
        mandatoryElement.setText(Boolean.toString(joinCondition.isMandatory()));
        Element matchStringElement = new Element("matchString");
        matchStringElement.setText(Boolean.toString(joinCondition.isMatchString()));
        joinConditionElement.addContent(foreignKeyElement);
        joinConditionElement.addContent(mandatoryElement);
        joinConditionElement.addContent(matchStringElement);
        return joinConditionElement;
    }

    private Element createSelectionPaths(List<SelectionCondition> selectionConditions) {
        Element selectionPaths = new Element("selectionConditions");
        for (SelectionCondition selectionCondition : selectionConditions) {
            selectionPaths.addContent(createSelectionPath(selectionCondition));
        }
        return selectionPaths;
    }

    private Element createSelectionPath(SelectionCondition selectionCondition) {
        Element selectionElement = new Element("selectionCondition");
        for (PathExpression setPath : selectionCondition.getSetPaths()) {
            Element setPathElement = new Element("setPath");
            setPathElement.setText(setPath.toString());
            selectionElement.addContent(setPathElement);
        }
        Element condition = new Element("condition");
        condition.setText(selectionCondition.getCondition().toString());
        selectionElement.addContent(condition);
        return selectionElement;
    }

    private Element createRelationalDetail(Map<String, Object> annotations) {
        Element relational = new Element("relational");
        //driver
        AccessConfiguration accessConfiguration = (AccessConfiguration) annotations.get(SpicyEngineConstants.ACCESS_CONFIGURATION);
        Element driver = new Element("driver");
        driver.setText(accessConfiguration.getDriver());
        relational.addContent(driver);
        //uri
        Element uri = new Element("uri");
        uri.setText(accessConfiguration.getUri());
        relational.addContent(uri);
        //login
        Element login = new Element("login");
        login.setText(accessConfiguration.getLogin());
        relational.addContent(login);
        //password
        Element password = new Element("password");
        password.setText(accessConfiguration.getPassword());
        relational.addContent(password);
        //
        return relational;
    }

    @SuppressWarnings("unchecked")
    private Element createXMLDetail(Map<String, Object> annotations, String mappingTaskFilePath) {
        Element sourceXML = new Element("xml");
        //source-xml-schema
        Element sourceXmlSchema = new Element("xml-schema");
        String schemaPath = (String) annotations.get(SpicyEngineConstants.XML_SCHEMA_FILE);
        String relativeSchemaPath = filePathTransformator.relativize(mappingTaskFilePath, schemaPath);
        sourceXmlSchema.setText(relativeSchemaPath);
        sourceXML.addContent(sourceXmlSchema);
        //source-xml-instances
        Element sourceXmlInstances = new Element("xml-instances");
        sourceXML.addContent(sourceXmlInstances);
        //source-xml-instance
        List<String> sourceXmlInstancesList = (List<String>) annotations.get(SpicyEngineConstants.XML_INSTANCE_FILE_LIST);
        for (String absoluteInstancePath : sourceXmlInstancesList) {
            String relativeInstancePath = filePathTransformator.relativize(mappingTaskFilePath, absoluteInstancePath);
            Element sourceXmlInstance = new Element("xml-instance");
            sourceXmlInstance.setText(relativeInstancePath);
            sourceXmlInstances.addContent(sourceXmlInstance);
        }
        return sourceXML;
    }

    //giannisk
    @SuppressWarnings("unchecked")
    private Element createCSVDetail(Map<String, Object> annotations, String mappingTaskFilePath) {
        Element sourceCSV = new Element("csv");
        //source-csv-db-name
        Element sourceCsvSchema = new Element("csv-db-name");
        String dbName = (String) annotations.get(SpicyEngineConstants.CSV_DB_NAME);
        sourceCsvSchema.setText(dbName);
        sourceCSV.addContent(sourceCsvSchema);
        //source-csv-tables
        Element sourceCsvTables = new Element("csv-tables");
        sourceCSV.addContent(sourceCsvTables);
        //source-csv-table
        List<String> sourceCsvTablesList = (List<String>) annotations.get(SpicyEngineConstants.CSV_TABLE_FILE_LIST);
        HashMap<String,ArrayList<Object>> sourceCsvInstancesList = new HashMap<String,ArrayList<Object>>();
        //if there are instance files, get their values to the list
        if (annotations.containsKey(SpicyEngineConstants.CSV_INSTANCES_INFO_LIST))
            {sourceCsvInstancesList = (HashMap<String,ArrayList<Object>>) annotations.get(SpicyEngineConstants.CSV_INSTANCES_INFO_LIST);}
        if(!sourceCsvTablesList.isEmpty()){
            for (String absoluteTablePath : sourceCsvTablesList) {                
                String relativeTablePath = filePathTransformator.relativize(mappingTaskFilePath, absoluteTablePath);
                Element sourceCsvTable = new Element("csv-table");
                sourceCsvTables.addContent(sourceCsvTable);
                
                Element schemaCsvTable = new Element("schema");
                schemaCsvTable.setText(relativeTablePath);
                sourceCsvTable.addContent(schemaCsvTable);            
                
                Element instancesCsvTable = new Element("instances");
                sourceCsvTable.addContent(instancesCsvTable);
                
                if (!sourceCsvInstancesList.isEmpty()){
                    for (Map.Entry<String, ArrayList<Object>> entry : sourceCsvInstancesList.entrySet()) {
                        //if the table name of the instance is the same as the filename of the schema                        
                        String instTableName = (String) entry.getValue().get(0);
                        String schemaTableName = absoluteTablePath.substring(absoluteTablePath.lastIndexOf(File.separator)+1);
                        //exclude filename extension
                        if (schemaTableName.indexOf(".") > 0) {
                            schemaTableName = schemaTableName.substring(0, schemaTableName.lastIndexOf("."));
                        }
                        if (instTableName.equals(schemaTableName)){
                            Element instanceCsvTable = new Element("instance");
                            instancesCsvTable.addContent(instanceCsvTable);
                            
                            Element pathInstanceCsvTable = new Element("path");
                            String relativeInstancePath = filePathTransformator.relativize(mappingTaskFilePath, entry.getKey());
                            pathInstanceCsvTable.setText(relativeInstancePath);
                            
                            Element colNamesCsvTable = new Element("column-names");
                            boolean inclColNames = (Boolean) entry.getValue().get(1);
                            if(!inclColNames){
                                colNamesCsvTable.setText(SpicyEngineConstants.NOT_INCL_COL_NAMES);
                            }
                            else{
                                colNamesCsvTable.setText(SpicyEngineConstants.INCL_COL_NAMES);
                            }
                         
                            instanceCsvTable.addContent(pathInstanceCsvTable);
                            instanceCsvTable.addContent(colNamesCsvTable);                              
                        }                        
                    }                  
                }               
            }
        }
        return sourceCSV;
    }
    
   //giannisk
    private Element createSQLDetail(Map<String, Object> annotations, String mappingTaskFilePath) {
        Element sourceSQL = new Element("sql");
        //source-csv-db-name
        Element sourceSQLSchema = new Element("sql-db-name");
        String dbName = (String) annotations.get(SpicyEngineConstants.SQL_DB_NAME);
        sourceSQLSchema.setText(dbName);
        sourceSQL.addContent(sourceSQLSchema);
        //source-csv-tables
        Element sourceSQLFile = new Element("sql-file");
        String absolutePath = (String) annotations.get(SpicyEngineConstants.SQL_FILE_PATH);
        String relativePath = filePathTransformator.relativize(mappingTaskFilePath, absolutePath);
        sourceSQLFile.setText(relativePath);
        sourceSQL.addContent(sourceSQLFile);        
        return sourceSQL;
    }
    
    private Element createObjectDetail(Map<String, Object> annotations) {
        Element objectModelElement = new Element("objectModel");
        String classPathFolder = (String) annotations.get(SpicyEngineConstants.CLASSPATH_FOLDER);
        String objectModelFactoryName = (String) annotations.get(SpicyEngineConstants.OBJECT_MODEL_FACTORY);
        Element classPathFolderElement = new Element("classPathFolder");
        classPathFolderElement.setText(classPathFolder);
        Element objectModelFactoryElement = new Element("objectModelFactory");
        objectModelFactoryElement.setText(objectModelFactoryName);
        objectModelElement.addContent(classPathFolderElement);
        objectModelElement.addContent(objectModelFactoryElement);
        return objectModelElement;
    }

    private Element createMockDetail(Map<String, Object> annotations) {
        Element mockElement = new Element("mock");
        mockElement.setText("Mock data source");
        return mockElement;
    }

    private void setValueCorrespondences(List<ValueCorrespondence> valueCorrespondences, Element rootElement) {
        Element correspondences = new Element("correspondences");
        rootElement.addContent(correspondences);
        if (valueCorrespondences != null) {
            for (ValueCorrespondence valueCorrespondence : valueCorrespondences) {
                setValueCorrespondence(correspondences, valueCorrespondence);
            }
        }
    }

    private void setValueCorrespondence(Element correspondences, ValueCorrespondence valueCorrespondence) {
        Element correspondence = new Element("correspondence");
        correspondences.addContent(correspondence);

        if (valueCorrespondence == null) {
            return;
        }
        //source-paths
        Element sourcePaths = new Element("source-paths");
        correspondence.addContent(sourcePaths);
        List<PathExpression> sourcePathsExpressionList = valueCorrespondence.getSourcePaths();
        if (sourcePathsExpressionList != null) {
            for (PathExpression path : sourcePathsExpressionList) {
                Element sourcePath = new Element("source-path");
                sourcePath.setText(path.toString());
                sourcePaths.addContent(sourcePath);
            }
        }
//        System.out.println("edw");
        //source-value - ioannisxar
        if (valueCorrespondence.getSourceValue() != null) {
            Element sourceValue = new Element("source-value");
//            System.out.println("edw1: " + valueCorrespondence.getSourceValue().toString());
            if(valueCorrespondence.getSourceValue().getSequence() != null){
                sourceValue.setText(valueCorrespondence.getSourceValue().toString()+"_"+valueCorrespondence.getSourceValue().getSequence());
            } else {
                sourceValue.setText(valueCorrespondence.getSourceValue().toString());
            }
            //if the offset is a constant value or get the offset from database
            if(valueCorrespondence.getSourceValue().toString().split("_")[0].equals("newId()") && valueCorrespondence.getSourceValue().getType().equals("constant")){
                Element sequence = new Element("sequence");
                sequence.setText(valueCorrespondence.getSourceValue().getSequence());
                Element offset = new Element("offset");
                offset.setText(SpicyEngineConstants.OFFSET_MAPPING.get(valueCorrespondence.getSourceValue().getSequence()));
                sourceValue.addContent(sequence);
                sourceValue.addContent(offset);
            } else if(valueCorrespondence.getSourceValue().toString().split("_")[0].equals("newId()") && valueCorrespondence.getSourceValue().getType().equals("getId()")){
                Element relational = new Element("relational");
                Element sequence = new Element("sequence");
                String sequenceName = valueCorrespondence.getSourceValue().getSequence();
                sequence.setText(sequenceName);
                GetIdFromDb getIdFromDb = SpicyEngineConstants.GET_ID_FROM_DB.get(sequenceName);
                
                //add each element text for getting id from database
                Element driverElement = new Element("driver");
                driverElement.setText(getIdFromDb.getDriver());
                Element uriElement = new Element("uri");
                uriElement.setText(getIdFromDb.getUri());
                Element schemaNameElement = new Element("schema");
                if(!getIdFromDb.getSchema().equals("")){
                    schemaNameElement.setText(getIdFromDb.getSchema());
                }
                Element loginElement = new Element("login");
                loginElement.setText(getIdFromDb.getLogin());
                Element passwordElement = new Element("password");
                passwordElement.setText(getIdFromDb.getPassword());
                Element tableElement = new Element("table");
                tableElement.setText(getIdFromDb.getTable());
                Element columnElement = new Element("column");
                columnElement.setText(getIdFromDb.getColumn());
                Element functionElement = new Element("function");
                functionElement.setText(getIdFromDb.getFunction());
                
                //add relational elements to newId
                relational.addContent(driverElement);
                relational.addContent(uriElement);
                if(!getIdFromDb.getSchema().equals("")){
                    relational.addContent(schemaNameElement);
                }
                relational.addContent(loginElement);
                relational.addContent(passwordElement);
                relational.addContent(tableElement);
                relational.addContent(columnElement);
                relational.addContent(functionElement);
                
                
                sourceValue.setText("newId(getId())_"+sequenceName);
                sourceValue.addContent(sequence);
                sourceValue.addContent(relational);
            }
            correspondence.addContent(sourceValue);
        }
        //target-path
        Element targetPath = new Element("target-path");
        targetPath.setText(valueCorrespondence.getTargetPath().toString());
        correspondence.addContent(targetPath);
        //transformation-function
        if (valueCorrespondence.getTransformationFunction() != null) {
            Element transformation = new Element("transformation-function");
            transformation.setText(valueCorrespondence.getTransformationFunction().toString());
            correspondence.addContent(transformation);
        }
        //confidence
        Element confidence = new Element("confidence");
        confidence.setText(Double.toString(valueCorrespondence.getConfidence()));
        correspondence.addContent(confidence);
    }
}
