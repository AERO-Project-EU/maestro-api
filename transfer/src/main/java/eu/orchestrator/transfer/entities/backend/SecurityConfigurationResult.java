package eu.orchestrator.transfer.entities.backend;


/**
 * @author Panagiotis Parthenis.
 */
public class SecurityConfigurationResult {

    private HashType hashType;
    private String value;

    public HashType getHashType() {
        return hashType;
    }

    public void setHashType(HashType hashType) {
        this.hashType = hashType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SecurityConfigurationResult{"
                + "hashType=" + hashType
                + ", value='" + value + '\''
                + '}';
    }

    public enum HashType {
        DOCKER_CREDENTIALS,
        DOCKER_IMAGE,
        DOCKER_PORT,
        DOCKER_ENV,

        AGENT_SERVICE,
        NETDATA_SERVICE,
        CONSUL_SERVICE,
        SNORT_SERVICE,
        EBPF_SERVICE;
    }
}
