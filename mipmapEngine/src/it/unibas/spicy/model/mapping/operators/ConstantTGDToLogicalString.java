/* Copyright 2015-2016 by the Athens University of Economics and Business (AUEB).

   This file is part of MIPMap.

   MIPMap is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MIPMap is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

//giannisk
package it.unibas.spicy.model.mapping.operators;

import it.unibas.spicy.model.correspondence.ISourceValue;
import it.unibas.spicy.model.mapping.ConstantFORule;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.model.paths.SetAlias;
import it.unibas.spicy.model.paths.VariableCorrespondence;
import it.unibas.spicy.model.paths.VariablePathExpression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConstantTGDToLogicalString {

    private TGDToLogicalStringUtility utility = new TGDToLogicalStringUtility();
    private boolean useSaveFormat;
    private boolean printSkolems;

    public ConstantTGDToLogicalString(boolean printSkolems, boolean useSaveFormat) {
        this.printSkolems = printSkolems;
        this.useSaveFormat = useSaveFormat;
    }

    public String toLogicalString(ConstantFORule rule, MappingTask mappingTask, String indent) {
        StringBuilder result = new StringBuilder();
        if (!useSaveFormat) {
            result.append(indent).append(rule.getId()).append(": \n");
        }
        result.append(" -> ");
        result.append(conclusionString(rule, mappingTask, indent));
        result.append(".\n");
        if (!useSaveFormat && printSkolems) {
            result.append("-----------------------------------\n");
        }
        return result.toString();
    }


    ////////////////////////////////   CONCLUSION   ////////////////////////////////////////////////
    public String conclusionString(ConstantFORule tgd, MappingTask mappingTask, String indent) {
        //find target paths that have a constant value correspondence
        List <VariablePathExpression> constantTargetPaths = new ArrayList<VariablePathExpression>();     
        HashMap<VariablePathExpression, ISourceValue> constantValueMap = new HashMap<VariablePathExpression, ISourceValue>();
        for (VariableCorrespondence varCor: tgd.getCoveredCorrespondences()){
            constantTargetPaths.add(varCor.getTargetPath());
            constantValueMap.put(varCor.getTargetPath(), varCor.getSourceValue());            
        }        
        
        StringBuilder result = new StringBuilder();

        //result.append("\n");
        for (int i = 0; i < tgd.getTargetView().getGenerators().size(); i++) {
            SetAlias targetVariable = tgd.getTargetView().getGenerators().get(i);
            //result.append(indent).append(utility.INDENT);
            if (!useSaveFormat) {
                result.append(utility.printAtomName(targetVariable, mappingTask.getTargetProxy().isNested()));
            } else {
                result.append(utility.printAtomNameForSaveFormat(targetVariable, mappingTask.getTargetProxy().isNested()));
            }
            List<VariablePathExpression> attributePaths = targetVariable.getFirstLevelAttributes(mappingTask.getTargetProxy().getIntermediateSchema());
            for (int j = 0; j < attributePaths.size(); j++) {                
                VariablePathExpression attributePath = attributePaths.get(j);              
                result.append(attributePath.getLastStep()).append(": ");
                result.append(constantValueMap.get(attributePath));
                if (j != attributePaths.size() - 1) {
                    result.append(", ");
                }
            }
            result.append(")");
            if (i != tgd.getTargetView().getGenerators().size() - 1) {
                result.append(", \n");
            }
        }
        return result.toString();
    }


}
