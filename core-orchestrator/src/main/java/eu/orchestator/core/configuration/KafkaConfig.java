package eu.orchestator.core.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Konstantinos Theodosiou.
 */
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "kafka")
@Component
public class KafkaConfig {

    private String url;
    private String port;
    private String reportStatusTopic;
    private String backendRequest;
    private String policyEngineTopic;
    private String group;
    private String securityResults;
    private String socUrl;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getReportStatusTopic() {
        return reportStatusTopic;
    }

    public void setReportStatusTopic(String reportStatusTopic) {
        this.reportStatusTopic = reportStatusTopic;
    }

    public String getBackendRequest() {
        return backendRequest;
    }

    public void setBackendRequest(String backendRequest) {
        this.backendRequest = backendRequest;
    }

    public String getPolicyEngineTopic() {
        return policyEngineTopic;
    }

    public void setPolicyEngineTopic(String policyEngineTopic) {
        this.policyEngineTopic = policyEngineTopic;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSecurityResults() {
        return securityResults;
    }

    public void setSecurityResults(String securityResults) {
        this.securityResults = securityResults;
    }

    public String getSocUrl() {
        return socUrl;
    }

    public void setSocUrl(String socUrl) {
        this.socUrl = socUrl;
    }
}
