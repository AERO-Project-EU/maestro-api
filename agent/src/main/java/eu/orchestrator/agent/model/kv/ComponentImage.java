package eu.orchestrator.agent.model.kv;

/**
 * @author Panagiotis Parthenis.
 */
public class ComponentImage {

    private String userName;
    private String password;
    private String url;
    // docker image with tag
    private String serviceImage;

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

    public String getServiceImage() {
        return serviceImage;
    }

    public void setServiceImage(String serviceImage) {
        this.serviceImage = serviceImage;
    }
}
