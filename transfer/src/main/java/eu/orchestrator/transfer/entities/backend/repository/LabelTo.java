package eu.orchestrator.transfer.entities.backend.repository;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class LabelTo implements Serializable,Comparable<LabelTo> {

    private Long labelID;
    private String name;
    private Date dateCreated;
    private Date lastModified;

    public LabelTo() {
    }

    public Long getLabelID() {
        return labelID;
    }

    public void setLabelID(Long labelID) {
        this.labelID = labelID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public int compareTo(LabelTo labelTo) {
        return (int)(this.labelID - labelTo.getLabelID());
    }
}
