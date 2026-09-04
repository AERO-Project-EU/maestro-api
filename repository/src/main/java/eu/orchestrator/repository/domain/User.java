package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "user")
public class User implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String username;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Column(nullable = true)
  private String password;

  @Column(nullable = true, name = "first_name")
  private String firstName;

  @Column(nullable = true, name = "last_name")
  private String lastName;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String phone;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "country", nullable = true)
  private Country country;

  @Column(nullable = false)
  private String role;

  @Column(nullable = true, name = "notification_web_enabled", columnDefinition = "TINYINT(1) UNSIGNED default '1'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean notificationWebEnabled;

  @Column(nullable = true, name = "notification_email_enabled", columnDefinition = "TINYINT(1) UNSIGNED default '1'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean notificationEmailEnabled;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = true, name = "first_login", columnDefinition = "TINYINT(1) UNSIGNED default '1'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean firstLogin;

  @Column(nullable = true, name = "is_enabled", columnDefinition = "TINYINT(1) UNSIGNED default '1'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean enabled;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "organization", nullable = true)
  private Organization organization;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Transient
  private String newPassword;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Transient
  private String verifyPassword;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Transient
  private String currentPassword;

  public enum RoleName {
    ADMIN, USER, ORGANIZATIONADMIN
  }

  public User() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public Boolean getNotificationWebEnabled() {
    return notificationWebEnabled;
  }

  public void setNotificationWebEnabled(Boolean notificationWebEnabled) {
    this.notificationWebEnabled = notificationWebEnabled;
  }

  public Boolean getNotificationEmailEnabled() {
    return notificationEmailEnabled;
  }

  public void setNotificationEmailEnabled(Boolean notificationEmailEnabled) {
    this.notificationEmailEnabled = notificationEmailEnabled;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public Boolean getFirstLogin() {
    return firstLogin;
  }

  public void setFirstLogin(Boolean firstLogin) {
    this.firstLogin = firstLogin;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getVerifyPassword() {
    return verifyPassword;
  }

  public void setVerifyPassword(String verifyPassword) {
    this.verifyPassword = verifyPassword;
  }

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public boolean isAdmin() {
    return this.role.equals(RoleName.ADMIN.name());
  }

  public boolean isOrganizationAdmin() {
    return this.role.equals(RoleName.ORGANIZATIONADMIN.name());
  }

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(Organization organization) {
    this.organization = organization;
  }

  public Boolean hasEditAllowance(User user) {
    if (user.isAdmin() || user.getId().equals(getId())) {
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

