package cs555.chiba.iotDevices;

public class Watch implements IotDevice{
    @Override
    public String toString() {
        return "Watch{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
