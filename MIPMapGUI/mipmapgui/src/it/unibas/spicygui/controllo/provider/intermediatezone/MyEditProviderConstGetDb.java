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
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetInterConst;
import it.unibas.spicygui.vista.intermediatezone.GetConstantFromDbDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.windows.WindowManager;

public class MyEditProviderConstGetDb implements EditProvider {

    private CaratteristicheWidgetInterConst caratteristiche;
    private GetConstantFromDbDialog dbDialog;

    public MyEditProviderConstGetDb(CaratteristicheWidgetInterConst caratteristiche) {
        this.caratteristiche = caratteristiche;
        dbDialog = new GetConstantFromDbDialog(WindowManager.getDefault().getMainWindow(), caratteristiche, true);
        dbDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dbDialog.setVisible(true);
        
    }

    @Override
    public void edit(Widget widget) {
           
    }










    
}
