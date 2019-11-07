package cs555.chiba.iotDevices;

public class DoorLock implements IotDevice{
    @Override
    public String toString() {
        return "DoorLock{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
