package eu.orchestator.core;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogNode;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.kv.model.GetValue;
import eu.orchestator.core.configuration.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Konstantinos Theodosiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {eu.orchestator.core.Application.class})
public class ConsulTests {

    @Autowired
    ConsulConfig consulConfig;

    @Autowired
    IntrusionDetectionConfig idsConfig;

    @Autowired
    VirtualizationManagerConfig virtualizationManagerConfig;

    @Autowired
    KafkaConfig kafkaConfig;

    @Autowired
    private BackendConfig backendConfig;

    @Test
//    @Ignore
    public void queryKValues(){

        String graphID = "CMSApp";
        String graphInstanceID = "kon123";
        String componentNodeId = "WordPress" + "LB";

        //remove the IPs from the traefik key value path
        ConsulClient consulClient = new ConsulClient(System.getProperty("consul.host", "localhost"));

        String KVLBConfigPath = graphID + "/" + graphInstanceID + "/" + componentNodeId + "/" + "lbConfiguration" + "/backends";
        Response<List<GetValue>> KVResponseBinary = consulClient.getKVValues(KVLBConfigPath);
        List<String> keysToDelete = new ArrayList<>();

        String nodeName = "cmsapp-kon123-wordpress-wordpress";
        String pattern = "(" + KVLBConfigPath  + "/backend[0-9]+/servers/" + nodeName + "/)";
        Pattern r = Pattern.compile(pattern);



        for(GetValue value : KVResponseBinary.getValue()){
            Matcher m = r.matcher(value.getKey());
            if(m.find()){
                if(!keysToDelete.contains(m.group(0))) {
                    keysToDelete.add(m.group());
                }
            }
        }

        for(String keyToDelete : keysToDelete){
            consulClient.deleteKVValues(keyToDelete);
            System.out.println(keyToDelete);
        }
    }

    @Test
//    @Ignore
    public void queryCatalogNodes(){
        String graphId = "GP8DE521wT";
        String graphInstanceId = "Kky2FQ1H7T";
        String componentNodeId = "fy29cAmiMM";
        String componentNodeInstanceId = "nB0NzmsPMq";

        ConsulClient consulClient = new ConsulClient(consulConfig.getUrl());

        String nodeName = graphId + "-" + graphInstanceId + "-" + componentNodeId + "-" + componentNodeInstanceId;
        Response<CatalogNode> response =  consulClient.getCatalogNode(nodeName, QueryParams.DEFAULT);

        //Check if the response has any values, if it's emtpy
        // means that has no running service
        if (response == null || response.getValue() == null || response.getValue().getNode() == null ||
                response.getValue().getNode().getAddress() == null || response.getValue().getNode().getAddress().isEmpty()) {
            System.out.println("Couldn't get a proper response from the consul NODES Service");
        }else{
            System.out.println(response.getValue().getNode().getAddress());
        }
    }

    @Test
    @Ignore
    public void queryServices(){
        String graphId = "CMSApp";
        String graphInstanceId = "konstheo3";
        String serviceName = "WordPress";

        //TODO change the componenNodeName to componentNodeID
        ConsulClient consulClient = new ConsulClient(consulConfig.getUrl());
        Response<List<HealthService>> response = consulClient.getHealthServices(graphId + ":" + graphInstanceId + ":" + serviceName, true, QueryParams.DEFAULT);


        //Check if the response has any values, if it's emtpy
        // means that has no running service
        if (response == null || response.getValue() == null || response.getValue().isEmpty()) {
            System.out.println("response failed");
            return;
        }

//        LoadBalancerConfig loadBalancerConfig = new LoadBalancerConfig();


        //Search for the IPs of all the nodes that having that service running healthy
        List<HealthService> healthServiceList = response.getValue();
        for (HealthService healthService : healthServiceList) {
//            loadBalancerConfig.getIps().add("" + healthService.getNode().getAddress());
            System.out.println(healthService.getNode().getAddress());
        }



    }
}
