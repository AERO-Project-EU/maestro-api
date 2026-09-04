package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "runtime_policy_expression")
public class RuntimePolicyExpression implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "runtime_policy", nullable = true)
  private RuntimePolicy runtimePolicy;

  @Column(nullable = true)
  private String function;

  @Column(nullable = true, name = "component_name")
  private String componentNodeName;

  @Column(nullable = true, name = "component_hex_id")
  private String componentNodeHexID;

  @Column(nullable = true)
  private String metric;

  @Column(nullable = true)
  private String dimension;

  @Column(nullable = true)
  private String operand;

  @Column(nullable = true)
  private String threshold;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public RuntimePolicyExpression() {
  }

  public enum FunctionType {

    NONE("No Function"),
    SUM("Sum"),
    AVG("Average");

    private String friendlyName;

    FunctionType(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public enum OperandType {

    LESS("<"),
    LESSOREQUAL("<="),
    GREATER(">"),
    GREATEROREQUAL(">=");

    private String friendlyName;

    OperandType(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public enum ArithmeticOperator {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/");

    private String friendlyName;

    ArithmeticOperator(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public RuntimePolicy getRuntimePolicy() {
    return runtimePolicy;
  }

  public void setRuntimePolicy(RuntimePolicy runtimePolicy) {
    this.runtimePolicy = runtimePolicy;
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
