package eu.orchestrator.repository.domain;

import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "notification")
public class Notification implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = true, name = "entity_id")
  private String entityID;
  @Column
  private String message;

  @Column(name = "when_date")
  private Date when;

  @Column
  private Long timestamp;

  @Column(nullable = true, name = "notification_type")
  private String notificationType;

  @Column(nullable = true, name = "component_type")
  private String componentType;

  @Column(name = "classes")
  private String classes;

  @Column(nullable = true, name = "is_read", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private boolean read = false;

  @Column(nullable = true, name = "is_seen", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private boolean seen = false;

  @Column(nullable = true, name = "is_dismissed", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private boolean dismiss = false;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "notification_template", nullable = true)
  private NotificationTemplate notificationTemplate;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "user", nullable = true)
  private User user;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "receiver", nullable = true)
  private User receiver;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "sender", nullable = true)
  private User sender;

  @Column(nullable = true, name = "status", columnDefinition = "TINYINT(1) UNSIGNED default '1'")
  @Convert(converter = NumericBooleanConverter.class)
  private boolean status = true;

  @Column(nullable = true, name = "retries")
  private Integer retries = 0;

  @Column(nullable = true, name = "redirect_url")
  private String redirectURL;

  public Notification() {
  }

  public String getEntityID() {
    return entityID;
  }

  public void setEntityID(String entityID) {
    this.entityID = entityID;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public boolean isSeen() {
    return seen;
  }

  public void setSeen(boolean seen) {
    this.seen = seen;
  }

  public boolean isDismiss() {
    return dismiss;
  }

  public void setDismiss(boolean dismiss) {
    this.dismiss = dismiss;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public Integer getRetries() {
    return retries;
  }

  public void setRetries(Integer retries) {
    this.retries = retries;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public enum NotificationType {

    EMAIL("EMAIL"),
    PUSH("PUSH"),
    PUB_SUB("Message Broker");

    private final String friendlyName;

    private NotificationType(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public enum ComponentType {

    APPLICATION_INSTANCE("Application Instance");

    private final String friendlyName;

    private ComponentType(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public String getComponentType() {
    return componentType;
  }

  public void setComponentType(String componentType) {
    this.componentType = componentType;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getWhen() {
    return when;
  }

  public void setWhen(Date when) {
    this.when = when;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(String notificationType) {
    this.notificationType = notificationType;
  }

  public String getClasses() {
    return classes;
  }

  public void setClasses(String classes) {
    this.classes = classes;
  }

  public User getReceiver() {
    return receiver;
  }

  public void setReceiver(User receiver) {
    this.receiver = receiver;
  }

  public User getSender() {
    return sender;
  }

  public void setSender(User sender) {
    this.sender = sender;
  }

  public NotificationTemplate getNotificationTemplate() {
    return notificationTemplate;
  }

  public void setNotificationTemplate(NotificationTemplate notificationTemplate) {
    this.notificationTemplate = notificationTemplate;
  }

  public String getRedirectURL() {
    return redirectURL;
  }

  public void setRedirectURL(String redirectURL) {
    this.redirectURL = redirectURL;
  }
}
