package cs555.chiba.iotDevices;

import cs555.chiba.util.Utilities;

import java.util.Arrays;
import java.util.List;

public class WaterLeakSensor extends IotDevice{

    private List<Utilities.METRIC_TYPES> listOfValidStates = Arrays.asList(Utilities.METRIC_TYPES.POWER_CONSUMPTION, Utilities.METRIC_TYPES.POWER_STATE, Utilities.METRIC_TYPES.CURRENT_LEAK);

    public WaterLeakSensor() {
        super(IotType.WaterLeakSensor);
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
        return "WaterLeakSensor{}";
    }
}
