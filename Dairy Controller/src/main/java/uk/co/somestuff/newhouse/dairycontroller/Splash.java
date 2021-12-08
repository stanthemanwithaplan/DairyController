package uk.co.somestuff.newhouse.dairycontroller;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Splash extends JFrame {

    final Properties properties = new Properties();

    interface FinishEventHandler {
        public void handleFinish();
    }

    public void onFinish(FinishEventHandler finishHandler) {
        finishHandler.handleFinish();
    }

    public Splash(ArrayList<SplashRunnable> runnableList) {

        class FinishHandler implements FinishEventHandler {
            public void handleFinish() {
                dispose();
            }
        }

        try {
            properties.load(Splash.class.getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource("splash.png");

        JPanel basePanel = new JPanel();
        basePanel.setLayout(new BorderLayout());

        ImageIcon imageicon = new ImageIcon(url);
        JLabel label = new JLabel(imageicon);
        basePanel.add(label, BorderLayout.NORTH);

        JPanel versionPanel = new JPanel();
        versionPanel.setLayout(new BoxLayout(versionPanel, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("Dairy Controller");
        name.setBorder(BorderFactory.createEmptyBorder(5,10,0,0));
        name.setFont(new Font("Calibri", Font.BOLD, 14));
        JLabel bVersion = new JLabel("Build " + properties.getProperty("version"));
        bVersion.setBorder(BorderFactory.createEmptyBorder(5,11,0,0));
        versionPanel.add(name);
        versionPanel.add(bVersion);

        JPanel informationPanel = new JPanel();
        informationPanel.setLayout(new BoxLayout(informationPanel, BoxLayout.Y_AXIS));
        JLabel progress = new JLabel("Loading...");
        progress.setBorder(BorderFactory.createEmptyBorder(28,20,0,0));
        informationPanel.add(progress);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(versionPanel, BorderLayout.WEST);
        panel.add(informationPanel, BorderLayout.CENTER);

        basePanel.add(versionPanel, BorderLayout.LINE_START);
        basePanel.add(informationPanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(basePanel);

        setSize(420, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setTitle("Splash");
        setVisible(true);

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < runnableList.size(); i++) {
            SplashRunnable currentRunnable = runnableList.get(i);
            progress.setText(currentRunnable.getName());
            progress.repaint();
            currentRunnable.run();
        }

        this.onFinish(new FinishHandler());

    }
}
