package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class RuntimePolicyExpressionTO implements Serializable {

    private Long id;
    private String function;
    private String componentNodeName;
    private String componentNodeHexID;
    private String metric;
    private String dimension;
    private String operand;
    private String threshold;
    private Date dateCreated;
    private Date lastModified;

    public RuntimePolicyExpressionTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getComponentNodeName() {
        return componentNodeName;
    }

    public void setComponentNodeName(String componentNodeName) {
        this.componentNodeName = componentNodeName;
    }

    public String getComponentNodeHexID() {
        return componentNodeHexID;
    }

    public void setComponentNodeHexID(String componentNodeHexID) {
        this.componentNodeHexID = componentNodeHexID;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
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
