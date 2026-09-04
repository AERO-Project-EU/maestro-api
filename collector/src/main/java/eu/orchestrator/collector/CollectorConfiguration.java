package eu.orchestrator.collector;

/**
 * Collector configuration.
 *
 * Deployment specific values are read from environment variables, each one keeping the previous
 * hardcoded value as its default.
 *
 * @author Vasileios Matsoukas
 */
public final class CollectorConfiguration {

    private CollectorConfiguration() {
    }

    //Port the reporting server binds to when no port is passed explicitly
    public static final int DEFAULT_REPORTING_PORT = envInt("COLLECTOR_PORT", 9090);

    //Reporting server the sample client connects to
    public static final String CLIENT_SERVER_HOST = env("COLLECTOR_CLIENT_HOST", "localhost");
    public static final int CLIENT_SERVER_PORT = envInt("COLLECTOR_CLIENT_PORT", DEFAULT_REPORTING_PORT);

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
}
