package uk.co.somestuff.newhouse.dairycontroller;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class SheddingCondition {
    private ObjectId _id;
    private String group;
    private boolean isGroup;
    private String responder1;
    private boolean isIndividual;
    private boolean isAM;
    private boolean isPM;
    private boolean isOneOff;
    private boolean isForever;
    private LocalDateTime start;
    private LocalDateTime finish;
    private boolean isExpired;

    public SheddingCondition() {}

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public void setResponder1(String responder1) {
        this.responder1 = responder1;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setGroup(boolean group) {
        this.isGroup = group;
    }

    public void setAM(boolean AM) {
        this.isAM = AM;
    }

    public void setForever(boolean forever) {
        this.isForever = forever;
    }

    public void setIndividual(boolean individual) {
        this.isIndividual = individual;
    }

    public void setOneOff(boolean oneOff) {
        this.isOneOff = oneOff;
    }

    public void setPM(boolean PM) {
        this.isPM = PM;
    }

    public void setFinish(LocalDateTime finish) {
        this.finish = finish;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public ObjectId get_id() {
        return this._id;
    }

    public void setExpired(boolean expired) {
        this.isExpired = expired;
    }

    public String getGroup() {
        return this.group;
    }

    public String getResponder1() {
        return this.responder1;
    }

    public boolean isAM() {
        return this.isAM;
    }

    public boolean isPM() {
        return this.isPM;
    }

    public boolean isGroup() {
        return this.isGroup;
    }

    public boolean isIndividual() {
        return this.isIndividual;
    }

    public boolean isForever() {
        return this.isForever;
    }

    public boolean isOneOff() {
        return this.isOneOff;
    }

    public LocalDateTime getStart() {
        return this.start;
    }

    public LocalDateTime getFinish() {
        return this.finish;
    }

    public boolean isExpired() {
        return this.isExpired;
    }
}
