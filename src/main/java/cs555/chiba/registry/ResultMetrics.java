package cs555.chiba.registry;

import java.util.Date;
import java.util.UUID;

public class ResultMetrics {

    private UUID requestId;
    private int totalNumberOfHops;
    private int totalNumberOfDevices;
    private Date timeQueryStarted;
    private Date timeOfLastReceivedResultMessage;

    public ResultMetrics(UUID requestId, int totalNumberOfHops, int totalNumberOfDevices) {
        this.requestId = requestId;
        this.totalNumberOfHops = totalNumberOfHops;
        this.totalNumberOfDevices = totalNumberOfDevices;
        this.timeQueryStarted = new Date();
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public int getTotalNumberOfHops() {
        return totalNumberOfHops;
    }

    public void addTotalNumberOfHops(int totalNumberOfHops) {
        this.totalNumberOfHops += totalNumberOfHops;
    }

    public int getTotalNumberOfDevices() {
        return totalNumberOfDevices;
    }

    public void addTotalNumberOfDevices(int totalNumberOfDevices) {
        this.totalNumberOfDevices += totalNumberOfDevices;
    }

    public Date getTimeQueryStarted() {
        return timeQueryStarted;
    }

    public Date getTimeOfLastReceivedResultMessage() {
        return timeOfLastReceivedResultMessage;
    }

    public void setTimeOfLastReceivedResultMessage() {
        this.timeOfLastReceivedResultMessage = new Date();
    }

    public static ResultMetrics merge(ResultMetrics rm1, ResultMetrics rm2) {
        rm1.addTotalNumberOfHops(rm2.getTotalNumberOfHops());
        rm1.addTotalNumberOfDevices(rm2.getTotalNumberOfDevices());
        rm1.setTimeOfLastReceivedResultMessage();
        return rm1;
    }
}