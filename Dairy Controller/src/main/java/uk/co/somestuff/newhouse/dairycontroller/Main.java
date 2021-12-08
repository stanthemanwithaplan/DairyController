package uk.co.somestuff.newhouse.dairycontroller;

import com.fazecast.jSerialComm.SerialPort;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.tinylog.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    // TODO: Use a local mongo server? and get a copy of dairyplan to play around with

    public static Configuration configuration = null;
    protected static SerialPort serialPort = null;
    protected static Com thread = new Com(null);
    private static MongoClient mongoClient = null;
    protected static DB newhouseDairyDB = null;

    private static class ConnectToMongo extends SplashRunnable {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void run() {
            try {
                mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            } catch (UnknownHostException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred while communicating with mongoDB, please try again", "Oops", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
            DB newhouseDairyDB = mongoClient.getDB("newhousedairycontroller");
        }
    }

    private static class LoadConfiguration extends SplashRunnable {

        @Override
        public String getName() {
            return "Loading Configuration...";
        }

        @Override
        public void run() {
            try {
                // We want to wait until the configuration has been loaded before we carry on
                configuration = new Assets().getConfiguration();
            } catch (Exception e) {
                // A fatal error has occurred, we cannot continue, we need to close the program
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred while trying to locate the configuration file, please try again", "Oops", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
    }

    private static class UpdateCattleRecords extends SplashRunnable {

        @Override
        public String getName() {
            return "Updating Cattle Records...";
        }

        @Override
        public void run() {

            // TODO: Clear all cattle records before updating because of a weird error with uniform where it changes the responder

            ADIS adis = new ADIS(mongoClient);
            try {
                adis.retrieveADSData();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred while talking to DPDataExchange.exe, please try again", "Oops", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
    }

    // The following SplashRunnable are run if it's not a first time run.

    private static class CloseDPProcessControl extends SplashRunnable {

        @Override
        public String getName() {
            return "Closing DPProcessControl.exe...";
        }

        @Override
        public void run() {

            /*if (configuration.getCommunicationPort() != null && configuration.getMilkingAM() != null && configuration.getMilkingPM() != null) {
                Runtime rt = Runtime.getRuntime();
                try {
                    rt.exec("taskkill /F /IM DPProcessControl.exe");
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "An error occurred while closing DPProcessControl, please try again", "Oops", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            }*/
        }
    }

    private static class OpenComPort extends SplashRunnable {

        @Override
        public String getName() {
            return "Opening '" + configuration.getCommunicationPort() + "'...";
        }

        @Override
        public void run() {
            if (configuration.getCommunicationPort() != null && configuration.getMilkingAM() != null && configuration.getMilkingPM() != null) {
                serialPort = SerialPort.getCommPort(configuration.getCommunicationPort());

                boolean serialPortExists = false;
                boolean serialPortOpen  = false;

                try {
                    if (SerialPort.getCommPort(configuration.getCommunicationPort()).isOpen()) {
                        serialPortOpen = true;
                    }
                    serialPortExists = true;
                } catch (Exception e ) {
                    e.printStackTrace();
                    serialPortExists = false;
                }

                if (!serialPortExists) {
                    JOptionPane.showMessageDialog(null, "Serial port '" + configuration.getCommunicationPort() + "' doesn't exist, please try again", "Oops", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                if (serialPortOpen) {
                    JOptionPane.showMessageDialog(null, "Serial port '" + configuration.getCommunicationPort() + "' is currently unavailable, please try again", "Oops", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }

                serialPort.setBaudRate(4800);
                serialPort.setParity(SerialPort.EVEN_PARITY);
                serialPort.setNumStopBits(SerialPort.TWO_STOP_BITS);
                serialPort.setNumDataBits(7);
                try {
                    serialPort.openPort();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Serial port '" + configuration.getCommunicationPort() + "' opening error, please try again", "Oops", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            }
        }
    }

    private static class StartListener extends SplashRunnable {

        @Override
        public String getName() {
            return "Starting listener on '" + configuration.getCommunicationPort() + "'...";
        }

        @Override
        public void run() {
            if (configuration.getCommunicationPort() != null && configuration.getMilkingAM() != null && configuration.getMilkingPM() != null) {
                thread = new Com(new Shedding(mongoClient));
                thread.start();
            }
        }

    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Display the splash screen

        ArrayList<SplashRunnable> splashFunctions = new ArrayList<SplashRunnable>();
        splashFunctions.add(new ConnectToMongo());
        splashFunctions.add(new LoadConfiguration());
        splashFunctions.add(new UpdateCattleRecords());
        splashFunctions.add(new CloseDPProcessControl());
        splashFunctions.add(new OpenComPort());
        splashFunctions.add(new StartListener());
        Splash splash = new Splash(splashFunctions);
        splash.onFinish(new Splash.FinishEventHandler() {
            @Override
            public void handleFinish() {
                // This is when the splash has finished, and we can load the main program
                System.out.println("Running the program");

                if (configuration.getCommunicationPort() == null || configuration.getMilkingAM() == null || configuration.getMilkingPM() == null) {
                    // Ahh there's no communication port, that makes me think this might be our first run, so we need to be set up
                    // Open the configuration window
                    ConfigurationWindow configurationWindow = new ConfigurationWindow(true, configuration);
                    // Now we really want to open the splash screen again to close DPProcessControl.exe and take control over the COM port
                    configurationWindow.onFinish(new ConfigurationWindow.FinishEventHandler() {
                        @Override
                        public void handleFinish() {
                            ArrayList<SplashRunnable> splashFunctions = new ArrayList<SplashRunnable>();
                            splashFunctions.add(new CloseDPProcessControl());
                            splashFunctions.add(new OpenComPort());
                            splashFunctions.add(new StartListener());
                            Splash splash = new Splash(splashFunctions);
                            splash.onFinish(new Splash.FinishEventHandler() {
                                @Override
                                public void handleFinish() {
                                    System.out.println("Configuration complete");
                                    // Open the main window
                                    mainProgram();
                                }
                            });
                        }
                    });
                } else {
                    mainProgram();
                }
            }
        });
    }

    public static void mainProgram() {
        TrayIcon trayIcon = null;
        // Start tray icon and window
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();

            // create a popup menu
            PopupMenu popup = new PopupMenu();

            // Close Item in the menu item
            MenuItem closeItem = new MenuItem("Close");
            ActionListener closeItemListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };
            closeItem.addActionListener(closeItemListener);
            popup.add(closeItem);

            // Loads the tray icon from resources and loads the tray menu
            BufferedImage trayIconImage = null;
            try {
                trayIconImage = ImageIO.read(Main.class.getResource("/cow.gif"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
            trayIcon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH), "Moooo", popup);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Can't add the tray item");
        }

        System.out.println("Program started");

        // load the C# nice interface?
    }
}
