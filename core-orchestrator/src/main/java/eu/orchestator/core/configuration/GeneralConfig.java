package eu.orchestator.core.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 26/7/2019
 */

@EnableConfigurationProperties
@Component
public class GeneralConfig {

    @Value("${nfs.server.ip}")
    String nfsServerIp;

    @Value("${prometheus.url}")
    String prometheusUrl;

    @Value("${monitoring.delay.enabled}")
    Boolean monitoringDelayEnabled;

    @Value("${key.token}")
    String encryptionKeyToken;

    @Value("${soc.manager.ip}")
    String socManagerIp;

    @Value("${soc.manager.port}")
    String socManagerPort;

    @Value("${soc.manager.username}")
    String socManagerUsername;

    @Value("${soc.manager.password}")
    String socManagerPassword;

    @Value("${soc.manager.private.ip}")
    String socManagerPrivateIp;

    @Value("${soc.agent.url}")
    String socAgentUrl;

    @Value("${soc.auditd.enabled}")
    Boolean socAuditdEnabled;

    @Value("${eu.project}")
    String euProject;

    @Value("${agent.url}")
    String agentUrl;

    @Value("${agent.token}")
    String agentToken;

    // Comma separated CIDRs routed through the private gateway on each worker, e.g. "10.0.0.0/24,10.1.0.0/24".
    // Empty means no extra routes are added.
    @Value("${network.additional.routes:}")
    String networkAdditionalRoutes;

    // Default user of the worker image and, optionally, the password to set for it during
    // provisioning. Leave worker.password empty to keep the image's own credentials.
    @Value("${worker.username:ubuntu}")
    String workerUsername;

    @Value("${worker.password:}")
    String workerPassword;

    // Init script installed as /etc/init.d/cjdns on each worker. Host the script yourself and point
    // this at it; when empty the workers are provisioned without the cjdns service.
    @Value("${cjdns.init.script.url:}")
    String cjdnsInitScriptUrl;

    public String getNfsServerIp() {
        return nfsServerIp;
    }

    public void setNfsServerIp(String nfsServerIp) {
        this.nfsServerIp = nfsServerIp;
    }

    public String getPrometheusUrl() {
        return prometheusUrl;
    }

    public void setPrometheusUrl(String prometheusUrl) {
        this.prometheusUrl = prometheusUrl;
    }

    public Boolean getMonitoringDelayEnabled() {
        return monitoringDelayEnabled;
    }

    public void setMonitoringDelayEnabled(Boolean monitoringDelayEnabled) {
        this.monitoringDelayEnabled = monitoringDelayEnabled;
    }

    public String getEncryptionKeyToken() {
        return encryptionKeyToken;
    }

    public void setEncryptionKeyToken(String encryptionKeyToken) {
        this.encryptionKeyToken = encryptionKeyToken;
    }

    public String getSocManagerIp() {
        return socManagerIp;
    }

    public void setSocManagerIp(String socManagerIp) {
        this.socManagerIp = socManagerIp;
    }

    public String getSocManagerPort() {
        return socManagerPort;
    }

    public void setSocManagerPort(String socManagerPort) {
        this.socManagerPort = socManagerPort;
    }

    public String getSocManagerUsername() {
        return socManagerUsername;
    }

    public void setSocManagerUsername(String socManagerUsername) {
        this.socManagerUsername = socManagerUsername;
    }

    public String getSocManagerPassword() {
        return socManagerPassword;
    }

    public void setSocManagerPassword(String socManagerPassword) {
        this.socManagerPassword = socManagerPassword;
    }

    public String getSocManagerPrivateIp() {
        return socManagerPrivateIp;
    }

    public void setSocManagerPrivateIp(String socManagerPrivateIp) {
        this.socManagerPrivateIp = socManagerPrivateIp;
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

    public String getEuProject() {
        return euProject;
    }

    public void setEuProject(String euProject) {
        this.euProject = euProject;
    }

    public String getAgentUrl() {
        return agentUrl;
    }

    public void setAgentUrl(String agentUrl) {
        this.agentUrl = agentUrl;
    }

    public String getAgentToken() {
        return agentToken;
    }

    public void setAgentToken(String agentToken) {
        this.agentToken = agentToken;
    }

    public String getNetworkAdditionalRoutes() {
        return networkAdditionalRoutes;
    }

    public void setNetworkAdditionalRoutes(String networkAdditionalRoutes) {
        this.networkAdditionalRoutes = networkAdditionalRoutes;
    }

    public String getWorkerUsername() {
        return workerUsername;
    }

    public void setWorkerUsername(String workerUsername) {
        this.workerUsername = workerUsername;
    }

    public String getWorkerPassword() {
        return workerPassword;
    }

    public void setWorkerPassword(String workerPassword) {
        this.workerPassword = workerPassword;
    }

    public String getCjdnsInitScriptUrl() {
        return cjdnsInitScriptUrl;
    }

    public void setCjdnsInitScriptUrl(String cjdnsInitScriptUrl) {
        this.cjdnsInitScriptUrl = cjdnsInitScriptUrl;
    }
}
