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
 
package it.unibas.spicy.model.correspondence;

import it.unibas.spicy.utility.SpicyEngineConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateFunction extends AbstractSourceValue {

    private String type, sequence;
    @Override
    public String toString() {
       return SpicyEngineConstants.SOURCEVALUE_DATE_FUNCTION;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof DateFunction)) {
            return false;
        }
        return true;
    }
    
    @Override
    public void setType(String type){
        this.type = type;
    }
    
    @Override
    public String getType(){
        return this.type;
    }
    
    @Override
    public void setSequence(String sequence){
        this.sequence = sequence;
    }
    
    @Override
    public String getSequence(){
        return this.sequence;
    }
}
