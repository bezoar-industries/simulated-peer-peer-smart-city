package cs555.chiba.registry;

import java.util.Date;
import java.util.UUID;

public class ResultMetrics {

    private UUID requestId;
    private long totalNumberOfHops;
    private long totalNumberOfDevices;
    private long totalNumberOfDevicesWithMetric;
    private long maxHops;
    private long hopLimit;
    private Date timeQueryStarted;
    private Date timeOfLastReceivedResultMessage;
    private String typeOfQuery;

    public ResultMetrics(UUID requestId, long totalNumberOfHops, long totalNumberOfDevices, long
            totalNumberOfDevicesWithMetric, long maxHops, long hopLimit, String typeOfQuery) {
    	this.requestId = requestId;
        this.totalNumberOfHops = totalNumberOfHops;
        this.totalNumberOfDevices = totalNumberOfDevices;
        this.timeQueryStarted = new Date();
        this.totalNumberOfDevicesWithMetric = totalNumberOfDevicesWithMetric;
        this.maxHops = maxHops;
        this.typeOfQuery = typeOfQuery;
        this.hopLimit = hopLimit;
    }

    public synchronized void addResult(int totalNumberOfHops, int totalNumberOfDevices, int
            totalNumberOfDevicesWithMetric, int maxHops, int hopLimit) {
        this.totalNumberOfHops += totalNumberOfHops;
        this.totalNumberOfDevices += totalNumberOfDevices;
        this.totalNumberOfDevicesWithMetric += totalNumberOfDevicesWithMetric;
        this.maxHops = Math.max(maxHops, this.maxHops);
        this.hopLimit = hopLimit;
        this.timeOfLastReceivedResultMessage = new Date();
    }

    @Override
    public String toString() {
        return "ResultMetrics{" +
                "totalNumberOfHops=" + totalNumberOfHops +
                ", maxNumberOfHops=" + maxHops +
                ", totalNumberOfDevices=" + totalNumberOfDevices +
                ", totalNumberOfDevicesWithMetric=" + totalNumberOfDevicesWithMetric +
                ", timeQueryStarted=" + timeQueryStarted +
                ", timeOfLastReceivedResultMessage=" + timeOfLastReceivedResultMessage +
                ", typeOfQuery='" + typeOfQuery + '\'' +
                '}';
    }

	public UUID getRequestId() {
		return requestId;
	}
	
	public long getTotalNumberOfHops() {
		return totalNumberOfHops;
	}
	
	public long getTotalNumberOfDevices() {
		return totalNumberOfDevices;
	}
	
	public long getTotalNumberOfDevicesWithMetric() {
		return totalNumberOfDevicesWithMetric;
	}
	    
	public long getMaxHops() {
		return maxHops;
	}
	
	public long getHopLimit() {
		return hopLimit;
	}
	
	public Date getTimeQueryStarted() {
		return timeQueryStarted;
	}
	
	public Date getTimeOfLastReceivedResultMessage() {
		return timeOfLastReceivedResultMessage;
	}
	
	public String getTypeOfQuery() {
		return typeOfQuery;
	}

}
