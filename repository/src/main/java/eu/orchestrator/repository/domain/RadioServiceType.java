package eu.orchestrator.repository.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "radio_service_type")
public class RadioServiceType implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "service_type")
  private String serviceType;

  @Column(nullable = false, name = "sst_value")
  private Integer sstValue;

  @Column(nullable = false, name = "characteristics")
  private String characteristics;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  public RadioServiceType() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getServiceType() {
    return serviceType;
  }

  public void setServiceType(String serviceType) {
    this.serviceType = serviceType;
  }

  public Integer getSstValue() {
    return sstValue;
  }

  public void setSstValue(Integer sstValue) {
    this.sstValue = sstValue;
  }

  public String getCharacteristics() {
    return characteristics;
  }

  public void setCharacteristics(String characteristics) {
    this.characteristics = characteristics;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public Boolean hasEditAllowance(User user) {
    if (user.isAdmin()) {
      return true;
    }
    return false;
  }

  public Boolean hasDeleteAllowance(User user) {
    if (user.isAdmin()) {
      return true;
    }
    return false;
  }
}
