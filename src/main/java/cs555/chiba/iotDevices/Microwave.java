package cs555.chiba.iotDevices;

public class Microwave implements IotDevice{
    @Override
    public String toString() {
        return "Microwave{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
