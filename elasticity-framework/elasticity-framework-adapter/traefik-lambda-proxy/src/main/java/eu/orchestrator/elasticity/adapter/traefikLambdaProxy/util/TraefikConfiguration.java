package eu.orchestrator.elasticity.adapter.traefikLambdaProxy.util;

import eu.orchestrator.elasticity.adapter.traefikLambdaProxy.model.LambdaProxyBackend;
import eu.orchestrator.elasticity.adapter.traefikLambdaProxy.model.LambdaProxyMetadata;
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

    public static void pushLambdaProxyStaticConfig(ConsulConfigTO consulConfig, String graphId, String graphInstanceId, String componentNodeId, LambdaProxyMetadata metadata) {

        String body = STATIC_CONF;
//        body = body + "[traefikLog]\n" +
//                "\tfilePath = \"/traefik.log\"\n";

        body = body + "  [entryPoints.api]\n" +
                "  address = \":" + metadata.getUiPort() + "\"\n";

        for(LambdaProxyBackend backend : metadata.getLambdaProxyBackends().values()){
            if(backend.getBalanceType().getBalanceType().equals(LambdaProxyBackend.BalanceType.HTTP.name())){
                body = body + "  [entryPoints.http" + backend.getBackendPort() + "]\n" +
                        "  address = \":" + backend.getBackendPort() + "\"\n";
            }else{
                body = body + "  [entryPoints.grpc" + backend.getBackendPort() + "]\n" +
                        "  address = \":" + backend.getBackendPort() + "\"\n";
            }
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
        while(true) {
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

    public static void pushLambdaProxyDynamicConfig(ConsulConfigTO consulConfig, String graphId, String graphInstanceId, String componentNodeId, LambdaProxyMetadata metadata, Boolean ipv6Enabled) {
        String body = DYNAMIC_CONF;
        String hostIP = metadata.getPrivateIP();
        if (ipv6Enabled) {
            hostIP = metadata.getIpv6Address();
        }

        for (LambdaProxyBackend backend : metadata.getLambdaProxyBackends().values()) {
            body = body + "  [backends." + backend.getBackendName() + "]\n";
            body = body +
                    "      [backends." + backend.getBackendName() + ".circuitbreaker]\n" +
                    "      expression = \"NetworkErrorRatio() > 0.5\"\n";


            if (backend.getBalanceType().getBalanceType().equals(LambdaProxyBackend.BalanceType.HTTP.name())) {
                for (String serverName : backend.getServers().keySet()) {
                    String ip = backend.getServers().get(serverName);
                    if (ipv6Enabled) {
                        ip = /*"[" + */backend.getServers().get(serverName) /*+ "]"*/;
                    }

                    System.out.println("Update Dynamic ServerName: " + serverName + " IP: " + ip);
                    body = body + "      [backends." + backend.getBackendName() + ".servers." + serverName + "]\n" +
                            "      url = \"http://" + ip + ":" + backend.getBackendPort() + "\"\n";
                }
            } else {
                for (String serverName : backend.getServers().keySet()) {
                    String ip = backend.getServers().get(serverName);
                    System.out.println("Update Dynamic ServerName: " + serverName + " IP: " + ip);
                    body = body + "      [backends." + backend.getBackendName() + ".servers." + serverName + "]\n" +
                            "      url = \"h2c://" + ip + ":" + backend.getBackendPort() + "\"\n";
                }
            }
        }

        body = body + "\n[frontends]\n";
        for (LambdaProxyBackend backend : metadata.getLambdaProxyBackends().values()) {
            if (backend.getBalanceType().getBalanceType().equals(LambdaProxyBackend.BalanceType.HTTP.name())) {
                body = body + "  [frontends." + backend.getBackendName() + "]\n" +
                        "  backend = \"" + backend.getBackendName() + "\"\n" +
                        "  passHostHeader = true\n" +
                        "  entrypoints = [\"http" + backend.getBackendPort() + "\"]\n" +
                        "    [frontends." + backend.getBackendName() + ".routes.test_1]\n" +
                        "    rule = \"PathPrefix:/\"\n";
            } else {
                body = body + "  [frontends." + backend.getBackendName() + "]\n" +
                        "  backend = \"" + backend.getBackendName() + "\"\n";
                body += "  entrypoints = [\"grpc" + backend.getBackendPort() + "\"]\n";
                body += "  [frontends." + backend.getBackendName() + ".routes.test]\n" +
                        "  rule = \"Host:" + hostIP + "\"\n";
            }
        }

        try {
            FileInteractions.writeFile("dynamic.toml", body);
        } catch (IOException e) {
            logger.error("Couldn't write the static.toml file: " + e.getMessage());
        }

        Runtime rt = Runtime.getRuntime();
        while (true) {
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
