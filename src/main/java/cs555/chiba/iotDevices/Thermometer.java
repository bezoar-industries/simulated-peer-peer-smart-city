package cs555.chiba.iotDevices;

public class Thermometer implements IotDevice{
    @Override
    public String toString() {
        return "Thermometer{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
