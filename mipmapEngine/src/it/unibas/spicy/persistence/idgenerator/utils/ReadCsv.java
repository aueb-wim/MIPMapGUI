/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.idgenerator.utils;

import au.com.bytecode.opencsv.CSVReader;
import it.unibas.spicy.persistence.idgenerator.generator.ColumnMatcherModel;
import it.unibas.spicy.persistence.idgenerator.generator.InputDataModel;
import java.io.FileReader;
import java.io.IOException;
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
            CSVReader reader = new CSVReader(new FileReader(FilenameUtils.separatorsToSystem(this.path)));
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
                    String value = nextLine[colNums.get(i)].trim();
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
            CSVReader reader = new CSVReader(new FileReader(FilenameUtils.separatorsToSystem(this.path)));
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
                System.out.println(nextLine[i]);
                System.out.println(generatedColumn.trim());
                if(nextLine[i].trim().equals(generatedColumn.trim())){
                    colNums.add(i);
                    colName.add(generatedColumn.trim());
                    break;
                }
            }
//            for(String s: colName){
//            System.out.println(s);
//            }
            
            while((nextLine = reader.readNext()) != null){
                InputDataModel idm = new InputDataModel();
                for (int i=0; i<colNums.size(); i++){             
                    String value = nextLine[colNums.get(i)].trim();
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
