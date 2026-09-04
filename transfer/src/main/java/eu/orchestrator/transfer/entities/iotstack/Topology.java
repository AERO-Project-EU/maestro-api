package eu.orchestrator.transfer.entities.iotstack;

import java.io.Serializable;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 */
public class Topology implements Serializable {
    private String id;
    private List<Peer> peers;
    private List<Nodestat> nodes;


    public Topology() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }

    public List<Nodestat> getNodes() {
        return nodes;
    }

    public void setNodes(List<Nodestat> nodes) {
        this.nodes = nodes;
    }
}
