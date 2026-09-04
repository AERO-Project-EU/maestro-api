package eu.orchestrator.agent.configuration;

/**
 * Agent configuration.
 *
 * Deployment specific values are read from environment variables. Every value below keeps a
 * sensible default, except the secrets, which have no default at all and must be provided by the
 * environment.
 *
 * @author Vasileios Matsoukas
 */
public final class AgentConfiguration {

    private AgentConfiguration() {
    }

    //Number of iterations
    public static final int MAX_ITERATIONS = envInt("AGENT_MAX_ITERATIONS", 10);

    //Time
    public static final int MAX_WAITING_TIME = envInt("AGENT_MAX_WAITING_TIME", 60000); // 1 min
    public static final int MIN_WAITING_TIME = envInt("AGENT_MIN_WAITING_TIME", 1000); // 1 sec

    //IPs
    public static final String LOCALHOST_IP = "127.0.0.1";
    public static final String LOCALHOST_DOCKER_IP = env("AGENT_DOCKER_BRIDGE_IP", "172.17.0.1");

    //Agent metric Exporter
    public static final int AGENT_METRIC_EXPORTER_RUNNING_ON_SOCKET = envInt("AGENT_METRIC_EXPORTER_PORT", 48013);

    //Ports of the services the agent talks to on the local host
    public static final int CONSUL_PORT = envInt("AGENT_CONSUL_PORT", 8500);
    public static final String CONSUL_HOST = env("AGENT_CONSUL_HOST", "localhost");
    public static final int NETDATA_PORT = envInt("AGENT_NETDATA_PORT", 19999);
    public static final int METRIC_EXPORTER_PORT = envInt("AGENT_COMPONENT_METRIC_PORT", 15568);

    //Cloud metadata service used to retrieve the public IPv4 of the VM
    public static final String OPENSTACK_URL = env("AGENT_METADATA_URL", "");

    //Volume path on VM
    public static final String ROOT_HOST_VOLUME_PATH = env("AGENT_HOST_VOLUME_PATH", "/home/ubuntu/docker_volume/");

    //Host working directory used by the benchmark and by the SOC helper scripts
    public static final String HOST_WORKING_DIRECTORY = env("AGENT_HOST_WORKING_DIRECTORY", "/home/ubuntu");

    //Network interface the XDP DDoS blacklist is attached to
    public static final String XDP_NETWORK_INTERFACE = env("AGENT_XDP_INTERFACE", "ens3");

    //Snort alert file consumed by the intrusion detection service
    public static final String SNORT_ALERT_FILE = env("AGENT_SNORT_ALERT_FILE", "/opt/snort/logs/alert");

    //Elastic Beats used by the SOC flow
    public static final String BEATS_DOWNLOAD_BASE_URL = env("AGENT_BEATS_DOWNLOAD_BASE_URL", "https://artifacts.elastic.co/downloads/beats");
    public static final String AUDITBEAT_VERSION = env("AGENT_AUDITBEAT_VERSION", "7.17.0");
    public static final String AUDITBEAT_LEGACY_VERSION = env("AGENT_AUDITBEAT_LEGACY_VERSION", "7.11.1");
    public static final String PACKETBEAT_VERSION = env("AGENT_PACKETBEAT_VERSION", "7.17.0");

    //Kafka broker the auditbeat output is pointed to (host:port)
    public static final String AUDIT_LOGS_KAFKA_BOOTSTRAP = env("AGENT_AUDIT_LOGS_KAFKA_BOOTSTRAP", "");
    public static final String AUDIT_LOGS_KAFKA_TOPIC = env("AGENT_AUDIT_LOGS_KAFKA_TOPIC", "audit-logs");

    //Forensic (OwlH) agent
    public static final String FORENSIC_SERVER_HOST = env("AGENT_FORENSIC_SERVER_HOST", "");
    public static final String FORENSIC_AGENT_URL = env("AGENT_FORENSIC_AGENT_URL", "");
    public static final String FORENSIC_AGENT_USERNAME = env("AGENT_FORENSIC_AGENT_USERNAME", "");
    public static final String FORENSIC_AGENT_PASSWORD = env("AGENT_FORENSIC_AGENT_PASSWORD", "");

    // Key used to encrypt/decrypt the credentials the agent reads from the Consul KV store.
    public static String encryptionKey() {
        return required("AGENT_ENCRYPTION_KEY");
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    private static int envInt(String name, int defaultValue) {
        String value = env(name, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Environment variable " + name + " is not a number: " + value, exception);
        }
    }

    private static String required(String name) {
        String value = env(name, null);
        if (value == null) {
            throw new IllegalStateException("Required environment variable " + name + " is not set");
        }
        return value;
    }
}
