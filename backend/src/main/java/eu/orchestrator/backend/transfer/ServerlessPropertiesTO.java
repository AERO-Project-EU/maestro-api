package eu.orchestrator.backend.transfer;

import java.util.Date;

public class ServerlessPropertiesTO {

    private Long ServerlessPropertiesID;
    private String autoscaler;
    private String metric;
    private String targetValue;
    private String minScale;
    private String maxScale;
    private String windowSize;

    public ServerlessPropertiesTO() {
    }

    public Long getServerlessPropertiesID() {
        return ServerlessPropertiesID;
    }

    public void setServerlessPropertiesID(Long serverlessPropertiesID) {
        ServerlessPropertiesID = serverlessPropertiesID;
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
