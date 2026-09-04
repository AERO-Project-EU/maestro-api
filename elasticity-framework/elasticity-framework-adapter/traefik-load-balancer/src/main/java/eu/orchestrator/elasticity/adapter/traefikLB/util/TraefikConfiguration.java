package eu.orchestrator.elasticity.adapter.traefikLB.util;

import eu.orchestrator.elasticity.adapter.traefikLB.model.LoadBalancerMetadata;
import eu.orchestrator.elasticity.spi.model.orchestrator.ConsulConfigTO;
import eu.orchestrator.elasticity.spi.service.orchestrator.FileInteractions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 26/7/2019
 */
public class TraefikConfiguration {

    private static final Logger logger = LogManager.getLogger(TraefikConfiguration.class);
    private static final String STATIC_CONF = "logLevel = \"DEBUG\"\n" +
            "\n" +
            "defaultEntryPoints = [\"http\"]\n" +
            "\n" +
            "[entryPoints]\n";

    private static final String DYNAMIC_CONF = "[file]\n" +
            "# rules\n" +
            "[backends]\n";

    private TraefikConfiguration() {
    }

    public static void pushStaticConfig(ConsulConfigTO consulConfig, String graphId, String graphInstanceId, String componentNodeId, LoadBalancerMetadata metadata) {

        String body = STATIC_CONF;
        body = body + "  [entryPoints.api]\n" +
                "  address = \":" + metadata.getUiPort() + "\"\n";

        for (String port : metadata.getPorts()) {
            body = body + "  [entryPoints.http" + port + "]\n" +
                    "  address = \":" + port + "\"\n";
        }

        body = body + "\n" +
                "[consul]\n" +
                "  endpoint = \"" + consulConfig.getUrl() + ":" + consulConfig.getPort() + "\"\n" +
                "  watch = true\n" +
                "  prefix = \"" + graphId + "/" + graphInstanceId + "/" + componentNodeId + "/lbConfiguration\"\n" +
                "\n" +
                "[api]\n" +
                "  entrypoint = \"api\"\n" +
                "  dashboard = true\n" +
                "  debug = true\n" +
                "[metrics]\n" +
                "  [metrics.prometheus]\n" +
                "    entryPoint = \"api\"\n" +
                "    buckets = [0.1,0.3,1.2,5.0]";

        try {
            FileInteractions.writeFile("static.toml", body);
        } catch (IOException e) {
            logger.error("Couldn't write the static.toml file: " + e.getMessage());
        }

        Runtime rt = Runtime.getRuntime();
        while (true) {
            try {
                Process pr = rt.exec("traefik_linux-amd64 storeconfig -c ./static.toml --consul.endpoint=" +
                        consulConfig.getUrl() + ":" + consulConfig.getPort());
                pr.waitFor();
                break;
            } catch (IOException e) {
                logger.error("Couldn't run the traefik bin tool IOException: " + e.getMessage());
            } catch (InterruptedException e) {
                logger.error("Couldn't run the traefik bin tool InterruptedException: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        String current_dir = System.getProperty("user.dir");
        File file = new File(current_dir + "/static.toml");
        boolean check = file.delete();
        if(check){
            logger.debug("The static.toml file is deleted");
        }else{
            logger.debug("The static.toml file is NOT deleted");
        }
    }

    public static void pushDynamicConfig(ConsulConfigTO consulConfig, String graphId, String graphInstanceId, String componentNodeId, LoadBalancerMetadata metadata, Boolean ipv6Enabled) {
        String body = DYNAMIC_CONF;
        int backendCounter = 1;

        for (String port : metadata.getPorts()) {
            body = body + "  [backends.backend" + backendCounter + "]\n";
            body = body + /* "    [backends.backend"+backendCounter+".healthcheck]\n" +
          "      path = \"/\"\n" +
          "      interval = \"10s\"\n" +*/
//                    "      [backends.backend"+backendCounter+".circuitbreaker]\n" +
//                    "      expression = \"NetworkErrorRatio() > 0.5\"\n";
            "      [backends.backend"+backendCounter+".loadbalancer]\n" +
                    "      method = \"drr\"\n";
            body = body +
                    "      [backends.backend"+backendCounter+".maxconn]\n" +
                    "      amount = 2000000\n" +
                    "      extractorfunc = \"request.host\"\n";


            for(String serverName : metadata.getServers().keySet()){
                String ip = metadata.getServers().get(serverName);
                if(ipv6Enabled){
                    ip = /*"[" + */ metadata.getServers().get(serverName) /*+ "]" */;
                }
                System.out.println("Update Dynamic ServerName: " + serverName + " IP: " + ip);
                body = body + "      [backends.backend" + backendCounter + ".servers." + serverName + "]\n" +
                        "      url = \"http://" + ip + ":" + port + "\"\n";
            }

            backendCounter++;
        }

        body = body + "\n[frontends]\n";

        for (int i = 1; i < backendCounter; i++) {
            body = body + "  [frontends.frontend" + i + "]\n" +
                    "  backend = \"backend" + i + "\"\n" +
                    "  passHostHeader = true\n" +
                    "  entrypoints = [\"http" + metadata.getPorts().get(i-1)+"\"]\n" +
                    "    [frontends.frontend" + i + ".routes.test_1]\n" +
                    "    rule = \"PathPrefix:/\"\n";
        }

        try {
            FileInteractions.writeFile("dynamic.toml", body);
        } catch (IOException e) {
            logger.error("Couldn't write the dynamic.toml file: " + e.getMessage());
        }

        Runtime rt = Runtime.getRuntime();
        while(true) {
            try {
                Process pr =
                        rt.exec("traefik_linux-amd64 storeconfig -c ./dynamic.toml --consul.prefix=" + graphId + "/" +
                                graphInstanceId + "/" + componentNodeId + "/lbConfiguration --consul.endpoint=" +
                                consulConfig.getUrl() + ":" + consulConfig.getPort());
                pr.waitFor();
                break;
            } catch (IOException e) {
                logger.error("Couldn't run the traefik bin tool IOException: " + e.getMessage());
            } catch (InterruptedException e) {
                logger.error("Couldn't run the traefik bin tool InterruptedException: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        String current_dir = System.getProperty("user.dir");
        File file = new File(current_dir + "/dynamic.toml");
        boolean check = file.delete();
        if(check){
            logger.debug("The static.toml file is deleted");
        }else{
            logger.debug("The static.toml file is NOT deleted");
        }
    }

}
