package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ProviderTO implements Serializable {

    private Long providerID;
    private String name;
    private ProviderTypeTO providerType;
    private Boolean defaultProvider;
    private Integer regionsCounter;
    private List<RegionTO> regions;
    private Date dateCreated;
    private Date lastModified;
    private ProviderQuotaTO providerQuota;
    private String organization;
    private Boolean allowEdit;
    private Boolean allowDelete;

    public ProviderTO() {
    }

    public Long getProviderID() {
        return providerID;
    }

    public void setProviderID(Long providerID) {
        this.providerID = providerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProviderTypeTO getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderTypeTO providerType) {
        this.providerType = providerType;
    }

    public Boolean getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(Boolean defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public Integer getRegionsCounter() {
        return regionsCounter;
    }

    public void setRegionsCounter(Integer regionsCounter) {
        this.regionsCounter = regionsCounter;
    }

    public List<RegionTO> getRegions() {
        return regions;
    }

    public void setRegions(List<RegionTO> regions) {
        this.regions = regions;
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

    public ProviderQuotaTO getProviderQuota() {
        return providerQuota;
    }

    public void setProviderQuota(ProviderQuotaTO providerQuota) {
        this.providerQuota = providerQuota;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
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
