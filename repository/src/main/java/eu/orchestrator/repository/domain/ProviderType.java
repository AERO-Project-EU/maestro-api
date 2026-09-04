package eu.orchestrator.repository.domain;

import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "provider_type")
public class ProviderType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String friendlyName;

    @Column(nullable = true, name = "adapter_implementation")
    private String adapterImplementation;

    @Column(nullable = true, name = "is_enabled", columnDefinition = "TINYINT(1) UNSIGNED default '1'")
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean enabled;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    public ProviderType() {
    }

    public enum ProviderName {

        USER_DEFINED("User-defined"),
        POLICY_DEFINED("Policy-defined"),
        OPENSTACK("OpenStack"),
        AWS("Amazon WS"),
        GCC("Google Cloud Compute"),
        FIFTH_GENERATION_TELCO_PROVIDER("5G Telco Provider"),
        IOT_GATEWAY("IoT Gateway"),
        KUBERNETES("Kubernetes"),
        KUBERNETES_KNATIVE("Kubernetes-Knative"),
        RAINBOW_KUBERNETES("Rainbow Kubernetes"),
        FIVE_G_INDUCE_SLICE("5GInduceSlice"),
        FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER("5G OSS Kubernetes"),
        NEXTWORKS_OSS("NextWorks OSS Kubernetes"),
        R_MARKET("R-MARKET");

        private final String friendlyName;

        private ProviderName(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
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


    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getAdapterImplementation() {
        return adapterImplementation;
    }

    public void setAdapterImplementation(String adapterImplementation) {
        this.adapterImplementation = adapterImplementation;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public boolean hasName(ProviderName providerName) {
        return name.equalsIgnoreCase(providerName.name());
    }

    public ProviderName getProviderName() {
        return ProviderName.valueOf(name);
    }

    @Override
    public String toString() {
        return "ProviderType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", friendlyName='" + friendlyName + '\'' +
                '}';
    }
}
