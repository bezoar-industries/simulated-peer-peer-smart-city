package cs555.chiba.registry;

import java.util.Date;
import java.util.UUID;

public class ResultMetrics {

    private UUID requestId;
    private long totalNumberOfHops;
    private long totalNumberOfDevices;
    private long totalNumberOfDevicesWithMetric;
    private Date timeQueryStarted;
    private Date timeOfLastReceivedResultMessage;
    private String typeOfQuery;

    public ResultMetrics(UUID requestId, long totalNumberOfHops, long totalNumberOfDevices, long
            totalNumberOfDevicesWithMetric, String typeOfQuery) {
        this.requestId = requestId;
        this.totalNumberOfHops = totalNumberOfHops;
        this.totalNumberOfDevices = totalNumberOfDevices;
        this.timeQueryStarted = new Date();
        this.totalNumberOfDevicesWithMetric = totalNumberOfDevicesWithMetric;
        this.typeOfQuery = typeOfQuery;
    }

    public synchronized void addResult(int totalNumberOfHops, int totalNumberOfDevices, int
            totalNumberOfDevicesWithMetric) {
        this.totalNumberOfHops += totalNumberOfHops;
        this.totalNumberOfDevices += totalNumberOfDevices;
        this.totalNumberOfDevicesWithMetric += totalNumberOfDevicesWithMetric;
        this.timeOfLastReceivedResultMessage = new Date();
    }

    @Override
    public String toString() {
        return "ResultMetrics{" +
                "totalNumberOfHops=" + totalNumberOfHops +
                ", totalNumberOfDevices=" + totalNumberOfDevices +
                ", totalNumberOfDevicesWithMetric=" + totalNumberOfDevicesWithMetric +
                ", timeQueryStarted=" + timeQueryStarted +
                ", timeOfLastReceivedResultMessage=" + timeOfLastReceivedResultMessage +
                ", typeOfQuery='" + typeOfQuery + '\'' +
                '}';
    }
}
