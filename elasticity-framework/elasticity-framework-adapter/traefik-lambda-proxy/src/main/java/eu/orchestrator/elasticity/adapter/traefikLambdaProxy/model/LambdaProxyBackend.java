package eu.orchestrator.elasticity.adapter.traefikLambdaProxy.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 25/7/2019
 */
public class LambdaProxyBackend {

    private String backendName;
    private String backendPort;
    //Map with server name and ip
    private Map<String, String> servers;
    private BalanceType balanceType;

    public enum BalanceType {
        HTTP("HTTP"),
        gRPC("gRPC");

        private String balanceType;

        BalanceType(String balanceType) {
            this.balanceType = balanceType;
        }

        public String getBalanceType() {
            return balanceType;
        }

        protected void setBalanceType(String balanceType) {
            this.balanceType = balanceType;
        }
    }

    public LambdaProxyBackend(){
        servers = new HashMap<>();
    }

    public String getBackendName() {
        return backendName;
    }

    public void setBackendName(String backendName) {
        this.backendName = backendName;
    }

    public String getBackendPort() {
        return backendPort;
    }

    public void setBackendPort(String backendPort) {
        this.backendPort = backendPort;
    }

    public Map<String, String> getServers() {
        return servers;
    }

    public void setServers(Map<String, String> servers) {
        this.servers = servers;
    }

    public BalanceType getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(BalanceType balanceType) {
        this.balanceType = balanceType;
    }
}
