package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class QITO implements Serializable {

    private Long id;
    private String qiValue;
    private String resourceType;
    private Integer defaultPriorityLevel;
    private String packetDelayBudget;
    private String packetErrorRate;
    private Date dateCreated;
    private Boolean allowEdit;
    private Boolean allowDelete;

    public QITO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQiValue() {
        return qiValue;
    }

    public void setQiValue(String qiValue) {
        this.qiValue = qiValue;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getDefaultPriorityLevel() {
        return defaultPriorityLevel;
    }

    public void setDefaultPriorityLevel(Integer defaultPriorityLevel) {
        this.defaultPriorityLevel = defaultPriorityLevel;
    }

    public String getPacketDelayBudget() {
        return packetDelayBudget;
    }

    public void setPacketDelayBudget(String packetDelayBudget) {
        this.packetDelayBudget = packetDelayBudget;
    }

    public String getPacketErrorRate() {
        return packetErrorRate;
    }

    public void setPacketErrorRate(String packetErrorRate) {
        this.packetErrorRate = packetErrorRate;
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
