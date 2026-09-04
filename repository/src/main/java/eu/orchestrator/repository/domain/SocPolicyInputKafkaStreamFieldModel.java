package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "soc_policy_input_kafka_stream_field_model")
public class SocPolicyInputKafkaStreamFieldModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonProperty("fieldName")
    @Column(nullable = true)
    private String name;

    @JsonProperty("fieldType")
    @Column(nullable = true)
    private String type;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "soc_policy_input_kafka_stream", nullable = true)
    private SocPolicyInputKafkaStream socPolicyInputKafkaStream;

    @JsonIgnore
    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @JsonIgnore
    @Column(nullable = false, name = "last_modified")
    private Date lastModified;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SocPolicyInputKafkaStream getSocPolicyInputKafkaStream() {
        return socPolicyInputKafkaStream;
    }

    public void setSocPolicyInputKafkaStream(SocPolicyInputKafkaStream socPolicyInputKafkaStream) {
        this.socPolicyInputKafkaStream = socPolicyInputKafkaStream;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    /**
     * Inner enum class
     */
    public enum FieldType {
        BOOLEAN("Boolean"),
        INTEGER("Integer"),
        BIGINT("Bigint"),
        DOUBLE("Double"),
        VARCHAR("Varchar"),
        STRING("String");
        private String friendlyName;

        FieldType(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }
}
