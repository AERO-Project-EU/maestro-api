package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class FileTO implements Serializable {

    private String type;
    private String name;
    private String path;
    private String size;
    private Date dateCreated;
    private Date dateModified;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    @Override
    public String toString() {
        return "FileTO{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", size='" + size + '\'' +
                ", dateCreated=" + dateCreated +
                ", dateModified=" + dateModified +
                '}';
    }
}
