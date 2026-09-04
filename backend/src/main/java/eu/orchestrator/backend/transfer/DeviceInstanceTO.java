package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class DeviceInstanceTO implements Serializable {

    private Long deviceInstanceID;
    private DeviceTO device;
    private String key;
    private String value;

    public DeviceInstanceTO() {
    }

    public Long getDeviceInstanceID() {
        return deviceInstanceID;
    }

    public void setDeviceInstanceID(Long deviceInstanceID) {
        this.deviceInstanceID = deviceInstanceID;
    }

    public DeviceTO getDevice() {
        return device;
    }

    public void setDevice(DeviceTO device) {
        this.device = device;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
