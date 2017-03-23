/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.idgenerator.utils;

import au.com.bytecode.opencsv.CSVWriter;
import it.unibas.spicy.persistence.idgenerator.generator.InputDataModel;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author ioannisxar
 */
public class ExportCsv {
    private ArrayList<InputDataModel> targetValues;
    private String pathToExport;
    private String[] header;
    
    public ExportCsv(String pathToExport, ArrayList<InputDataModel> targetValues, String[] header){
        this.pathToExport = pathToExport;
        this.targetValues = targetValues;
        this.header = header;
    }
    
    public void performAction(){
        CSVWriter csvWriter;
        try {
            csvWriter = new CSVWriter(new FileWriter(FilenameUtils.separatorsToSystem(pathToExport)), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
            csvWriter.writeNext(header, false);
            for(InputDataModel output: targetValues){
                String row = "";
                for(String s: output.getValue()){
                    row += s + ",";
                }
                csvWriter.writeNext(row.substring(0, row.length()-1).split(","));
            }
            csvWriter.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        
    }
    
}
