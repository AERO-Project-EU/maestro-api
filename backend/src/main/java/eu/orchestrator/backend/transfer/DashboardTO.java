package eu.orchestrator.backend.transfer;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 26/4/2019
 */
public class DashboardTO {

    private Boolean overview;
    private Boolean resources;
    private Boolean systemLogs;
    private Boolean alertLogs;
    private Boolean idsSecurity;
    private Boolean ipsSecurity;
    private Boolean elasticity;
    private Boolean providerHistory;

    public DashboardTO() {
        this.overview = false;
        this.resources = false;
        this.systemLogs = false;
        this.alertLogs = false;
        this.idsSecurity = false;
        this.ipsSecurity = false;
        this.elasticity = false;
        this.providerHistory = false;
    }

    public Boolean getOverview() {
        return overview;
    }

    public void setOverview(Boolean overview) {
        this.overview = overview;
    }

    public Boolean getResources() {
        return resources;
    }

    public void setResources(Boolean resources) {
        this.resources = resources;
    }

    public Boolean getSystemLogs() {
        return systemLogs;
    }

    public void setSystemLogs(Boolean systemLogs) {
        this.systemLogs = systemLogs;
    }

    public Boolean getAlertLogs() {
        return alertLogs;
    }

    public void setAlertLogs(Boolean alertLogs) {
        this.alertLogs = alertLogs;
    }

    public Boolean getIdsSecurity() {
        return idsSecurity;
    }

    public void setIdsSecurity(Boolean idsSecurity) {
        this.idsSecurity = idsSecurity;
    }

    public Boolean getIpsSecurity() {
        return ipsSecurity;
    }

    public void setIpsSecurity(Boolean ipsSecurity) {
        this.ipsSecurity = ipsSecurity;
    }

    public Boolean getElasticity() {
        return elasticity;
    }

    public void setElasticity(Boolean elasticity) {
        this.elasticity = elasticity;
    }

    public Boolean getProviderHistory() {
        return providerHistory;
    }

    public void setProviderHistory(Boolean providerHistory) {
        this.providerHistory = providerHistory;
    }
}
