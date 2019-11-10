package cs555.chiba.iotDevices;

import java.util.Arrays;
import java.util.List;

public class WindowSensor implements IotDevice{

    private List<String> listOfValidStates = Arrays.asList("powerConsumption", "powerState", "insideTemperature", "temperature", "openState");

    @Override
    public String toString() {
        return "WindowSensor{}";
    }

    @Override
    public Integer getMetric(String metricName) {
        if(listOfValidStates.contains(metricName)) {
            return 1;
        }
        return 0;
    }
}
