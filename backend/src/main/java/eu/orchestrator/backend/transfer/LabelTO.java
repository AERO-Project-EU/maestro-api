package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class LabelTO implements Serializable {

    private Long labelID;
    private String name;
    private Date dateCreated;
    private Date lastModified;

    public LabelTO() {
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

}
