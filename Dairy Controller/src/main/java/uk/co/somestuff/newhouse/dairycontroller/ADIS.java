package uk.co.somestuff.newhouse.dairycontroller;

import com.mongodb.*;
import org.bson.types.ObjectId;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ADIS {

    private ArrayList<Cow> cattle = new ArrayList<Cow>();
    private MongoClient mongoClient = null;

    private static class Status {
        public static String DN = "DN";
        public static String VN = "VN";
        public static String TN = "TN";
        public static String ZN = "ZN";
    }

    private static class DefinitionType {
        public static int Responder1 = 2023;
        public static int Name = 2003;
        public static int Group = 2021;
        public static int Cow = 2001;
        public static int Registration1 = 2002;
    }

    private class Definition {
        private int definition;
        private int width;

        public Definition(int definition, int width) {
            this.definition = definition;
            this.width = width;
        }

        public int getDefinition() {
            return this.definition;
        }

        public int getWidth() {
            return this.width;
        }
    }

    public ADIS(MongoClient mongoClient) { this.mongoClient = mongoClient; }

    public ADIS(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));

        ArrayList<Definition> definitions = new ArrayList<Definition>();

        try {
            String line = br.readLine();
            while (line != null) {

                String lineStatus = line.substring(0, 2);
                String lineData = line.substring(2);

                if (lineStatus.equals(Status.DN)) {

                    String data = lineData.substring(10); // Removing the initial 10 '0's

                    while (data.length() > 0) {
                        String workingDefinition = data.substring(0, 6);
                        String definitionType = workingDefinition.substring(0, 4);
                        Definition definition = new Definition(Integer.parseInt(definitionType), Integer.parseInt(workingDefinition.substring(4)));
                        definitions.add(definition);
                        data = data.substring(7);
                        if (data.length() >= 4) {
                            data = data.substring(4);
                        }
                    }
                } else if (lineStatus.equals(Status.VN)) {
                    String data = lineData.substring(6);

                    Cow cow = new Cow();

                    while (data.length() >= 6) {
                        for (int i = 0; i < definitions.size(); i++) {
                            if (definitions.get(i).definition == DefinitionType.Responder1) {
                                cow.setResponder1(data.substring(0, definitions.get(i).width));
                            } else if (definitions.get(i).definition == DefinitionType.Name) {
                                cow.setName(data.substring(0, definitions.get(i).width));
                            } else if (definitions.get(i).definition == DefinitionType.Group) {
                                cow.setGroup(data.substring(0, definitions.get(i).width));
                            } else if (definitions.get(i).definition == DefinitionType.Cow) {
                                cow.setUid(data.substring(0, definitions.get(i).width));
                            } else if (definitions.get(i).definition == DefinitionType.Registration1) {
                                cow.setRegistration1(data.substring(0, definitions.get(i).width));
                            }

                            data = data.substring(definitions.get(i).width);
                        }
                    }

                    this.cattle.add(cow);

                }
                line = br.readLine();
            }
        } finally {
            br.close();
        }
    }

    public void retrieveADSData() throws Exception {
        try {
            Process process = new ProcessBuilder("C:\\DairyPln\\DPDataExchange.exe","-AKT", "DPDataExchange3.ads", "newhousedairycontroller.ads", "/a", "/e", "/t").start();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        // We need to talk to DPDataExchange.exe then ADIS to load new cattle, we should do this everytime we load the program, then just before milking as well
        ADIS adis = null;
        try {
            adis = new ADIS("C:/DairyPln/newhousedairycontroller.ads");
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        // TODO: (testing) Should probably delete all cow records first because of the responder and uid changing

        DB mongoDatabase = mongoClient.getDB("newhousedairycontroller");
        DBCollection cattle = mongoDatabase.getCollection("cattle");
        cattle.drop();
        cattle = mongoDatabase.createCollection("cattle", new BasicDBObject());

        for (int i = 0; i < adis.getCattle().size(); i++) {
            Cow localCow = adis.getCattle().get(i);

            BasicDBObject query = new BasicDBObject();
            query.put("registration1", localCow.getRegistration1().strip());

            DBCursor cursor = cattle.find(query);

            if (!cursor.hasNext()) {
                BasicDBObject cow = new BasicDBObject("_id", new ObjectId());
                cow
                        .append("responder1", localCow.getResponder1().strip())
                        .append("name", localCow.getName().strip())
                        .append("group", localCow.getGroup().strip())
                        .append("registration1", localCow.getRegistration1().strip())
                        .append("cow", localCow.getUid().strip());
                cattle.insert(cow);
            }
        }

    }

    private ArrayList<Cow> getCattle() {
        return this.cattle;
    }
}
