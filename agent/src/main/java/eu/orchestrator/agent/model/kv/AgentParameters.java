package eu.orchestrator.agent.model.kv;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 22/3/2019
 */
public class AgentParameters {

    private String euProject;
    private boolean hasDependencies;
    private boolean isLoadBalancer;
    private boolean hasEnableIpv6;
    private boolean hasEnableIds;
    private boolean hasEnableIps;
    private boolean hasEnableSoc;
    private boolean hasEnablePublicIpV4;
    private boolean hasEnableSecurity;
    private String nfsServerHost;
    private String prometheusHost;
    private String socManagerIp;
    private String socAgentUrl;
    private Boolean socAuditdEnabled;

    
    public String getEuProject() {
        return euProject;
    }

    public void setEuProject(String euProject) {
        this.euProject = euProject;
    }

    public boolean isHasDependencies() {
        return hasDependencies;
    }

    public void setHasDependencies(boolean hasDependencies) {
        this.hasDependencies = hasDependencies;
    }

    public boolean isLoadBalancer() {
        return isLoadBalancer;
    }

    public void setLoadBalancer(boolean loadBalancer) {
        isLoadBalancer = loadBalancer;
    }

    public boolean isHasEnableIpv6() {
        return hasEnableIpv6;
    }

    public void setHasEnableIpv6(boolean hasEnableIpv6) {
        this.hasEnableIpv6 = hasEnableIpv6;
    }

    public boolean isHasEnableIds() {
        return hasEnableIds;
    }

    public void setHasEnableIds(boolean hasEnableIds) {
        this.hasEnableIds = hasEnableIds;
    }

    public boolean isHasEnableIps() {
        return hasEnableIps;
    }

    public void setHasEnableIps(boolean hasEnableIps) {
        this.hasEnableIps = hasEnableIps;
    }

    public boolean isHasEnableSoc() {
        return hasEnableSoc;
    }

    public void setHasEnableSoc(boolean hasEnableSoc) {
        this.hasEnableSoc = hasEnableSoc;
    }

    public boolean isHasEnablePublicIpV4() {
        return hasEnablePublicIpV4;
    }

    public void setHasEnablePublicIpV4(boolean hasEnablePublicIpV4) {
        this.hasEnablePublicIpV4 = hasEnablePublicIpV4;
    }

    public boolean isHasEnableSecurity() {
        return hasEnableSecurity;
    }

    public void setHasEnableSecurity(boolean hasEnableSecurity) {
        this.hasEnableSecurity = hasEnableSecurity;
    }

    public String getNfsServerHost() {
        return nfsServerHost;
    }

    public void setNfsServerHost(String nfsServerHost) {
        this.nfsServerHost = nfsServerHost;
    }

    public String getPrometheusHost() {
        return prometheusHost;
    }

    public void setPrometheusHost(String prometheusHost) {
        this.prometheusHost = prometheusHost;
    }

    public String getSocManagerIp() {
        return socManagerIp;
    }

    public void setSocManagerIp(String socManagerIp) {
        this.socManagerIp = socManagerIp;
    }

    public String getSocAgentUrl() {
        return socAgentUrl;
    }

    public void setSocAgentUrl(String socAgentUrl) {
        this.socAgentUrl = socAgentUrl;
    }

    public Boolean getSocAuditdEnabled() {
        return socAuditdEnabled;
    }

    public void setSocAuditdEnabled(Boolean socAuditdEnabled) {
        this.socAuditdEnabled = socAuditdEnabled;
    }
}
