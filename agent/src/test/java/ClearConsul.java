import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.Node;

import java.util.List;

/**
 * @author Panagiotis Parthenis
 */
public class ClearConsul {

    public static void main(String[] args) {
        ConsulClient consulClient = new ConsulClient(System.getProperty("consul.host", "localhost"));
        Response<List<Node>> allNodes = consulClient.getCatalogNodes(QueryParams.DEFAULT);
        for (Node node : allNodes.getValue()) {
            consulClient.agentForceLeave(node.getNode());
        }

        //consulClient.deleteKVValues("");
    }
}
