package cs555.chiba.iotDevices;

import cs555.chiba.util.Utilities;

import java.util.Arrays;
import java.util.List;

public class AirVent extends IotDevice {

    private List<Utilities.METRIC_TYPES> listOfValidStates = Arrays.asList(Utilities.METRIC_TYPES.POWER_CONSUMPTION, Utilities.METRIC_TYPES.POWER_STATE, Utilities.METRIC_TYPES.AIR_QUALITY, Utilities.METRIC_TYPES.TEMPERATURE, Utilities.METRIC_TYPES.THROTTLE_STATE);

    public AirVent() {
        super(IotType.AirVent);
    }
    
    @Override
    public String[] getMetricNames() {
    	String[] metricNames = new String[listOfValidStates.size()];
    	for(int i = 0; i < metricNames.length; i++) {
    		metricNames[i] = listOfValidStates.get(i).name();
    	}
    	return metricNames;
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
        return "AirVent{}";
    }
}
