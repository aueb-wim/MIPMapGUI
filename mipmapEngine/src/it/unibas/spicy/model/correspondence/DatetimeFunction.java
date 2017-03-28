package it.unibas.spicy.model.correspondence;

import it.unibas.spicy.utility.SpicyEngineConstants;

public class DatetimeFunction extends AbstractSourceValue {

    
    private String type;
    @Override
    public String toString() {
       return SpicyEngineConstants.SOURCEVALUE_DATETIME_FUNCTION;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof DatetimeFunction)) {
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
}
    