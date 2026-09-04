package eu.orchestrator.transfer.entities.iotstack;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 */
public class Peer implements Serializable {
    private String fromnode;
    private String tonode;
    private Date registrationdate;
    private String reportingnode;
    private boolean isactive;

    public Peer() {
    }

    public String getFromnode() {
        return fromnode;
    }

    public void setFromnode(String fromnode) {
        this.fromnode = fromnode;
    }

    public String getTonode() {
        return tonode;
    }

    public void setTonode(String tonode) {
        this.tonode = tonode;
    }

    public Date getRegistrationdate() {
        return registrationdate;
    }

    public void setRegistrationdate(Date registrationdate) {
        this.registrationdate = registrationdate;
    }

    public String getReportingnode() {
        return reportingnode;
    }

    public void setReportingnode(String reportingnode) {
        this.reportingnode = reportingnode;
    }

    public boolean isIsactive() {
        return isactive;
    }

    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }
}
