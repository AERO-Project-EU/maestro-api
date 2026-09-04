package eu.orchestrator.repository.domain;

import eu.orchestrator.repository.domain.Interface.InterfaceType;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "interface_instance")
public class InterfaceInstance implements Serializable, Comparable<InterfaceInstance> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long interfaceInstanceID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance", nullable = true)
  private ComponentNodeInstance componentNodeInstance;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "interface", nullable = true)
  private Interface interfaceObj;

  @Column(nullable = false)
  private String name;

  @Column(nullable = true)
  private String port;

  @Column(nullable = true, name = "interface_type")
  private String interfaceType;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public InterfaceInstance() {
  }

  public Long getInterfaceInstanceID() {
    return interfaceInstanceID;
  }

  public void setInterfaceInstanceID(Long interfaceInstanceID) {
    this.interfaceInstanceID = interfaceInstanceID;
  }

  public ComponentNodeInstance getComponentNodeInstance() {
    return componentNodeInstance;
  }

  public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
    this.componentNodeInstance = componentNodeInstance;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getInterfaceType() {
    return interfaceType;
  }

  public void setInterfaceType(String interfaceType) {
    this.interfaceType = interfaceType;
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

  public Interface getInterfaceObj() {
    return interfaceObj;
  }

  public void setInterfaceObj(Interface interfaceObj) {
    this.interfaceObj = interfaceObj;
  }

  @Override
  public int compareTo(InterfaceInstance compareInterface) {

    Long compareQuantity = compareInterface.getInterfaceInstanceID();

    return this.interfaceInstanceID.compareTo(compareQuantity);
  }

  public boolean hasInterfaceType(InterfaceType type) {
    return interfaceObj.hasType(type);
  }
}
