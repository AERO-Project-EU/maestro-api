package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Node implements Serializable {

    private String name;
    private Map<String, String> labels;
    private Affinity affinity;
    private List<Container> containers;
    private List<ImagePullSecret> imagePullSecrets;
    private List<Volume> volumes;
    private Replicas replicas;
    private ExposedPort exposedPorts;
    private NodeHardware nodeHardware;
    private Boolean hostNetwork;

    private List<SloTo> slos;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    public List<Container> getContainers() {
        if (containers == null) {
            containers = new ArrayList<>();
        }
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    public List<ImagePullSecret> getImagePullSecrets() {
        if (imagePullSecrets == null) {
            imagePullSecrets = new ArrayList<>();
        }
        return imagePullSecrets;
    }

    public void setImagePullSecrets(List<ImagePullSecret> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }

    public Replicas getReplicas() {
        return replicas;
    }

    public void setReplicas(Replicas replicas) {
        this.replicas = replicas;
    }

    public ExposedPort getExposedPorts() {
        return exposedPorts;
    }

    public void setExposedPorts(ExposedPort exposedPorts) {
        this.exposedPorts = exposedPorts;
    }

    public NodeHardware getNodeHardware() {
        return nodeHardware;
    }

    public void setNodeHardware(NodeHardware nodeHardware) {
        this.nodeHardware = nodeHardware;
    }

    public Boolean getHostNetwork() {
        return hostNetwork;
    }

    public void setHostNetwork(Boolean hostNetwork) {
        this.hostNetwork = hostNetwork;
    }

    public List<SloTo> getSlos() {
        if(slos == null) {
            slos = new ArrayList<>();
        }
        return slos;
    }

    public void setSlos(List<SloTo> sloTos) {
        this.slos = sloTos;
    }


    public static class Builder {

        private String name;
        private Map<String, String> labels;
        private Affinity affinity;
        private List<Container> containers;
        private List<ImagePullSecret> imagePullSecrets;
        private List<Volume> volumes;
        private Replicas replicas;
        private ExposedPort exposedPorts;
        private NodeHardware nodeHardware;
        private Boolean hostNetwork;
        private List<SloTo> sloTos;

        public Node.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Node.Builder withLabels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Node.Builder withAffinity(Affinity affinity) {
            this.affinity = affinity;
            return this;
        }

        public Node.Builder withContainers(List<Container> containers) {
            this.containers = containers;
            return this;
        }

        public Node.Builder withImagePullSecrets(List<ImagePullSecret> imagePullSecrets) {
            this.imagePullSecrets = imagePullSecrets;
            return this;
        }

        public Node.Builder withVolumes(List<Volume> volumes) {
            this.volumes = volumes;
            return this;
        }

        public Node.Builder withReplicas(Replicas replicas) {
            this.replicas = replicas;
            return this;
        }

        public Node.Builder withExposedPorts(ExposedPort exposedPorts) {
            this.exposedPorts = exposedPorts;
            return this;
        }

        public Node.Builder withNodeHardware(NodeHardware nodeHardware) {
            this.nodeHardware = nodeHardware;
            return this;
        }

        public Node.Builder withHostNetwork(Boolean hostNetwork) {
            this.hostNetwork = hostNetwork;
            return this;
        }

        public Node.Builder withSlos(List<SloTo> sloTos) {
            this.sloTos = sloTos;
            return this;
        }

        public Node build() {
            Node node = new Node();

            node.setName(this.name);
            node.setLabels(this.labels);
            node.setAffinity(this.affinity);
            node.setContainers(this.containers);
            node.setImagePullSecrets(this.imagePullSecrets);
            node.setVolumes(this.volumes);
            node.setReplicas(this.replicas);
            node.setExposedPorts(this.exposedPorts);
            node.setNodeHardware(this.nodeHardware);
            node.setHostNetwork(this.hostNetwork);
            node.setSlos(this.sloTos);

            return node;
        }
    }
}
