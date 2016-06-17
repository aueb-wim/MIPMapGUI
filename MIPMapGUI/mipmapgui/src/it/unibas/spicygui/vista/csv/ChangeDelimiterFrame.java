package it.unibas.spicygui.vista.csv;

import it.unibas.spicygui.Costanti;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.JPanel;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class ChangeDelimiterFrame extends javax.swing.JDialog{
    
    private String sourceDelimiter;
    private String sourceQuotes;
    private String targetQuotes="";
    
    public ChangeDelimiterFrame() {
        Image imageDefault = ImageUtilities.loadImage(Costanti.ICONA_MIPMAP, true);
        setIconImage(imageDefault);
        setTitle(org.openide.util.NbBundle.getMessage(Costanti.class, Costanti.CHANGE_DELIMITER_TITLE));
        //setSize(300, 200);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel panel = new ChangeDelimiterPanel(this);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);  
        pack();
        setLocationRelativeTo(null);
        setVisible(true); 
    }
    
    public void setSourceDelimiter(String sourceDelimiter){
        this.sourceDelimiter = sourceDelimiter;
    }
    
    public void setSourceQuotes(String sourceQuotes){
        this.sourceQuotes = sourceQuotes;
    }
    
    public void setTargetQuotes(String targetQuotes){
        this.targetQuotes = targetQuotes;
    }
    
    public String getSourceDelimiter(){
        return this.sourceDelimiter;
    }
    
    public String getSourceQuotes(){
        return this.sourceQuotes;
    }
    
    public boolean getTargetQuotes(){
        return (this.targetQuotes.equals(NbBundle.getMessage(Costanti.class,Costanti.DOUBLE_QUOTES_OPTION)));
    }
    
}
