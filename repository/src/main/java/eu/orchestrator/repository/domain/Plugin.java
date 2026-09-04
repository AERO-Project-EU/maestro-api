package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import org.hibernate.annotations.Cascade;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "plugin")
public class Plugin implements Serializable, Comparable<Plugin> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long pluginID;

  @Column(nullable = true)
  private String name;

  @Column(nullable = true, name = "module_name")
  private String moduleName;

  @Column(nullable = true, name = "download_url")
  private String downloadURL;

  @Column(nullable = true, name = "plugin_type")
  private String pluginType;

  @Column(nullable = true, name = "port")
  private String port;

  @Column(nullable = true, name = "endpoint")
  private String endpoint;

  @Column(nullable = true, name = "is_public", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean publicPlugin;

  @Column(nullable = true, name = "is_immutable", columnDefinition = "tinyint(1) default 0")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean immutablePlugin;

  @Column(nullable = true, name = "is_default", columnDefinition = "tinyint(1) default 0")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean defaultPlugin;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "user", nullable = true)
  private User user;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "organization", nullable = true)
  private Organization organization;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "plugin")
  @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  private List<Metric> metrics;

  @Transient
  private Integer metricsCounter;

  public Plugin() {
  }

  public enum PluginType {

    HTTP("HTTP"),
    SOCKET("Socket"),
    DOWNLOAD_CONF("Download Conf");

    private final String friendlyName;

    PluginType(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public Long getPluginID() {
    return pluginID;
  }

  public void setPluginID(Long pluginID) {
    this.pluginID = pluginID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public List<Metric> getMetrics() {
    return metrics;
  }

  public void setMetrics(List<Metric> metrics) {
    this.metrics = metrics;
  }

  public Integer getMetricsCounter() {
    return metricsCounter;
  }

  public void setMetricsCounter(Integer metricsCounter) {
    this.metricsCounter = metricsCounter;
  }

  public Boolean getImmutablePlugin() {
    return immutablePlugin;
  }

  public void setImmutablePlugin(Boolean immutablePlugin) {
    this.immutablePlugin = immutablePlugin;
  }

  public Boolean getPublicPlugin() {
    return publicPlugin;
  }

  public void setPublicPlugin(Boolean publicPlugin) {
    this.publicPlugin = publicPlugin;
  }

  public Boolean getDefaultPlugin() {
    return defaultPlugin;
  }

  public void setDefaultPlugin(Boolean defaultPlugin) {
    this.defaultPlugin = defaultPlugin;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
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

  public String getDownloadURL() {
    return downloadURL;
  }

  public void setDownloadURL(String downloadURL) {
    this.downloadURL = downloadURL;
  }

  public String getPluginType() {
    return pluginType;
  }

  public void setPluginType(String pluginType) {
    this.pluginType = pluginType;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public int compareTo(Plugin comparePlugin) {
    Long compareQuantity = comparePlugin.getPluginID();
    return this.pluginID.compareTo(compareQuantity);
  }

  public List<Long> getMetricIDs() {

    List<Long> metricIDs = new ArrayList<>();

    if (null != this.metrics && !this.metrics.isEmpty()) {

      this.metrics
          .stream()
          .forEach(
              metric -> {
                metricIDs.add(metric.getMetricID());
              });
    }

    return metricIDs;
  }

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(Organization organization) {
    this.organization = organization;
  }

  public Boolean hasEditAllowance(User user) {

    if (user != null && user.getOrganization() != null && getOrganization() != null &&
        (getUser().equals(user) || user.isAdmin() ||
            (getOrganization().equals(user.getOrganization()) && user.isOrganizationAdmin()))) {
      return true;
    }
    return false;
  }

  public Boolean hasDeleteAllowance(User user) {
    if (user != null && user.getOrganization() != null && getOrganization() != null &&
        (getUser().equals(user) || user.isAdmin() ||
            (getOrganization().equals(user.getOrganization()) && user.isOrganizationAdmin()))) {
      return true;
    }
    return false;
  }
}
