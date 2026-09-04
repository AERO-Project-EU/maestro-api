package eu.orchestrator.elasticity.adapter.traefikLambdaProxy.model;

import eu.orchestrator.elasticity.spi.model.orchestrator.ControllerMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 25/7/2019
 */
public class LambdaProxyMetadata extends ControllerMetadata {

    //Lambda Proxy Backend name, LambdaProxyBackend object
    private Map<String, LambdaProxyBackend> lambdaProxyBackends;;
    private String uiPort;
    private String privateIP;
    private String ipv6Address;

    public LambdaProxyMetadata() {
        lambdaProxyBackends = new HashMap<>();
    }

    public Map<String, LambdaProxyBackend> getLambdaProxyBackends() {
        return lambdaProxyBackends;
    }

    public void setLambdaProxyBackends(Map<String, LambdaProxyBackend> lambdaProxyBackends) {
        this.lambdaProxyBackends = lambdaProxyBackends;
    }

    public String getUiPort() {
        return uiPort;
    }

    public void setUiPort(String uiPort) {
        this.uiPort = uiPort;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    public void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }

    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }
}
