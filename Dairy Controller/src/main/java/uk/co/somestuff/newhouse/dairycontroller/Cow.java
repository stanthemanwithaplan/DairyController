package uk.co.somestuff.newhouse.dairycontroller;

public class Cow {
    private String registration1;
    private String responder1;
    private String name;
    private String group;
    private String uid;

    public Cow() {}

    public void setGroup(String group) {
        this.group = group;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRegistration1(String registration1) {
        this.registration1 = registration1;
    }

    public void setResponder1(String responder1) {
        this.responder1 = responder1.strip();
        while (this.responder1.length() < 4) {
            this.responder1 = "0" + this.responder1;
        }
    }

    public void setUid(String uid) {
        this.uid = uid.strip();
        while (this.uid.length() < 4) {
            this.uid = "0" + this.uid;
        }
    }

    public String getRegistration1() {
        return this.registration1;
    }

    public String getResponder1() {
        return this.responder1;
    }

    public String getName() {
        return this.name;
    }

    public String getGroup() {
        return this.group;
    }

    public String getUid() {
        return this.uid;
    }

    public String toString() {
        return "Cow(" + this.uid + ", " + this.responder1 + ", " + this.registration1 + ", " + this.group + ")";
    }
}
