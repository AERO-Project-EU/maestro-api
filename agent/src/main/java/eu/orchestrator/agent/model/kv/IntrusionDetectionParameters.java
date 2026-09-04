package eu.orchestrator.agent.model.kv;

import java.util.List;

/**
 * @author Panagiotis Parthenis.
 */
public class IntrusionDetectionParameters {

    private String userName;
    private String password;
    private String url;
    // docker image with tag
    private String idsDockerImage;

    private List<String> volumes;
    private List<String> capabilities;
    private String alertPath;
    private String ruleSetPath;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIdsDockerImage() {
        return idsDockerImage;
    }

    public void setIdsDockerImage(String idsDockerImage) {
        this.idsDockerImage = idsDockerImage;
    }

    public List<String> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<String> volumes) {
        this.volumes = volumes;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    public String getAlertPath() {
        return alertPath;
    }

    public void setAlertPath(String alertPath) {
        this.alertPath = alertPath;
    }

    public String getRuleSetPath() {
        return ruleSetPath;
    }

    public void setRuleSetPath(String ruleSetPath) {
        this.ruleSetPath = ruleSetPath;
    }
}
