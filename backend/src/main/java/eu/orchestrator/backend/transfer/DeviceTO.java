package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class DeviceTO implements Serializable {

    private Long deviceID;
    private String key;
    private String value;

    public DeviceTO() {
    }

    public Long getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(Long deviceID) {
        this.deviceID = deviceID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
