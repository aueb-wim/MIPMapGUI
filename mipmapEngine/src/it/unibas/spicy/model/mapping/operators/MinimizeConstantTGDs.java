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
package it.unibas.spicy.model.mapping.operators;

import it.unibas.spicy.model.mapping.ConstantFORule;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.model.mapping.rewriting.operators.CheckTGDHomomorphism;
import it.unibas.spicy.model.paths.VariableCorrespondence;
import it.unibas.spicy.utility.SpicyEngineUtility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MinimizeConstantTGDs {

    private static Log logger = LogFactory.getLog(MinimizeTGDs.class);
    private GenerateCandidateSTTGDs tgdGenerator = new GenerateCandidateSTTGDs();
    private CheckTGDHomomorphism subsumptionChecker = new CheckTGDHomomorphism();

    public List<ConstantFORule> generateAndMinimizeTGDs(MappingTask mappingTask) {
        List<ConstantFORule> tgds = tgdGenerator.generateCandidateConstantTGDs(mappingTask);
        if (logger.isDebugEnabled()) logger.debug("*************************Candidate Constant TGDs: " + tgds.size() + "\n" + SpicyEngineUtility.printCollection(tgds));
        removeEquivalentMappings(tgds);
        if (logger.isDebugEnabled()) logger.debug("*************************After equivalence removals: " + tgds.size() + "\n" + SpicyEngineUtility.printCollection(tgds));
        removeFullySubsumedTGDs(tgds, mappingTask);
        if (logger.isDebugEnabled()) logger.debug("*************************After full subsumption removals: " + tgds.size() + "\n" + SpicyEngineUtility.printCollection(tgds));
        return tgds;
    }

    private void removeEquivalentMappings(List<ConstantFORule> candidateTGDs) {
        Map<List<VariableCorrespondence>, List<ConstantFORule>> equivalenceClasses = findEquivalenceClasses(candidateTGDs);
        for (List<ConstantFORule> equivalenceClass : equivalenceClasses.values()) {
            ////Collections.sort(equivalenceClass);
            for (int i = 1; i < equivalenceClass.size(); i++) {
                candidateTGDs.remove(equivalenceClass.get(i));
            }
        }
    }

    private Map<List<VariableCorrespondence>, List<ConstantFORule>> findEquivalenceClasses(List<ConstantFORule> candidateTGDs) {
        Map<List<VariableCorrespondence>, List<ConstantFORule>> equivalenceClasses = new HashMap<List<VariableCorrespondence>, List<ConstantFORule>>();
        for (ConstantFORule tgd : candidateTGDs) {
//            if (SpicyEngineUtility.hasSelfJoins(tgd)) {
//                continue;
//            }
            List<ConstantFORule> equivalenceClass = equivalenceClasses.get(tgd.getCoveredCorrespondences());
            if (equivalenceClass == null) {
                equivalenceClass = new ArrayList<ConstantFORule>();
                equivalenceClasses.put(tgd.getCoveredCorrespondences(), equivalenceClass);
            }
            equivalenceClass.add(tgd);
        }
        if (logger.isTraceEnabled()) logger.trace("Equivalence classes: " + equivalenceClasses);
        return equivalenceClasses;
    }

    private void removeFullySubsumedTGDs(List<ConstantFORule> tgds, MappingTask mappingTask) {
        if (logger.isDebugEnabled()) logger.trace("*************Removing fully subsumed tgds from tgd list: " + SpicyEngineUtility.printCollection(tgds));
        for (Iterator<ConstantFORule> it = tgds.iterator(); it.hasNext();) {
            ConstantFORule tgd = it.next();
            ConstantFORule fatherTGD = existsFatherWithEqualSourceView(tgd, tgds, mappingTask);
            if (fatherTGD != null) {
                if (logger.isDebugEnabled()) logger.trace("Removing tgd: " + tgd + "because of father: " + fatherTGD);
                it.remove();
            }
        }
    }

    private ConstantFORule existsFatherWithEqualSourceView(ConstantFORule tgd, List<ConstantFORule> tgds, MappingTask mappingTask) {
        for (ConstantFORule otherTGD : tgds) {
            if (SpicyEngineUtility.hasSelfJoins(tgd) || SpicyEngineUtility.hasSelfJoins(otherTGD)) {
                continue;
            }
            if (tgd.equals(otherTGD)) {
                continue;
            }
            if (subsumptionChecker.subsumes(otherTGD, tgd, mappingTask.getTargetProxy(), mappingTask)) {
                return otherTGD;
            }
        }
        return null;
    }
    
}
