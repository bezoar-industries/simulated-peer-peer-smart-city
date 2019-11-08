package cs555.chiba.iotDevices;

public class AirVent implements IotDevice{
    @Override
    public String toString() {
        return "AirVent{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
