package eu.orchestrator.repository.domain;

import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "notification_template")
public class NotificationTemplate implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = true, name = "notification_type")
  private String notificationType;

  @Column(nullable = true)
  private String description;

  @Column(nullable = true, name = "notification_subject")
  private String notificationSubject;

  @Column(nullable = true, name = "notification_from")
  private String notificationFrom;

  @Column(nullable = true, name = "notification_content_text")
  private String notificationContentText;

  @Column(nullable = true, name = "notification_content_html")
  private String notificationContentHtml;

  @Column(nullable = true, name = "status", columnDefinition = "TINYINT(1) UNSIGNED default '1'")
  @Convert(converter = NumericBooleanConverter.class)
  private boolean status = true;

  public NotificationTemplate() {
  }

  public enum NotificationType {

    EMAIL("EMAIL"),
    PUB_SUB("Message Broker");

    private final String friendlyName;

    private NotificationType(String friendlyName) {
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(String notificationType) {
    this.notificationType = notificationType;
  }

  public String getNotificationContentText() {
    return notificationContentText;
  }

  public void setNotificationContentText(String notificationContentText) {
    this.notificationContentText = notificationContentText;
  }

  public String getNotificationContentHtml() {
    return notificationContentHtml;
  }

  public void setNotificationContentHtml(String notificationContentHtml) {
    this.notificationContentHtml = notificationContentHtml;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public String getNotificationSubject() {
    return notificationSubject;
  }

  public void setNotificationSubject(String notificationSubject) {
    this.notificationSubject = notificationSubject;
  }

  public String getNotificationFrom() {
    return notificationFrom;
  }

  public void setNotificationFrom(String notificationFrom) {
    this.notificationFrom = notificationFrom;
  }
}
