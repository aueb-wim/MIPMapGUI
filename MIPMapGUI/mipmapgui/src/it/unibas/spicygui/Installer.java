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
 
package it.unibas.spicygui;

import it.unibas.spicygui.commons.Modello;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.theme.LightGray;

import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.DAOHandleDB;
import it.unibas.spicy.utility.SpicyEngineConstants;
import it.unibas.spicygui.commons.LastActionBean;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.controllo.Scenarios;
import it.unibas.spicygui.vista.Vista;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.WriterAppender;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;
import sun.audio.*;

public class Installer extends ModuleInstall {

    public static final String LOG4J_CONFIGURATION_FILE = "/conf/spicyGUI-log4j.properties";
    public static String postgres_db_conf_file; //= "postgresdb.properties"//kostisk: going from constant to variable...
//    private ActionExitApplication actionExitApplication = new ActionExitApplication();
    private boolean close;
    
    
    @Override
    public void close() {

        if (close) {
            super.close();
        }
    }

    @Override
    public boolean closing() {

        //TODO codice commentato per il bug di nb platform 6.1
        //        boolean esito = actionExitApplication.canExit();
        //        if (esito) {
        //            Utility.closeOutputWindow();
        //return Utility.closeAllTopComponent();

        return checkForTrayBar();
    }

