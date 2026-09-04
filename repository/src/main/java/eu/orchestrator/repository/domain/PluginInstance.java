package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "plugin_instance")
public class PluginInstance implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long pluginInstanceID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance", nullable = true)
  private ComponentNodeInstance componentNodeInstance;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "plugin", nullable = true)
  private Plugin plugin;

  @Column(nullable = true)
  private String name;

  @Column(nullable = true, name = "module_name")
  private String moduleName;

  @Column(nullable = true, name = "is_immutable", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean immutablePlugin = false;

  @Column(nullable = true, name = "is_deleted", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean deletedPlugin = false;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public PluginInstance() {
  }

  public Boolean getImmutablePlugin() {
    return immutablePlugin;
  }

  public void setImmutablePlugin(Boolean immutablePlugin) {
    this.immutablePlugin = immutablePlugin;
  }

  public Long getPluginInstanceID() {
    return pluginInstanceID;
  }

  public void setPluginInstanceID(Long pluginInstanceID) {
    this.pluginInstanceID = pluginInstanceID;
  }

  public ComponentNodeInstance getComponentNodeInstance() {
    return componentNodeInstance;
  }

  public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
    this.componentNodeInstance = componentNodeInstance;
  }

  public Plugin getPlugin() {
    return plugin;
  }

  public void setPlugin(Plugin plugin) {
    this.plugin = plugin;
  }

  public Boolean getDeletedPlugin() {
    return deletedPlugin;
  }

  public void setDeletedPlugin(Boolean deletedPlugin) {
    this.deletedPlugin = deletedPlugin;
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
}
