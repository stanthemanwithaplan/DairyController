package uk.co.somestuff.newhouse.dairycontroller;

import org.json.JSONObject;

import javax.swing.*;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Assets {

    public Configuration getConfiguration() throws Exception {
        Configuration configuration = new Configuration();

        // Check/Load/Create Settings XML
        if (!new File("configuration.xml").exists()) {
            // File doesn't exist we should make one
            FileOutputStream fos = new FileOutputStream("configuration.xml");
            XMLEncoder encoder = new XMLEncoder(fos);
            encoder.writeObject(configuration);
            encoder.close();
            fos.close();
        } else {
            // File does exist we should read it
            FileInputStream fis = new FileInputStream("configuration.xml");
            XMLDecoder decoder = new XMLDecoder(fis);
            configuration = (Configuration) decoder.readObject();
            decoder.close();
            fis.close();
        }

        return configuration;
    }

    public void updateConfiguration(Configuration configuration) throws IOException {
        if (new File("configuration.xml").exists()) {
            // File doesn't exist we should make one
            FileOutputStream fos = new FileOutputStream("configuration.xml");
            XMLEncoder encoder = new XMLEncoder(fos);
            encoder.writeObject(configuration);
            encoder.close();
            fos.close();
        } else {
            throw new IOException("That file 'configuration.xml' couldn't be found");
        }
    }

    public List<Cow> getCattle() throws IOException {
        List<Cow> cattle = new ArrayList<Cow>();

        // Check/Load/Create Cattle XML
        if (!new File("cattleRecords.xml").exists()) {
            // File doesn't exist we should make one
            FileOutputStream fos = new FileOutputStream("cattleRecords.xml");
            XMLEncoder encoder = new XMLEncoder(fos);
            encoder.writeObject(cattle);
            encoder.close();
            fos.close();
        } else {
            // File does exist we should read it
            FileInputStream fis = new FileInputStream("cattleRecords.xml");
            XMLDecoder decoder = new XMLDecoder(fis);
            cattle = (List<Cow>) decoder.readObject();
            decoder.close();
            fis.close();
        }

        return cattle;
    }



}
