/*
    Copyright (C) 2007-2011  Database Group - Universita' della Basilicata
    Giansalvatore Mecca - giansalvatore.mecca@unibas.it
    Salvatore Raunich - salrau@gmail.com
    Marcello Buoncristiano - marcello.buoncristiano@yahoo.it

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
 
package it.unibas.spicygui.controllo.tree;

import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.vista.JLayeredPaneCorrespondences;
import it.unibas.spicygui.vista.MappingTaskTopComponent;
import it.unibas.spicygui.widget.FunctionalDependencyWidget;
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetInterFunctionalDep;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class ActionShowHideFunctionalDependencies extends AbstractAction implements Observer {

    private static Log logger = LogFactory.getLog(ActionShowHideFunctionalDependencies.class);
    private Modello modello = null;
    private LastActionBean lastActionBean;

    public ActionShowHideFunctionalDependencies() {
        this.executeInjection();
        this.putValue(NAME, NbBundle.getMessage(Costanti.class, Costanti.ACTION_SHOW_HIDE_FUNCTIONAL_DEPENDENCIES));
        this.putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(Costanti.ICONA_SHOW_HIDE_FUNCTIONAL_DEPENDENCIES)));
        this.putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_F));
        this.setEnabled(false);
        registraAzione();
    }

    public void actionPerformed(ActionEvent e) {
        //queste sono le azioni sullo scenario di riferimento quindi bisogna mettere in modello lo scenraio corrente scelto
        //come si fa in netbeans
        Scenario scenario = (Scenario) this.modello.getBean(Costanti.CURRENT_SCENARIO);
        MappingTaskTopComponent topComponent = scenario.getMappingTaskTopComponent();
        JLayeredPaneCorrespondences jLayeredPane = topComponent.getJLayeredPane();
        LayerWidget connectionLayer = jLayeredPane.getGlassPane().getConnectionLayer();
        for (Widget widget : connectionLayer.getChildren()) {
            ConnectionWidget connectionWidget = (ConnectionWidget) widget;
            if (connectionWidget.getSourceAnchor().getRelatedWidget() instanceof FunctionalDependencyWidget ||
                connectionWidget.getTargetAnchor().getRelatedWidget() instanceof FunctionalDependencyWidget) {
                connectionWidget.setVisible(!connectionWidget.isVisible());
            }
        }
        LayerWidget mainLayer = jLayeredPane.getGlassPane().getMainLayer();
        for (Widget widget : mainLayer.getChildren()) {
            if (widget instanceof FunctionalDependencyWidget) {
                CaratteristicheWidgetInterFunctionalDep caratteristiche = (CaratteristicheWidgetInterFunctionalDep) mainLayer.getChildConstraint(widget);
                caratteristiche.getWidgetBarra().setVisible(!caratteristiche.getWidgetBarra().isVisible());
                widget.setVisible(!widget.isVisible());
            }
        }

        jLayeredPane.moveToFront();
        jLayeredPane.getGlassPane().getScene().validate();
    }

    private void executeInjection() {
        if (this.modello == null) {
            this.modello = Lookup.getDefault().lookup(Modello.class);
        }
    }

    public void update(Observable o, Object stato) {
        if (stato.equals(LastActionBean.CLOSE) || (stato.equals(LastActionBean.NO_SCENARIO_SELECTED))) {
            this.setEnabled(false);
        } else {
            this.setEnabled(true);
        }
    }

    private void registraAzione() {
        lastActionBean = (LastActionBean) modello.getBean(Costanti.LAST_ACTION_BEAN);
        lastActionBean.addObserver(this);
    }
}
