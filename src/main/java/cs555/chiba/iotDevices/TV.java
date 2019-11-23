package cs555.chiba.iotDevices;

import cs555.chiba.util.Utilities;

import java.util.Arrays;
import java.util.List;

public class TV extends IotDevice{

    private List<Utilities.METRIC_TYPES> listOfValidStates = Arrays.asList(Utilities.METRIC_TYPES.POWER_CONSUMPTION, Utilities.METRIC_TYPES.POWER_STATE, Utilities.METRIC_TYPES.CPU_USAGE, Utilities.METRIC_TYPES.MEMORY_USAGE, Utilities.METRIC_TYPES.NETFLIX, Utilities.METRIC_TYPES.AMAZON_PRIME);

    public TV() {
        super(IotType.TV);
    }

    @Override
    public Integer getMetric(String metricName) {
        if (listOfValidStates.contains(Utilities.getEnum(metricName))) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "TV{}";
    }
}
