package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class ServerlessPropertiesInstanceTO implements Serializable {

    private Long serverlessPropertiesInstanceID;
    private ServerlessPropertiesTO serverlessProperties;
    private String autoscaler;
    private String metric;
    private String targetValue;
    private String minScale;
    private String maxScale;
    private String windowSize;

    public ServerlessPropertiesInstanceTO() {
    }

    public Long getServerlessPropertiesInstanceID() {
        return serverlessPropertiesInstanceID;
    }

    public void setServerlessPropertiesInstanceID(Long serverlessPropertiesInstanceID) {
        this.serverlessPropertiesInstanceID = serverlessPropertiesInstanceID;
    }

    public ServerlessPropertiesTO getServerlessProperties() {
        return serverlessProperties;
    }

    public void setServerlessProperties(ServerlessPropertiesTO serverlessProperties) {
        this.serverlessProperties = serverlessProperties;
    }

    public String getAutoscaler() {
        return autoscaler;
    }

    public void setAutoscaler(String autoscaler) {
        this.autoscaler = autoscaler;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public String getMinScale() {
        return minScale;
    }

    public void setMinScale(String minScale) {
        this.minScale = minScale;
    }

    public String getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(String maxScale) {
        this.maxScale = maxScale;
    }

    public String getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(String windowSize) {
        this.windowSize = windowSize;
    }
}
