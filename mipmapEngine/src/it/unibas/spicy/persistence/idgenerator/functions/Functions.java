/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.idgenerator.functions;

/**
 *
 * @author ioannisxar
 */
public class Functions {
    
    public static String split(String value, String delimeter, int part){
        return value.split(delimeter.trim().replace("\"", ""))[part];
    }
    
}
