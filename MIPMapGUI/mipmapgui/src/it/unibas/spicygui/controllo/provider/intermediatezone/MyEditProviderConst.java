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
 
package it.unibas.spicygui.controllo.provider.intermediatezone;

import it.unibas.spicy.model.exceptions.ExpressionSyntaxException;
import it.unibas.spicy.persistence.AccessConfiguration;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.relational.IConnectionFactory;
import it.unibas.spicy.persistence.relational.SimpleDbConnectionFactory;
import it.unibas.spicy.utility.SpicyEngineConstants;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetInterConst;
import it.unibas.spicygui.widget.ConstantWidget;
import it.unibas.spicygui.controllo.mapping.operators.CreateCorrespondencesMappingTask;
import it.unibas.spicygui.controllo.mapping.operators.ReviewCorrespondences;
import it.unibas.spicygui.Utility;
import it.unibas.spicygui.vista.intermediatezone.ConstantDialog;
import it.unibas.spicygui.vista.intermediatezone.GetConstantFromDbDialog;
import it.unibas.spicygui.widget.caratteristiche.ConnectionInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class MyEditProviderConst implements EditProvider {

    private ReviewCorrespondences review = new ReviewCorrespondences();
    private CreateCorrespondencesMappingTask creator = new CreateCorrespondencesMappingTask();
    private CaratteristicheWidgetInterConst caratteristiche;
    private ConstantDialog dialog;
    private GetConstantFromDbDialog dbDialog = null;
    private ConstantWidget rootNode;
    private Log logger = LogFactory.getLog(MyEditProviderConst.class);

    public MyEditProviderConst(CaratteristicheWidgetInterConst caratteristiche) {
        this.caratteristiche = caratteristiche;
        this.dialog = new ConstantDialog(WindowManager.getDefault().getMainWindow(), caratteristiche, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    
    private void getOffsetButton() {
        this.dialog.getOffsetButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!dialog.getTextSequenceName().getText().trim().equals("")){
                    if(dbDialog == null){
                        dbDialog = new GetConstantFromDbDialog(WindowManager.getDefault().getMainWindow(), caratteristiche, true);
                        dbDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        dbDialog.setVisible(true);
                    } else {
                        dbDialog.setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "Please insert a sequence name!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    public void edit(Widget widget) {
        if (!(caratteristiche.getTipoFunzione() || caratteristiche.getTipoNumero() || caratteristiche.getTipoStringa())) {
            caratteristiche.setTipoStringa(true);
            caratteristiche.getFormValidation().setTextFieldState(true);
            caratteristiche.getFormValidation().setButtonState(false);
        }
        if(dbDialog == null){
            getOffsetButton();
        }
        rootNode = (ConstantWidget) widget;
        if (!caratteristiche.getTgdView()) {
            dialog.clean();
            CaratteristicheWidgetInterConst oldCaratteristiche = caratteristiche.clone();
            boolean oldButtonState = dialog.getFormValidation().getButtonState();
            dialog.setVisible(true);
            try {
                String type = verificaDati();
                
                if (dialog.getReturnStatus() == ConstantDialog.RET_CANCEL) {
                    ripristina(oldButtonState, oldCaratteristiche);
                } else if (caratteristiche.getConnectionList().size() > 0) {
                    updateCorrespondences(oldButtonState, oldCaratteristiche, type);
                }
            } catch (ExpressionSyntaxException e) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.SYNTAX_WARNING) + " : " + e, DialogDescriptor.WARNING_MESSAGE));
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Costanti.class, Costanti.SYNTAX_WARNING));
                ripristina(oldButtonState, oldCaratteristiche);
            } catch (DAOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void ripristina(boolean oldButtonState, CaratteristicheWidgetInterConst oldCaratteristiche) {
        dialog.getFormValidation().setButtonState(oldButtonState);
        caratteristiche.setCostante(oldCaratteristiche.getCostante());
        caratteristiche.setTipoFunzione(oldCaratteristiche.getTipoFunzione());
        caratteristiche.setTipoNumero(oldCaratteristiche.getTipoNumero());
        caratteristiche.setTipoStringa(oldCaratteristiche.getTipoStringa());
    }

    private void updateCorrespondences(boolean oldButtonState, CaratteristicheWidgetInterConst oldCaratteristiche, String type) {
        ConnectionInfo connectionInfoExtern = null;
        try {
            for (ConnectionInfo connectionInfo : caratteristiche.getConnectionList()) {
                connectionInfoExtern = connectionInfo;
                review.removeCorrespondence(connectionInfo.getValueCorrespondence());
                creator.setGetIdType(type);
                creator.createCorrespondenceWithSourceValue((LayerWidget) rootNode.getParentWidget(), rootNode, connectionInfo.getTargetWidget(), connectionInfo);
            }
        } catch (ExpressionSyntaxException ese) {
            creator.undo(connectionInfoExtern.getValueCorrespondence());
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.SYNTAX_WARNING) + " : " + ese.getMessage(), DialogDescriptor.WARNING_MESSAGE));
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Costanti.class, Costanti.SYNTAX_WARNING));
            ripristina(oldButtonState, oldCaratteristiche);
        }
    }

    public String verificaDati() throws DAOException, SQLException {
        String type = null;
        if (this.dialog.getJRadioButtonFunction().isSelected()) {
            if(this.dialog.getJComboBoxFunction().getSelectedItem().toString().equalsIgnoreCase("newId()")){
                String sequence = this.dialog.getTextSequenceName().getText().trim();
                if(SpicyEngineConstants.GET_ID_FROM_DB.get(sequence) == null)
                    SpicyEngineConstants.TEMP_DB_PROPERTIES = SpicyEngineConstants.GET_ID_FROM_DB.get(sequence);
                              
                caratteristiche.setCostante(this.dialog.getJComboBoxFunction().getSelectedItem() + "_" + sequence);
                
                if(this.dialog.getJRadioButtonConstant().isSelected()){
                    SpicyEngineConstants.OFFSET_MAPPING.put(sequence, this.dialog.getOffsetText().getText().trim());
                    type = "constant";
                } else if(this.dialog.getJRadioButtonDatabase().isSelected()){
                    if(SpicyEngineConstants.TEMP_DB_PROPERTIES != null){
                        Connection connection = connectToDb();
                        Statement statement = connection.createStatement();
                        if (SpicyEngineConstants.TEMP_DB_PROPERTIES.getFunction().equalsIgnoreCase("max")){
                            statement.execute("SELECT MAX(\""+ SpicyEngineConstants.TEMP_DB_PROPERTIES.getColumn() 
                                    +"\") FROM \"" + SpicyEngineConstants.TEMP_DB_PROPERTIES.getTable() + "\";");
                        }
                        ResultSet rs = statement.getResultSet();
                        if (rs.next()) {
                            SpicyEngineConstants.OFFSET = String.valueOf(rs.getInt(1));
                        } else {
                            SpicyEngineConstants.OFFSET = "0";
                        }
                        SpicyEngineConstants.OFFSET_MAPPING.put(sequence,SpicyEngineConstants.OFFSET);
                        SpicyEngineConstants.GET_ID_FROM_DB.put(sequence, SpicyEngineConstants.TEMP_DB_PROPERTIES);
                        SpicyEngineConstants.TEMP_DB_PROPERTIES = null;
                        type = "getId()";
                    } else {
                        if(dialog.getReturnStatus() == ConstantDialog.RET_OK){
                            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), 
                                "Please setup the database configuration!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    if(dialog.getReturnStatus() == ConstantDialog.RET_OK){
                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), 
                            "Please select offset source input!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if(this.dialog.getJComboBoxFunction().getSelectedItem().toString().equalsIgnoreCase("date()")){
                type = "date";
                caratteristiche.setCostante(this.dialog.getJComboBoxFunction().getSelectedItem());
            } else {
                type = "datetime";
                caratteristiche.setCostante(this.dialog.getJComboBoxFunction().getSelectedItem());
            }
        }
        if (this.dialog.getJRadioButtonNumber().isSelected()) {
            type = "number";
            Double.parseDouble((String) caratteristiche.getCostante());
        }
        if (this.dialog.getJRadioButtonString().isSelected()) {
            type = "string";
            String valoreCostante = (String) caratteristiche.getCostante();
            caratteristiche.setCostante(Utility.sostituisciVirgolette(valoreCostante));
        }
        caratteristiche.setType(type);
        return type;
    }
    
    private Connection connectToDb() throws DAOException {
        AccessConfiguration accessConfiguration = new AccessConfiguration();
        accessConfiguration.setDriver(SpicyEngineConstants.TEMP_DB_PROPERTIES.getDriver());
        accessConfiguration.setUri(SpicyEngineConstants.TEMP_DB_PROPERTIES.getUri());
        if (!SpicyEngineConstants.TEMP_DB_PROPERTIES.getSchema().equals("")) {
            accessConfiguration.setSchemaName(SpicyEngineConstants.TEMP_DB_PROPERTIES.getSchema());
        }
        accessConfiguration.setLogin(SpicyEngineConstants.TEMP_DB_PROPERTIES.getLogin());
        accessConfiguration.setPassword(SpicyEngineConstants.TEMP_DB_PROPERTIES.getPassword());
        IConnectionFactory connectionFactory = new SimpleDbConnectionFactory();
        return connectionFactory.getConnection(accessConfiguration);
    }
    
}
