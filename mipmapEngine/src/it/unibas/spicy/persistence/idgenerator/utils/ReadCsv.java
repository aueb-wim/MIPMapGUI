/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.idgenerator.utils;

//import au.com.bytecode.opencsv.CSVReader;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import it.unibas.spicy.persistence.idgenerator.generator.ColumnMatcherModel;
import it.unibas.spicy.persistence.idgenerator.generator.InputDataModel;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author ioannisxar
 */
public class ReadCsv {
    private String path;
    
    public ReadCsv(String path){
        this.path = path;
    }
    
    public ArrayList<InputDataModel> readInputCsv(ArrayList<ColumnMatcherModel> cmmList){
        ArrayList<InputDataModel> inputData = new ArrayList<>();
        try{
            String[] nextLine;
//            CSVReader reader = new CSVReader(new FileReader(FilenameUtils.separatorsToSystem(this.path)));
            Reader r = new FileReader(FilenameUtils.separatorsToSystem(this.path));
            CSVReader reader = new CSVReaderBuilder(r).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).build();

            nextLine = reader.readNext();
            ArrayList<Integer> colNums = new ArrayList<>();
            ArrayList<String> colName = new ArrayList<>();
            for(ColumnMatcherModel cmm : cmmList){
                for(int i=0;i<nextLine.length;i++){
                    if(nextLine[i].equals(cmm.getSourceColumn())){
                        colNums.add(i);
                        colName.add(cmm.getSourceColumn());
                        break;
                    }
                }
            }
            
            while((nextLine = reader.readNext()) != null){
                InputDataModel idm = new InputDataModel();
                for (int i=0; i<colNums.size(); i++){  
                    //avenet 20170721
                    String value;
                    if ( nextLine[colNums.get(i)] != null)
                        value = nextLine[colNums.get(i)].trim();
                    else
                        value = null;
                    idm.addValue(value);
                    idm.addKey(colName.get(i));
                }
                inputData.add(idm);
            }
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return inputData;
    }
    
    public ArrayList<InputDataModel> readTargetCsv(ArrayList<ColumnMatcherModel> cmmList, String generatedColumn){
        ArrayList<InputDataModel> idmList = new ArrayList<>();
        try{
            String[] nextLine;
//            CSVReader reader = new CSVReader(new FileReader(FilenameUtils.separatorsToSystem(this.path)));
            Reader r = new FileReader(FilenameUtils.separatorsToSystem(this.path));
            CSVReader reader = new CSVReaderBuilder(r).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).build();

            nextLine = reader.readNext();
            ArrayList<Integer> colNums = new ArrayList<>();
            ArrayList<String> colName = new ArrayList<>();
            for(ColumnMatcherModel cmm : cmmList){
                for(int i=0;i<nextLine.length;i++){
                    if(nextLine[i].trim().equals(cmm.getTargetColumn().trim())){
                        colNums.add(i);
                        colName.add(cmm.getTargetColumn().trim());
                        break;
                    }
                }
            }        
            for(int i=0;i<nextLine.length;i++){
                if(nextLine[i].trim().equals(generatedColumn.trim())){
                    colNums.add(i);
                    colName.add(generatedColumn.trim());
                    break;
                }
            }

            while((nextLine = reader.readNext()) != null){
                InputDataModel idm = new InputDataModel();
                for (int i=0; i<colNums.size(); i++){ 
                    //avenet 20170721
                    String value;
                    if (nextLine[colNums.get(i)] != null)
                        value = nextLine[colNums.get(i)].trim();
                    else
                        value = null;
                    idm.addValue(value);
                    idm.addKey(colName.get(i));
                }
                idmList.add(idm);
            }
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return idmList;
    }
}
