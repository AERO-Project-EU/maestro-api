package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

@Entity
@Table(name = "volume_instance")
public class VolumeInstance implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long volumeInstanceID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance", nullable = true)
  private ComponentNodeInstance componentNodeInstance;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "volume", nullable = true)
  private Volume volume;

  @Column(nullable = false, name = "docker_path" )
  private String dockerPath;

  @Column(nullable = false, name = "host_path" )
  private String hostPath;

  @Column(nullable = false, name = "is_file", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean isFile = false;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public VolumeInstance() {
  }

  public Long getVolumeInstanceID() {
    return volumeInstanceID;
  }

  public void setVolumeInstanceID(Long volumeInstanceID) {
    this.volumeInstanceID = volumeInstanceID;
  }

  public ComponentNodeInstance getComponentNodeInstance() {
    return componentNodeInstance;
  }

  public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
    this.componentNodeInstance = componentNodeInstance;
  }

  public Volume getVolume() {
    return volume;
  }

  public void setVolume(Volume volume) {
    this.volume = volume;
  }

  public String getDockerPath() {
    return dockerPath;
  }

  public void setDockerPath(String dockerPath) {
    this.dockerPath = dockerPath;
  }

  public String getHostPath() {
    return hostPath;
  }

  public void setHostPath(String hostPath) {
    this.hostPath = hostPath;
  }

  public Boolean getFile() {
    return isFile;
  }

  public void setFile(Boolean file) {
    isFile = file;
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
