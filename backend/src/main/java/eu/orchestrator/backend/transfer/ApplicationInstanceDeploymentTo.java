package eu.orchestrator.backend.transfer;

import java.io.Serializable;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 3/2/21
 */
public class ApplicationInstanceDeploymentTo implements Serializable {

    private Long id;
    private String hexId;

    public ApplicationInstanceDeploymentTo() {

    }

    public ApplicationInstanceDeploymentTo(Long id, String hexId) {
        this.id = id;
        this.hexId = hexId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHexId() {
        return hexId;
    }

    public void setHexId(String hexId) {
        this.hexId = hexId;
    }
}
