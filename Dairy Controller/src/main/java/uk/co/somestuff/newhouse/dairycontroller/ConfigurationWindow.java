package uk.co.somestuff.newhouse.dairycontroller;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class ConfigurationWindow extends JFrame {

    interface FinishEventHandler {
        public void handleFinish();
    }

    public void onFinish(FinishEventHandler finishHandler) {
        finishHandler.handleFinish();
    }

    public ConfigurationWindow(boolean initial, Configuration configuration) {

        class FinishHandler implements FinishEventHandler {
            public void handleFinish() {
                dispose();
            }
        }

        ArrayList<String> availablePorts = new ArrayList<String>();

        for (int i = 0; i < SerialPort.getCommPorts().length; i++) {
            availablePorts.add(SerialPort.getCommPorts()[i].getSystemPortName());
        }

        JSpinner amTimeSpinner = new JSpinner( new SpinnerDateModel() );
        JSpinner.DateEditor amTimeEditor = new JSpinner.DateEditor(amTimeSpinner, "HH:mm:ss");
        amTimeSpinner.setEditor(amTimeEditor);
        amTimeSpinner.setValue(new Date());
        JSpinner pmTimeSpinner = new JSpinner( new SpinnerDateModel() );
        JSpinner.DateEditor pmTimeEditor = new JSpinner.DateEditor(pmTimeSpinner, "HH:mm:ss");
        pmTimeSpinner.setEditor(pmTimeEditor);
        pmTimeSpinner.setValue(new Date());

        JComboBox comPortCombo = new JComboBox(availablePorts.toArray());
        comPortCombo.setPreferredSize(new Dimension(150, 20));

        JLabel comPortLabel = new JLabel("COM Port");
        JLabel milkingAmLabel = new JLabel("Milking Start (AM)");
        JLabel milkingPmLabel = new JLabel("Milking Start (PM)");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;

        c.insets = new Insets(10,20,0,40);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        mainPanel.add(comPortLabel, c);
        c.gridx = 1;
        c.weightx = 0;
        mainPanel.add(comPortCombo, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        mainPanel.add(milkingAmLabel, c);
        c.gridx = 1;
        c.weightx = 0;
        mainPanel.add(amTimeSpinner, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        mainPanel.add(milkingPmLabel, c);
        c.gridx = 1;
        c.weightx = 0;
        mainPanel.add(pmTimeSpinner, c);

        JPanel bottomPanel = new JPanel();
        JButton save = new JButton("Save");
        JButton close = new JButton("Close");

        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Now we wanna save the settings, call save settings or something?

                Date amDate = (Date) amTimeSpinner.getValue();
                SimpleDateFormat amFormat = new SimpleDateFormat("HH:mm:ss");
                String amTime = amFormat.format(amDate);

                Date pmDate = (Date) pmTimeSpinner.getValue();
                SimpleDateFormat pmFormat = new SimpleDateFormat("HH:mm:ss");
                String pmTime = pmFormat.format(pmDate);

                LocalTime am = LocalTime.parse(amTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
                LocalTime pm = LocalTime.parse(pmTime, DateTimeFormatter.ofPattern("HH:mm:ss"));

                configuration.setCommunicationPort((String) comPortCombo.getSelectedItem());
                configuration.setMilkingAM(am.toString());
                configuration.setMilkingPM(pm.toString());

                try {
                    new Assets().updateConfiguration(configuration);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "An error occurred while saving to the configuration file, please try again", "Oops", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                JOptionPane.showMessageDialog(null, "Done that for you there", "Brilliant", JOptionPane.INFORMATION_MESSAGE);
                onFinish(new FinishHandler());
                dispose();
            }
        });

        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (initial) {
                    dispose();
                    System.exit(0);
                } else {
                    dispose();
                }
            }
        });

        bottomPanel.add(save);
        bottomPanel.add(close);

        JPanel lineEnd = new JPanel();
        lineEnd.setLayout(new BorderLayout());
        lineEnd.add(bottomPanel, BorderLayout.LINE_END);

        JPanel lastPanel = new JPanel();
        lastPanel.setLayout(new BorderLayout());
        lastPanel.add(mainPanel, BorderLayout.CENTER);
        lastPanel.add(lineEnd, BorderLayout.SOUTH);

        getContentPane().add(lastPanel);

        setSize(400, 200);
        setLocationRelativeTo(null);
        setResizable(true);
        setUndecorated(false);
        setTitle("Configuration");
        setVisible(true);
        if (initial) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
    }
}
