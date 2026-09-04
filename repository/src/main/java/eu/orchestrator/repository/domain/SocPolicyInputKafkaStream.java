package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "soc_policy_input_kafka_stream")
public class SocPolicyInputKafkaStream implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "soc_policy", nullable = true)
    private SocPolicy socPolicy;

    @Column(nullable = true)
    private String inputTopic;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "socPolicyInputKafkaStream")
    private List<SocPolicyInputKafkaStreamFieldModel> fieldModelList;

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

    public SocPolicy getSocPolicy() {
        return socPolicy;
    }

    public void setSocPolicy(SocPolicy socPolicy) {
        this.socPolicy = socPolicy;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    public List<SocPolicyInputKafkaStreamFieldModel> getFieldModelList() {
        return fieldModelList;
    }

    public void setFieldModelList(List<SocPolicyInputKafkaStreamFieldModel> fieldModelList) {
        this.fieldModelList = fieldModelList;
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
