package eu.orchestator.core.model.orchestrator;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 6/5/20
 */
public enum IPType {
    IPv6(1),
    IPv4(2),
    PublicIP(3);

    private final int ipType;

    IPType(int ipType) {
        this.ipType = ipType;
    }

    public int getIpType() {
        return ipType;
    }
}
