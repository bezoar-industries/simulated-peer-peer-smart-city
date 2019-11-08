package cs555.chiba.iotDevices;

public class WindowSensor implements IotDevice{
    @Override
    public String toString() {
        return "WindowSensor{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
