package cs555.chiba.iotDevices;

public class TV implements IotDevice{
    @Override
    public String toString() {
        return "TV{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
