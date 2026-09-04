package eu.orchestrator.repository.domain;

import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "notification_configuration")
public class NotificationConfiguration implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = true, name = "notification_type")
  private String notificationType;

  @Column(nullable = true, name = "smtp_host")
  private String smtpHost;

  @Column(nullable = true, name = "smtp_port")
  private String smtpPort;

  @Column(nullable = true, name = "smtp_username")
  private String smtpUsername;

  @Column(nullable = true, name = "smtp_password")
  private String smtpPassword;

  @Column(nullable = true, name = "smpt_auth", columnDefinition = "TINYINT(1) UNSIGNED default '1'")
  @Convert(converter = NumericBooleanConverter.class)
  private boolean smtpAuth = true;

  @Column(nullable = true, name = "smtp_tls", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private boolean smtpTls = true;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "last_modified")
  @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
  private Date lastModified;

  public NotificationConfiguration() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(String notificationType) {
    this.notificationType = notificationType;
  }

  public String getSmtpHost() {
    return smtpHost;
  }

  public void setSmtpHost(String smtpHost) {
    this.smtpHost = smtpHost;
  }

  public String getSmtpPort() {
    return smtpPort;
  }

  public void setSmtpPort(String smtpPort) {
    this.smtpPort = smtpPort;
  }

  public String getSmtpUsername() {
    return smtpUsername;
  }

  public void setSmtpUsername(String smtpUsername) {
    this.smtpUsername = smtpUsername;
  }

  public String getSmtpPassword() {
    return smtpPassword;
  }

  public void setSmtpPassword(String smtpPassword) {
    this.smtpPassword = smtpPassword;
  }

  public boolean isSmtpAuth() {
    return smtpAuth;
  }

  public void setSmtpAuth(boolean smtpAuth) {
    this.smtpAuth = smtpAuth;
  }

  public boolean isSmtpTls() {
    return smtpTls;
  }

  public void setSmtpTls(boolean smtpTls) {
    this.smtpTls = smtpTls;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }
}
