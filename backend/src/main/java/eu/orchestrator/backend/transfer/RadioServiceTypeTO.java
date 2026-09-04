package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class RadioServiceTypeTO implements Serializable {

    private Long id;
    private String serviceType;
    private Integer sstValue;
    private String characteristics;
    private Date dateCreated;
    private Boolean allowEdit;
    private Boolean allowDelete;

    public RadioServiceTypeTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Integer getSstValue() {
        return sstValue;
    }

    public void setSstValue(Integer sstValue) {
        this.sstValue = sstValue;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Boolean getAllowEdit() {
        return allowEdit;
    }

    public void setAllowEdit(Boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    public Boolean getAllowDelete() {
        return allowDelete;
    }

    public void setAllowDelete(Boolean allowDelete) {
        this.allowDelete = allowDelete;
    }
}
