package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

@Entity
@Table(name = "volume")
public class Volume implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long volumeID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component", nullable = true)
  private Component component;

  @Column(nullable = false, name = "docker_path" )
  private String dockerPath;

  @Column(nullable = false, name = "is_file", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean isFile = false;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public Volume() {
  }

  public Long getVolumeID() {
    return volumeID;
  }

  public void setVolumeID(Long volumeID) {
    this.volumeID = volumeID;
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
  }

  public String getDockerPath() {
    return dockerPath;
  }

  public void setDockerPath(String dockerPath) {
    this.dockerPath = dockerPath;
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

  @Override
  public String toString() {
    return "Volume{" +
        "volumeID=" + volumeID +
        ", component=" + component +
        ", dockerPath='" + dockerPath + '\'' +
        ", isFile=" + isFile +
        ", dateCreated=" + dateCreated +
        ", lastModified=" + lastModified +
        '}';
  }
}
