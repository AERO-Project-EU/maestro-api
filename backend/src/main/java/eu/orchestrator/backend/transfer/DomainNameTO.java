package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class DomainNameTO implements Serializable {

    private Long domainNameID;

    private String prefix;
    private String suffix;
    private String domain;

    private Boolean associate;

    private Long applicationInstanceId;
    private String applicationInstanceName;
    private String applicationInstanceHexId;

    private Long componentNodeInstanceId;
    private String componentNodeInstanceName;
    private String componentNodeInstanceHexId;

    private String componentNodeInstanceIPAddress;
    private Date dateCreated;

    public Long getDomainNameID() {
        return domainNameID;
    }

    public void setDomainNameID(Long domainNameID) {
        this.domainNameID = domainNameID;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Boolean getAssociate() {
        return associate;
    }

    public void setAssociate(Boolean associate) {
        this.associate = associate;
    }

    public Long getApplicationInstanceId() {
        return applicationInstanceId;
    }

    public void setApplicationInstanceId(Long applicationInstanceId) {
        this.applicationInstanceId = applicationInstanceId;
    }

    public String getApplicationInstanceName() {
        return applicationInstanceName;
    }

    public void setApplicationInstanceName(String applicationInstanceName) {
        this.applicationInstanceName = applicationInstanceName;
    }

    public String getApplicationInstanceHexId() {
        return applicationInstanceHexId;
    }

    public void setApplicationInstanceHexId(String applicationInstanceHexId) {
        this.applicationInstanceHexId = applicationInstanceHexId;
    }

    public Long getComponentNodeInstanceId() {
        return componentNodeInstanceId;
    }

    public void setComponentNodeInstanceId(Long componentNodeInstanceId) {
        this.componentNodeInstanceId = componentNodeInstanceId;
    }

    public String getComponentNodeInstanceName() {
        return componentNodeInstanceName;
    }

    public void setComponentNodeInstanceName(String componentNodeInstanceName) {
        this.componentNodeInstanceName = componentNodeInstanceName;
    }

    public String getComponentNodeInstanceHexId() {
        return componentNodeInstanceHexId;
    }

    public void setComponentNodeInstanceHexId(String componentNodeInstanceHexId) {
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
    }

    public String getComponentNodeInstanceIPAddress() {
        return componentNodeInstanceIPAddress;
    }

    public void setComponentNodeInstanceIPAddress(String componentNodeInstanceIPAddress) {
        this.componentNodeInstanceIPAddress = componentNodeInstanceIPAddress;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}