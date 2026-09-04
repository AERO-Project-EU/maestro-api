package eu.orchestrator.transfer.entities.dnsServer;

import java.io.Serializable;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 12/3/2019
 */
public class DNSEntryTO implements Serializable {

    private String subDomain;
    private String ip;
    private String fullDomain;
    private CredentialsTO credentials;

    public String getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getFullDomain() {
        return fullDomain;
    }

    public void setFullDomain(String fullDomain) {
        this.fullDomain = fullDomain;
    }

    public CredentialsTO getCredentials() {
        return credentials;
    }

    public void setCredentials(CredentialsTO credentials) {
        this.credentials = credentials;
    }
}
