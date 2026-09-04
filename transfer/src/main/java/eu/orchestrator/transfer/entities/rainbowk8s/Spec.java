package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Spec implements Serializable {

    private List<Node> nodes;
    private List<Link> links;
    private DnsConfig dnsConfig;
    //TODO application wide services


    public List<Node> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Link> getLinks() {
        if (links == null) {
            links = new ArrayList<>();
        }
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public DnsConfig getDnsConfig() {
        return dnsConfig;
    }

    public void setDnsConfig(DnsConfig dnsConfig) {
        this.dnsConfig = dnsConfig;
    }

    public static class Builder {

        private List<Node> nodes;
        private List<Link> links;
        private DnsConfig dnsConfig;
        private List<SloTo> sloTos;

        public Spec.Builder withNodes(List<Node> nodes) {
            this.nodes = nodes;
            return this;
        }

        public Spec.Builder withLinks(List<Link> links) {
            this.links = links;
            return this;
        }

        public Spec.Builder withDnsConfig(DnsConfig dnsConfig) {
            this.dnsConfig = dnsConfig;
            return this;
        }

        public Spec.Builder withSlos(List<SloTo> sloTos) {
            this.sloTos = sloTos;
            return this;
        }

        public Spec build() {
            Spec spec = new Spec();

            spec.setNodes(this.nodes);
            spec.setLinks(this.links);
            spec.setDnsConfig(this.dnsConfig);

            return spec;
        }
    }
}
