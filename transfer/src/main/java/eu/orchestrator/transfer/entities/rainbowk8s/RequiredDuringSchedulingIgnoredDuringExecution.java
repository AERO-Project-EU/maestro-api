package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RequiredDuringSchedulingIgnoredDuringExecution implements Serializable {

    private List<NodeSelectorTerms> nodeSelectorTerms;

    public List<NodeSelectorTerms> getNodeSelectorTerms() {
        if(nodeSelectorTerms == null) {
            nodeSelectorTerms = new ArrayList<>();
        }
        return nodeSelectorTerms;
    }

    public void setNodeSelectorTerms(List<NodeSelectorTerms> nodeSelectorTerms) {
        this.nodeSelectorTerms = nodeSelectorTerms;
    }


    //    public static class Builder {
//        private NodeSelectorTerms nodeSelectorTerms;
//
//        public RequiredDuringSchedulingIgnoredDuringExecution.Builder withNodeSelectorTerms(NodeSelectorTerms nodeSelectorTerms) {
//            this.nodeSelectorTerms = nodeSelectorTerms;
//            return this;
//        }
//
//        public RequiredDuringSchedulingIgnoredDuringExecution build() {
//            RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution = new RequiredDuringSchedulingIgnoredDuringExecution();
//
//            requiredDuringSchedulingIgnoredDuringExecution.setNodeSelectorTerms(this.nodeSelectorTerms);
//
//            return requiredDuringSchedulingIgnoredDuringExecution;
//        }
//    }
}
