package cs555.chiba.iotDevices;

import java.util.Objects;

public abstract class IotDevice {

   private final IotType type;

   public abstract Integer getMetric(String metricName);
   
   public abstract String[] getMetricNames();

   IotDevice(IotType type) {
      this.type = type;
   }

   public IotType getType() {
      return type;
   }

   @Override public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      IotDevice iotDevice = (IotDevice) o;
      return type == iotDevice.type;
   }

   @Override public int hashCode() {
      return Objects.hash(type);
   }

   @Override public String toString() {
      return "IotDevice{" + "type=" + type + '}';
   }
}
