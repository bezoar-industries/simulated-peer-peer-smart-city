package cs555.chiba.iotDevices;

public class WaterLeakSensor implements IotDevice{
    @Override
    public String toString() {
        return "WaterLeakSensor{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
