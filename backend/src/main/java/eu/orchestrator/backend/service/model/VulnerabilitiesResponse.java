package eu.orchestrator.backend.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VulnerabilitiesResponse implements Serializable {

    List<Vulnerability> vulnerabilities;


    public List<Vulnerability> getVulnerabilities() {
        if (vulnerabilities == null) {
            vulnerabilities = new ArrayList<>();
        }
        return vulnerabilities;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

}
