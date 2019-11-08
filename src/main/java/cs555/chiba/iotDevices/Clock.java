package cs555.chiba.iotDevices;

public class Clock implements IotDevice{
    @Override
    public String toString() {
        return "Clock{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
