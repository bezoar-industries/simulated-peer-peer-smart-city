package cs555.chiba.iotDevices;

public class Refrigerator implements IotDevice{
    @Override
    public String toString() {
        return "Refrigerator{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
