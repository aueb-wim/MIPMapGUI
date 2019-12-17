package it.unibas.spicy.persistence.json;

import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.csv.DAOCsv;
import it.unibas.spicy.persistence.csv.ExportCSVInstances;
import java.util.HashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//gianniks
public class DAOJson {
    
    private static Log logger = LogFactory.getLog(DAOCsv.class);
    private static final String TUPLE_SUFFIX = "Tuple";
    private static final String TRANSLATED_INSTANCE_SUFFIX = "-translatedInstances";
    private static final String PK_CONSTRAINT_VIOLATED_INSTANCE_SUFFIX ="-PKConstraintViolatedInstances";
    
    public void exportTranslatedJsonInstances(MappingTask mappingTask, String directoryPath, int scenarioNo) throws DAOException {
        try{
            ExportJsonInstances exporter = new ExportJsonInstances();        
            exporter.exportJsonInstances(mappingTask, directoryPath, TRANSLATED_INSTANCE_SUFFIX, scenarioNo);
        } catch (Throwable ex) {
            logger.error(ex);
            throw new DAOException(ex.getMessage());
        }
    }
    
    public void exportPKConstraintJsoninstances(MappingTask mappingTask, HashSet<String> tableNames, String directoryPath, int scenarioNo) throws DAOException {
        try{
            ExportJsonInstances exporter = new ExportJsonInstances();        
            exporter.exportPKConstraintJsonInstances(mappingTask, directoryPath, tableNames, PK_CONSTRAINT_VIOLATED_INSTANCE_SUFFIX, scenarioNo);
        } catch (Throwable ex) {
            logger.error(ex);
            throw new DAOException(ex.getMessage());
        }
    }
}
