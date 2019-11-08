package cs555.chiba.iotDevices;

public class DoorSensor implements IotDevice{
    @Override
    public String toString() {
        return "DoorSensor{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
