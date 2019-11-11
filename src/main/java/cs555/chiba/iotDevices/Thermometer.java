package cs555.chiba.iotDevices;

import java.util.Arrays;
import java.util.List;

public class Thermometer implements IotDevice{

    private List<String> listOfValidStates = Arrays.asList("powerConsumption", "powerState", "temperature", "recordHigh", "recordLow");

    @Override
    public Integer getMetric(String metricName) {
        if(listOfValidStates.contains(metricName)) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Thermometer{}";
    }
}
