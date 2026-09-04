package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class LocationInstanceTO implements Serializable {

    private Long locationInstanceID;
    private CountryTO country;
    private String region;

    public LocationInstanceTO() {
    }

    public Long getLocationInstanceID() {
        return locationInstanceID;
    }

    public void setLocationInstanceID(Long locationInstanceID) {
        this.locationInstanceID = locationInstanceID;
    }

    public CountryTO getCountry() {
        return country;
    }

    public void setCountry(CountryTO country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
