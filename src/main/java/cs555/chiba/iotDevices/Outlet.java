package cs555.chiba.iotDevices;

public class Outlet implements IotDevice{
    @Override
    public String toString() {
        return "Outlet{}";
    }

    @Override
    public Object getMetric(String metricName) {
        return null;
    }
}
