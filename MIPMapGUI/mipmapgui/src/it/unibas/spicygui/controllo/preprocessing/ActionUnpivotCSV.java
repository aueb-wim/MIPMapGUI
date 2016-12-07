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
    along with MIPMap.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.unibas.spicygui.controllo.preprocessing;

import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.csv.UnpivotCSVDAO;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.vista.Vista;
import it.unibas.spicygui.vista.csv.UnpivotCsvMainFrame;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.JFileChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

//giannisk
public class ActionUnpivotCSV extends CallableSystemAction implements Observer {
    
    private static Log logger = LogFactory.getLog(ActionUnpivotCSV.class);
    private Modello modello;
    private LastActionBean lastActionBean;
    Vista vista;
    
    public ActionUnpivotCSV() {
        executeInjection();
        this.putValue(SHORT_DESCRIPTION, NbBundle.getMessage(Costanti.class, Costanti.ACTION_UNPIVOT_CSV));
        registraAzione();
    }
    
    @Override
    public void performAction() {
        this.executeInjection();
        JFileChooser chooser = vista.getFileChooserApriCSV();
        File file;
        int returnVal = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            //file extension
            String ext = file.getPath().substring(file.getPath().lastIndexOf(".")+1);
            if (ext.equalsIgnoreCase("csv")){
                UnpivotCSVDAO daoUnpivot = new UnpivotCSVDAO();
                try {          
                    String[] columnNames = daoUnpivot.getCsvTableColumns(file);
                    UnpivotCsvMainFrame unpivotMain = new UnpivotCsvMainFrame(columnNames);
                    List<String> colNames = unpivotMain.getColNames();
                    List<String> keepColNames = unpivotMain.getKeepColNames();
                    String newColName = unpivotMain.getNewColName();                    
                    if (!colNames.isEmpty() && newColName!=null && !newColName.equals("")){
                        try{
                            daoUnpivot.unpivotTable(keepColNames, colNames, newColName, file);                            
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.EXPORT_UNPIVOTED_OK)+file.getParent()));
                        }
                        catch (DAOException ex) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
                            logger.error(ex);
                        }
                        catch (SQLException ex) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
                            logger.error(ex);
                        }
                    }                    
                } catch (IOException ex) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage() , DialogDescriptor.ERROR_MESSAGE));
                }
            }
            else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.MESSAGE_WRONG_FILE_INPUT)+": \""+ext+"\"", DialogDescriptor.ERROR_MESSAGE));
            }
        }
    }
    
    private void registraAzione() {
        lastActionBean = (LastActionBean) modello.getBean(Costanti.LAST_ACTION_BEAN);
        lastActionBean.addObserver(this);
    }
    
    private void executeInjection() {
        if (this.modello == null) {
            this.modello = Lookup.getDefault().lookup(Modello.class);
        }
        if (this.vista == null) {
            this.vista = Lookup.getDefault().lookup(Vista.class);
        }
    }
    
    public void update(Observable o, Object stato) {}

    @Override
    public String getName() {
        return NbBundle.getMessage(Costanti.class, Costanti.ACTION_UNPIVOT_CSV);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /*@Override
    protected String iconResource() {
        return Costanti.ICONA_GENERATE_QUERY;
    }*/

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
}
