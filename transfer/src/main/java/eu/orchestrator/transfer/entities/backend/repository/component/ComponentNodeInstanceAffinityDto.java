package eu.orchestrator.transfer.entities.backend.repository.component;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 4/9/23
 */
public class ComponentNodeInstanceAffinityDto implements Serializable {

    private Long applicationInstanceId;
    private Long componentNodeInstanceId;
    private List<String> affinityLabels;
    private Date dateCreated;
    private Date lastModified;

    public Long getComponentNodeInstanceId() {
        return componentNodeInstanceId;
    }

    public void setComponentNodeInstanceId(Long componentNodeInstanceId) {
        this.componentNodeInstanceId = componentNodeInstanceId;
    }

    public Long getApplicationInstanceId() {
        return applicationInstanceId;
    }

    public void setApplicationInstanceId(Long applicationInstanceId) {
        this.applicationInstanceId = applicationInstanceId;
    }

    public List<String> getAffinityLabels() {
        return affinityLabels;
    }

    public void setAffinityLabels(List<String> affinityLabels) {
        this.affinityLabels = affinityLabels;
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
