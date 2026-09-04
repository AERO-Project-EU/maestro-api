package eu.orchestrator.transfer.entities.kubernetes;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 4/9/23
 */
public class ClusterLabelsDto implements Serializable {

    private Collection<String> labels;

    public ClusterLabelsDto(Collection<String> labels) {
        this.labels = labels;
    }

    public Collection<String> getLabels() {
        return labels;
    }

    public void setLabels(Collection<String> labels) {
        this.labels = labels;
    }
}
