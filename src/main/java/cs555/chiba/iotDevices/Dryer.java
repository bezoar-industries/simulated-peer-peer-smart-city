package cs555.chiba.iotDevices;

public class Dryer implements IotDevice{
    @Override
    public String toString() {
        return "Dryer{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
