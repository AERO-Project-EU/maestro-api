package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "interface")
public class Interface implements Serializable, Comparable<Interface> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long interfaceID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component", nullable = true)
  private Component component;

  @Column(nullable = false)
  private String name;

  @Column(nullable = true)
  private String port;

  @Column(nullable = true)
  private String vna;

  @Column(nullable = true, name = "interface_type")
  private String interfaceType;

  @Column(nullable = true, name = "transmission_protocol")
  private String transmissionProtocol;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public Interface() {
  }

  public enum InterfaceType {

    CORE("Core"),
    ACCESS("Access");

    private String friendlyName;

    InterfaceType(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public enum TransmissionProtocol {

    TCP("TCP"),
    UDP("UDP"),
    BOTH("Both");

    private String friendlyName;

    TransmissionProtocol(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public Long getInterfaceID() {
    return interfaceID;
  }

  public void setInterfaceID(Long interfaceID) {
    this.interfaceID = interfaceID;
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

  public String getVna() {
    return vna;
  }

  public void setVna(String vna) {
    this.vna = vna;
  }

  public void setInterfaceType(String interfaceType) {
    this.interfaceType = interfaceType;
  }

  public String getTransmissionProtocol() {
    return transmissionProtocol;
  }

  public void setTransmissionProtocol(String transmissionProtocol) {
    this.transmissionProtocol = transmissionProtocol;
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

  @Override
  public int compareTo(Interface compareInterface) {
    Long compareQuantity = ((Interface) compareInterface).getInterfaceID();

    return this.interfaceID.compareTo(compareQuantity);
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
  }

  public boolean hasType(InterfaceType type) {
    return this.getInterfaceType().equalsIgnoreCase(type.name());
  }
}
