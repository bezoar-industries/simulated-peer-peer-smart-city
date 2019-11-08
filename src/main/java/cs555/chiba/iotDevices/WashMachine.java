package cs555.chiba.iotDevices;

public class WashMachine implements IotDevice{
    @Override
    public String toString() {
        return "WashMachine{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
