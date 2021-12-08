package uk.co.somestuff.newhouse.dairycontroller;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.tinylog.Logger;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Shedding {

    public List<SheddingCondition> toRemove = new ArrayList<SheddingCondition>();

    public class SheddingResponse {
        private boolean isShed;
        private boolean isCowKnown;
        private Cow cow;
        public SheddingResponse(Cow cow, boolean isShed, boolean isCowKnown) {
            this.isShed = isShed;
            this.cow = cow;
            this.isCowKnown = isCowKnown;
        }

        public Cow getCow() {
            return this.cow;
        }

        public boolean isShed() {
            return this.isShed;
        }

        public boolean isCowKnown() {
            return this.isCowKnown;
        }
    }

    private MongoClient mongoClient = null;
    public Shedding(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }


    // shedding with mongo

    public SheddingResponse isShed(String responder) {
        Cow foundCow = new Cow();
        boolean isShed = false;
        boolean isCowKnown = false;

        DB mongoDatabase = mongoClient.getDB("newhousedairycontroller");
        DBCollection cattle = mongoDatabase.getCollection("cattle");

        BasicDBObject query = new BasicDBObject();
        query.put("responder1", responder);
        DBCursor cursor = cattle.find(query);
        while(cursor.hasNext()) {
            DBObject obj = cursor.next();
            try {
                foundCow.setName((String) obj.get("name"));
                foundCow.setGroup((String) obj.get("group"));
                foundCow.setRegistration1((String) obj.get("registration1"));
                foundCow.setResponder1((String) obj.get("responder1"));
                foundCow.setUid((String) obj.get("cow"));
            }
            catch (Exception e) {
                foundCow.setResponder1(responder);
                e.printStackTrace();
            }
            isCowKnown = true;
            break;
        }

        // We should look through the conditions first by the group and if none are found or is expired by the individual cow

        if (!isCowKnown) {
            foundCow.setResponder1(responder);
            return new SheddingResponse(foundCow, false, false);
        }

        // TODO: treat responders as numbers not strings, same with cow number

        DBCollection sheddingConditions = mongoDatabase.getCollection("sheddingConditions");

        BasicDBObject query1 = new BasicDBObject();
        BasicDBObject query2 = new BasicDBObject();
        query1.put("group", foundCow.getGroup());
        query2.put("responder1", foundCow.getResponder1());
        DBCursor cursor1 = sheddingConditions.find(query1);
        DBCursor cursor2 = sheddingConditions.find(query2);

        while (cursor1.hasNext()) {
            DBObject obj1 = cursor1.next();
            SheddingCondition sheddingCondition = new SheddingCondition();
            sheddingCondition.set_id((ObjectId) obj1.get("_id"));
            sheddingCondition.setGroup((String) obj1.get("group"));
            sheddingCondition.setGroup((boolean) obj1.get("isGroup"));
            sheddingCondition.setResponder1((String) obj1.get("responder1"));
            sheddingCondition.setIndividual((boolean) obj1.get("isIndividual"));
            sheddingCondition.setAM((boolean) obj1.get("isAM"));
            sheddingCondition.setPM((boolean) obj1.get("isPM"));
            sheddingCondition.setOneOff((boolean) obj1.get("isOneOff"));
            sheddingCondition.setForever((boolean) obj1.get("isForever"));
            sheddingCondition.setExpired((boolean) obj1.get("isExpired"));

            if (!sheddingCondition.isGroup()) {
                continue;
            }

            if (!sheddingCondition.isForever()) {
                sheddingCondition.setStart(LocalDateTime.parse((String) cursor.next().get("start"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                sheddingCondition.setFinish(LocalDateTime.parse((String) cursor.next().get("finish"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            if (!sheddingCondition.isForever()) {
                if (sheddingCondition.getStart().isAfter(LocalDateTime.now()) || sheddingCondition.getFinish().isBefore(LocalDateTime.now())) {
                    sheddingCondition.setExpired(true);
                } else {
                    sheddingCondition.setExpired(false);
                }
            } else {
                sheddingCondition.setExpired(false);
            }

            if (sheddingCondition.isExpired()) {
                toRemove.add(sheddingCondition);
                continue;
            }

            if (sheddingCondition.isOneOff()) {
                if (LocalTime.now().getHour() < 12 && sheddingCondition.isAM()) {
                    isShed = true;
                    toRemove.add(sheddingCondition);
                    continue;
                }
                if (LocalTime.now().getHour() > 12 && sheddingCondition.isPM()) {
                    isShed = true;
                    toRemove.add(sheddingCondition);
                    continue;
                }
            }

            if (!sheddingCondition.isExpired()) {
                if (LocalTime.now().getHour() < 12 && sheddingCondition.isAM()) {
                    isShed = true;
                }
                if (LocalTime.now().getHour() > 12 && sheddingCondition.isPM()) {
                    isShed = true;
                }
            }
        }

        while (cursor2.hasNext()) {
            DBObject obj2 = cursor2.next();
            SheddingCondition sheddingCondition = new SheddingCondition();
            sheddingCondition.set_id((ObjectId) obj2.get("_id"));
            sheddingCondition.setGroup((String) obj2.get("group"));
            sheddingCondition.setGroup((boolean) obj2.get("isGroup"));
            sheddingCondition.setResponder1((String) obj2.get("responder1"));
            sheddingCondition.setIndividual((boolean) obj2.get("isIndividual"));
            sheddingCondition.setAM((boolean) obj2.get("isAM"));
            sheddingCondition.setPM((boolean) obj2.get("isPM"));
            sheddingCondition.setOneOff((boolean) obj2.get("isOneOff"));
            sheddingCondition.setForever((boolean) obj2.get("isForever"));
            sheddingCondition.setExpired((boolean) obj2.get("isExpired"));

            if (!sheddingCondition.isIndividual()) {
                continue;
            }

            if (!sheddingCondition.isForever()) {
                sheddingCondition.setStart(LocalDateTime.parse((String) cursor.next().get("start"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                sheddingCondition.setFinish(LocalDateTime.parse((String) cursor.next().get("finish"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            if (!sheddingCondition.isForever()) {
                if (sheddingCondition.getStart().isAfter(LocalDateTime.now()) || sheddingCondition.getFinish().isBefore(LocalDateTime.now())) {
                    sheddingCondition.setExpired(true);
                } else {
                    sheddingCondition.setExpired(false);
                }
            } else {
                sheddingCondition.setExpired(false);
            }

            if (sheddingCondition.isExpired()) {
                toRemove.add(sheddingCondition);
                continue;
            }

            if (sheddingCondition.isOneOff()) {
                if (LocalTime.now().getHour() < 12 && sheddingCondition.isAM()) {
                    isShed = true;
                    toRemove.add(sheddingCondition);
                    continue;
                }
                if (LocalTime.now().getHour() > 12 && sheddingCondition.isPM()) {
                    isShed = true;
                    toRemove.add(sheddingCondition);
                    continue;
                }
            }

            if (!sheddingCondition.isExpired()) {
                if (LocalTime.now().getHour() < 12 && sheddingCondition.isAM()) {
                    isShed = true;
                }
                if (LocalTime.now().getHour() > 12 && sheddingCondition.isPM()) {
                    isShed = true;
                }
            }
        }

        return new SheddingResponse(foundCow, isShed, isCowKnown);
    }

}