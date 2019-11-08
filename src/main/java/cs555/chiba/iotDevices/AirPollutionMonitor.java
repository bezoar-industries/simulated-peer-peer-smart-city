package cs555.chiba.iotDevices;

public class AirPollutionMonitor implements IotDevice{
    @Override
    public String toString() {
        return "AirPollutionMonitor{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
