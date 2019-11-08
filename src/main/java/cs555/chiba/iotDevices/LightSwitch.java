package cs555.chiba.iotDevices;

public class LightSwitch implements IotDevice{
    @Override
    public String toString() {
        return "LightSwitch{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
