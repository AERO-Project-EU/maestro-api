package eu.orchestator.core.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Konstantinos Theodosiou.
 */
@EnableConfigurationProperties
@Component
public class RelayConfig {

    @Value("${relay.ipv4}")
    private String ipv4;

    @Value("${relay.port}")
    private String port;

    @Value("${relay.login}")
    private String login;

    @Value("${relay.password}")
    private String password;

    @Value("${relay.publicKey}")
    private String publicKey;

    @Value("${relay.peerName}")
    private String peerName;

    @Value("${relay.ipv6}")
    private String ipv6;

    @Value("${relay.cluster}")
    private String cluster;


    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getIpv6() {
        return ipv6;
    }

    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
}
