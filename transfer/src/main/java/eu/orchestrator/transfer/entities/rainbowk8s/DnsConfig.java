package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DnsConfig implements Serializable {

    private String dnsPolicy;
    private List<String> nameservers;


    public String getDnsPolicy() {
        return dnsPolicy;
    }

    public void setDnsPolicy(String dnsPolicy) {
        this.dnsPolicy = dnsPolicy;
    }

    public List<String> getNameservers() {
        if(nameservers == null) {
            nameservers = new ArrayList<>();
        }
        return nameservers;
    }

    public void setNameservers(List<String> nameservers) {
        this.nameservers = nameservers;
    }


    public static class Builder {
        private String dnsPolicy;
        private List<String> nameservers;

        public DnsConfig.Builder withDnsPolicy(String dnsPolicy) {
            this.dnsPolicy = dnsPolicy;
            return this;
        }

        public DnsConfig.Builder withNameservers(List<String> nameservers) {
            this.nameservers = nameservers;
            return this;
        }

        public DnsConfig build() {
            DnsConfig dnsConfig = new DnsConfig();

            dnsConfig.setDnsPolicy(this.dnsPolicy);
            dnsConfig.setNameservers(this.nameservers);

            return dnsConfig;
        }
    }
}
