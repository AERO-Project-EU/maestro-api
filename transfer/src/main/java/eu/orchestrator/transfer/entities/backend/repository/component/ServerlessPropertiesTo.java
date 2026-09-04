package eu.orchestrator.transfer.entities.backend.repository.component;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Vasileios Matsoukas
 * @email billmats96@gmail.com
 * @date 3/9/24
 */
public class ServerlessPropertiesTo implements Serializable {

    private String autoscaler;
    private String metric;
    private String targetValue;
    private String minScale;
    private String maxScale;
    private String windowSize;
    private Date dateCreated;
    private Date lastModified;

    public ServerlessPropertiesTo() {
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
}
