/**
 * The Protocol class simply defines the ID for all
 * different message types (each ID must be unique)
 *
 * @author Kevin Bruhwiler
 * @version 1.0
 * @since 2019-06-11
 */

package cs555.chiba.iotDevices;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

enum IotType {
   AirPollutionMonitor, AirVent, Clock, DoorLock, DoorSensor, Dryer, LightSwitch, Microwave, Outlet, PowerMeter, Refrigerator, StreetLight, Thermometer, Thermostat, TV, WashMachine, Watch, WaterLeakSensor, WindowSensor;

   private static final Logger logger = Logger.getLogger(IotType.class.getName());

   public IotDevice getInstance() {
      try {
         String path = IotDevice.class.getPackage().getName() + "." + this.name();
         return (IotDevice) Class.forName(path).getConstructor().newInstance();
      }
      catch (Exception e) {
         logger.log(Level.SEVERE, "This is going to be a fun error: ", e);
         return new WindowSensor();  // this should never happen
      }
   }
}
