package eu.orchestrator.transfer.entities.backend.repository.component;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class MetricTo implements Serializable {

    private Long metricID;
    private String name;
    private String friendlyName;
    private String unit;
    private Date dateCreated;
    private Date lastModified;

    public MetricTo() {
    }

    public Long getMetricID() {
        return metricID;
    }

    public void setMetricID(Long metricID) {
        this.metricID = metricID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
