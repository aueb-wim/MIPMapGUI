/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicy.persistence.idgenerator.generator;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author ioannisxar
 */
public class InputDataModel {
    
    private int id;
    private ArrayList<String> values;
    private ArrayList<String> keys;
    public InputDataModel(){
        this.id = -1;
        this.values = new ArrayList<>();
        this.keys = new ArrayList<>();
    }
    
    public void addValue(String val){
        this.values.add(val);
    }
    
    public void addKey(String val){
        this.keys.add(val);
    }
    
    public ArrayList<String> getValue(){
        return this.values;
    }
    
    public ArrayList<String> getKey(){
        return this.keys;
    }
    
    public int size(){
        return this.values.size();
    }
    
    public int getId(){
        if (size()>0){
            id = Integer.valueOf(this.values.get(size()-1));
        }
        return id;
    }
    
    public static int getMaxValue(ArrayList<InputDataModel> targetValues){
        int max = 0;
        ArrayList<Integer> l = new ArrayList<>();
        for(int i=0;i<targetValues.size();i++){      
            l.add(targetValues.get(i).getId());
        }
        if (l.size()>0){
            max = Collections.max(l);
        }
        return max;
    }
    
    public boolean exists(ArrayList<InputDataModel> targetValues){
        for(InputDataModel target: targetValues){
            int matches = 0;
            for(int i=0; i<this.values.size();i++){
                if(this.values.get(i).trim().equals(target.getValue().get(i).trim())){
                    matches++;
                }
                if(matches==this.values.size()){
                    return true;
                }
            }
        }
        return false;
    }
}