package eu.orchestrator.repository.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "network_rule")
public class NetworkRule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long networkRuleID;

    @Column(name="componentNodeInstanceHexId")
    private String componentNodeInstanceHexId;

    @Column(name="ip")
    private String ip;


    public NetworkRule() {
    }

    public Long getNetworkRuleID() {
        return networkRuleID;
    }

    public void setNetworkRuleID(Long networkRuleID) {
        this.networkRuleID = networkRuleID;
    }

    public String getComponentNodeInstanceHexId() {
        return componentNodeInstanceHexId;
    }

    public void setComponentNodeInstanceHexId(String componentNodeInstanceHexId) {
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
