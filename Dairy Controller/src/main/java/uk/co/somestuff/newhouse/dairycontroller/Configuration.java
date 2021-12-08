package uk.co.somestuff.newhouse.dairycontroller;

import java.time.LocalDateTime;
import java.util.List;

public class Configuration {
    private LocalDateTime lastCattleRecordsUpdated;
    private String communicationPort;
    private String milkingAM;
    private String milkingPM;
    private List<SheddingCondition> sheddingRequirements;

    public LocalDateTime getLastCattleRecordsUpdated() {
        return this.lastCattleRecordsUpdated;
    }

    public String getCommunicationPort() {
        return this.communicationPort;
    }

    public String getMilkingAM() {
        return this.milkingAM;
    }

    public String getMilkingPM() {
        return this.milkingPM;
    }

    public List<SheddingCondition> getSheddingRequirements() {
        return this.sheddingRequirements;
    }

    public void setCommunicationPort(String communicationPort) {
        this.communicationPort = communicationPort;
    }

    public void setLastCattleRecordsUpdated(LocalDateTime lastCattleRecordsUpdated) {
        this.lastCattleRecordsUpdated = lastCattleRecordsUpdated;
    }

    public void setMilkingAM(String milkingAM) {
        this.milkingAM = milkingAM;
    }

    public void setMilkingPM(String milkingPM) {
        this.milkingPM = milkingPM;
    }

    public void setSheddingRequirements(List<SheddingCondition> sheddingRequirements) {
        this.sheddingRequirements = sheddingRequirements;
    }
}