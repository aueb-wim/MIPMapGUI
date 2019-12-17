/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.idgenerator.utils;

import au.com.bytecode.opencsv.CSVWriter;
import it.unibas.spicy.persistence.idgenerator.generator.InputDataModel;
import java.io.BufferedWriter;
import java.io.File;
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
//        CSVWriter csvWriter;
        BufferedWriter bwriter;
        try {
            bwriter = new BufferedWriter(new FileWriter(FilenameUtils.separatorsToSystem(pathToExport)));
//            csvWriter = new CSVWriter(new FileWriter(FilenameUtils.separatorsToSystem(pathToExport)), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
//            csvWriter.writeNext(header, false);
            int line = 1;
            String str = "";
            for ( int i = 0 ; i < header.length ; i ++) {
                str += "\"" + header[i].trim() + "\",";
            }
            str = str.substring(0, str.length()-1);
            str += "\n";
            for(InputDataModel output: targetValues){
                String row = "";
                for(String s: output.getValue()){
                    if (s != null )
                        row += "\"" + s + "\",";
                    else 
                        row += ",";
//                    row = row.replace("\"", "");
                }
//                csvWriter.writeNext(row.substring(0, row.length()-1).split(","),false);
                row = row.substring(0, row.length()-1);
                str += row + "\n";
            }
            bwriter.write(str);
            bwriter.close();
//            csvWriter.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        
    }
    
}