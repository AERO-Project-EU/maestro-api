package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ProfileTO implements Serializable {

    private Long id;
    private Long applicationInstanceID;


    private String name;
    private String status;
    private String algorithm;
    private Long step;
    private Date startTime;
    private Date endTime;

    private List<ProfileMetricTO> profileMetrics;

    private Date dateCreated;
    private String username;
    private Boolean allowEdit;
    private Boolean allowDelete;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(Long applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Long getStep() {
        return step;
    }

    public void setStep(Long step) {
        this.step = step;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<ProfileMetricTO> getProfileMetrics() {
        return profileMetrics;
    }

    public void setProfileMetrics(
            List<ProfileMetricTO> profileMetrics) {
        this.profileMetrics = profileMetrics;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