    private boolean checkForTrayBar() {
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Costanti.class, Costanti.CHECK_FOR_MINIMIZE), DialogDescriptor.YES_NO_OPTION);
        notifyDescriptor.setOptions(new Object[]{NbBundle.getMessage(Costanti.class, Costanti.CLOSE_BUTTON), NbBundle.getMessage(Costanti.class, Costanti.TRAY_BUTTON), NbBundle.getMessage(Costanti.class, Costanti.CANCEL_BUTTON)});
        DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (notifyDescriptor.getValue().equals(NbBundle.getMessage(Costanti.class, Costanti.TRAY_BUTTON))) {
            if (SystemTray.isSupported()) {
                menageTrayIcon();
            }
            return false;
        } else if (notifyDescriptor.getValue().equals(NbBundle.getMessage(Costanti.class, Costanti.CANCEL_BUTTON))) {
            return false;
        }

        return Utility.closeAllTopComponent();
    }

    private void menageTrayIcon() {
        Image imageTray = ImageUtilities.loadImage(Costanti.ICONA_MIPMAP, true);
        final TrayIcon trayIcon = new TrayIcon(imageTray, Costanti.MIPMAP_NAME);
//        trayIcon.addActionListener(new ActionListener() {
//
//
//
//            public void actionPerformed(ActionEvent e) {
//                System.out.println();
//                WindowManager.getDefault().getMainWindow().setVisible(true);
//                SystemTray.getSystemTray().remove(trayIcon);
//            }
//        });

        trayIcon.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println(e.getClickCount());
                if (e.getButton() == e.BUTTON1) {
                    WindowManager.getDefault().getMainWindow().setVisible(true);
                    SystemTray.getSystemTray().remove(trayIcon);
                } else {
                    int scenarioCounter;
                    if (((Scenarios)Lookup.getDefault().lookup(Modello.class).getBean(Costanti.SCENARIOS)) != null) {
                     scenarioCounter = ((Scenarios)Lookup.getDefault().lookup(Modello.class).getBean(Costanti.SCENARIOS)).getListaSceneri().size();
                    } else {
                        scenarioCounter = 0;
                    }
                    String text = "";
                    if (scenarioCounter > 1 || scenarioCounter == 0) {
                        text = NbBundle.getMessage(Costanti.class, Costanti.INFORMATION_ON_TRAY_START) + " " +
                            scenarioCounter + " " + NbBundle.getMessage(Costanti.class, Costanti.INFORMATION_ON_TRAY_END_P);
                    } else {
                        text = NbBundle.getMessage(Costanti.class, Costanti.INFORMATION_ON_TRAY_START) + " " +
                            scenarioCounter + " " + NbBundle.getMessage(Costanti.class, Costanti.INFORMATION_ON_TRAY_END_S);
                    }
                    trayIcon.displayMessage(Costanti.MIPMAP_NAME,text,TrayIcon.MessageType.INFO);
                }
            }

        });
        try {
            SystemTray.getSystemTray().add(trayIcon);
            WindowManager.getDefault().getMainWindow().setVisible(false);
        } catch (Exception e) {
        }
    }
    
    private void playSound() {
      try
      {
        InputStream inputStream = getClass().getResourceAsStream(Costanti.SOUND_FILE);
        AudioStream audioStream = new AudioStream(inputStream);
        AudioPlayer.player.start(audioStream);
      }
      catch (Exception e)
      {
        Logger.getLogger(Installer.class.getName()).severe("Unable to load audio file");
        } 
    }
    

    @Override
    public void restored() {
        
        //playSound();        
        System.setProperty("netbeans.buildnumber", "");
        java.util.Date date= new java.util.Date();
        System.out.println("MIPMap starting to load: "+new Timestamp(date.getTime()));
        setLookAndFeel();
        configureLog4j();        
        configuraObservable();
        configureFavoriteWindow();
        //kostisk
        new DBPropertiesChooserJFrame(this).setVisible(true);
        //this.postgres_db_conf_file=adbpc.getConfigurationFile();
        //giannisk commented by kostisk, they run once the user selects the properties db file in ActionDBPropertiesChooser
        //configureDatabaseProperties();
        //createDB();
        //java.util.Date date2= new java.util.Date();
        //System.out.println("MIPMap loaded: "+new Timestamp(date2.getTime()));
    }
    
    public void setPostgresDbConfFile(String fullPathFilename)
    {   this.postgres_db_conf_file = fullPathFilename;}
    
    public void createDB(){
        DAOHandleDB daoCreateDB = new DAOHandleDB();
        try {
            daoCreateDB.createNewDatabase();
        } catch (DAOException ex) {
            System.err.println("*** Something went wrong while creating the auxiliary db for the mapping-tasks I guess..? ***");
            Exceptions.printStackTrace(ex);
        }
        System.out.println("*** DB created ***");
    }
    
    private File getClassLocation() {
        Class cls = this.getClass();
        ClassLoader classLoader = cls.getClassLoader();
        
        if(classLoader==null) { 
            classLoader=ClassLoader.getSystemClassLoader(); 
        }

        URL url;
        if((url=classLoader.getResource(cls.getName().replace('.','/')+".class"))==null) {
            return null;
        }

        String extUrl;
        try {
            extUrl = URLDecoder.decode(url.toExternalForm(), "UTF-8");
            String lowerUrl = extUrl.toLowerCase();
            while(lowerUrl.startsWith("jar:") || lowerUrl.startsWith("file:/")) {
                if(lowerUrl.startsWith("jar:")) {
                    if(lowerUrl.indexOf("!/")!=-1) { 
                        extUrl=extUrl.substring(4,(extUrl.indexOf("!/"))); 
                    }     // strip encapsulating "jar:" and "!/..." from JAR url
                    else { 
                        extUrl=extUrl.substring(4); } // strip encapsulating "jar:"
                    }
                if(lowerUrl.startsWith("file:/")) {
                    extUrl=extUrl.substring(6);  // strip encapsulating "file:/"
                    if(!extUrl.startsWith("/")) { 
                        extUrl=("/"+extUrl); 
                    }
                    while(extUrl.length()>1 && extUrl.charAt(1)=='/') { 
                        extUrl=extUrl.substring(1); 
                    }
                }
                lowerUrl=extUrl.toLowerCase();
            }
            File path=new File(extUrl);
            if(lowerUrl.endsWith(".class") || (lowerUrl.endsWith(".jar"))) { 
                path=path.getParentFile(); 
            }
            if(path.exists()) { 
                path=path.getAbsoluteFile(); 
            }
            return path;
        } catch (UnsupportedEncodingException ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.NEW_ERROR) + " : " + ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
            Logger.getLogger(Installer.class.getName()).severe("UnsupportedEncodingException caught for installation url");
            return null;
        }
    }
    
    //giannisk
    //reads the properties file with database configuration and sets the appropriate variables
    public void configureDatabaseProperties() {
        Properties dbproperties = new Properties();            
        //TO RUN AS AN APPLICATION:
        //uncomment the following block and the "else" block
        //and comment the first two lines right after the block
        //// //comment for IDE
        /*File installationPath0 = this.getClassLocation();
        System.out.println("INST:"+installationPath0);
        File installationPath = installationPath0.getParentFile();
        File propertyFile = new File(installationPath.getAbsolutePath() + File.separator + POSTGRESDB_CONFIGURATION_FILE);
        InputStream stream = null; 
        if (propertyFile.exists()){
            try {
                stream = new FileInputStream(propertyFile);*/
        //// 
            //InputStream stream = Installer.class.getResourceAsStream(postgres_db_conf_file); //comment for application     
        System.out.println("DB Properties file is "+this.postgres_db_conf_file) ;
        InputStream stream=null;//changes by kostisk
        try {
            stream = new FileInputStream(new File (postgres_db_conf_file));
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
            //if (stream == null){System.out.println("---> Why stream is null here (1)...?? <---");}
            try {                                                                                            //comment for application
                dbproperties.load(stream);
                SpicyEngineConstants.setDatabaseParameters(dbproperties.getProperty("driver"),dbproperties.getProperty("uri"),
                        dbproperties.getProperty("username"), dbproperties.getProperty("password"),dbproperties.getProperty("mappingTask-DatabaseName"));
            } catch (IOException ex) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.NEW_ERROR) + " : " + ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
                Logger.getLogger(Installer.class.getName()).severe("Unable to load database configuration file");
            } finally {
                try {
                    //if (stream == null){System.out.println("---> Why stream is null here (2)...?? <---");}
                    stream.close();
                } catch (IOException ex) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.NEW_ERROR) + " : " + ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
                    Logger.getLogger(Installer.class.getName()).severe("Unable to close database configuration file");
                } 
            }  
        //// //comment for IDE
        /*}else{
           DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("File " + propertyFile.toString() + " not found", DialogDescriptor.ERROR_MESSAGE)); 
        }*/
        ////
    }
  
    private void configuraObservable() {
        LastActionBean lastActionBean = new LastActionBean();
        Lookup.getDefault().lookup(Modello.class).putBean(Costanti.LAST_ACTION_BEAN, lastActionBean);
    }

    private void configureFavoriteWindow() {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();

        FileObject fav = fs.findResource("Favorites/");

        File[] roots = File.listRoots();

        for (File file : roots) {

            try {

                if (file.exists()) {
                    FileObject shadow = fav.createFolder(file.getAbsolutePath().replaceAll("[:.?\"\'<>|]", "_").replaceAll("[\\\\/]", " ") + ".shadow");

                    shadow.setAttribute("originalFile", file.toURI().toString());
                }

            } catch (IOException ex) {
                // Do nothing
            }

        }

    }

    private void configureLog4j() {
        System.setProperty("log4j.defaultInitOverride", "true");
        Properties configurazione = caricaProperties();

//        if (configurazione == null) {
//            configurazione = new Properties();
//            configurazione.setProperty("log4j.rootLogger", "INFO, stdout");
//            configurazione.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
//            configurazione.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
//            configurazione.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%d %-5p %c - %n%m%n");
//        }
        PropertyConfigurator.configure(configurazione);
        createWriterAppender();
    }

    public void createWriterAppender() {
        PatternLayout layout = new PatternLayout("%m%n");
        WriterAppender writerAppender = new WriterAppender();
        InputOutput io = IOProvider.getDefault().getIO(Costanti.FLUSSO_SPICY, false);
        writerAppender.setWriter(io.getOut());
        writerAppender.setName("Output Window Appender");
        writerAppender.setLayout(layout);
        org.apache.log4j.Logger.getRootLogger().addAppender(writerAppender);
    }

    private Properties caricaProperties() {
        try {
            Properties configuration = new Properties();
            InputStream stream = Installer.class.getResourceAsStream(LOG4J_CONFIGURATION_FILE);
            configuration.load(stream);
            return configuration;
        } catch (IOException ex) {
            Logger.getLogger(Installer.class.getName()).severe("Unable to load log4j configuration file");
            return null;
        }
    }

    private void setLookAndFeel() {
        /*try {
            if ((Utilities.getOperatingSystem() & Utilities.OS_LINUX) != 0) {
                Plastic3DLookAndFeel.setPlasticTheme(new LightGray());
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            }
        } catch (UnsupportedLookAndFeelException ex) {
            Exceptions.printStackTrace(ex);
        }*/
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DBPropertiesChooserJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DBPropertiesChooserJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DBPropertiesChooserJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DBPropertiesChooserJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public void addFolderToClassPath(String folderPath) {
        try {
            File f = new File(folderPath);
            URL u = f.toURI().toURL();
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class urlClass = URLClassLoader.class;
            Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(urlClassLoader, new Object[]{u});
        } catch (Throwable throwable) {
            System.out.println("ERROR");
        }
    }
}
