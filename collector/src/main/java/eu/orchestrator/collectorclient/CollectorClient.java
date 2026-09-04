package eu.orchestrator.collectorclient;

import eu.orchestrator.collector.CollectorConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Logger;

/**
 *
 * @author Panagiotis Gouvas
 */
public class CollectorClient {
    
    private static final Logger logger = Logger.getLogger(CollectorClient.class.getName());
    
    private static final String USAGE = "Usage: CollectorClient [host] [port]\n"
            + "  host  reporting server to read from (default: $COLLECTOR_CLIENT_HOST or localhost)\n"
            + "  port  reporting server port (default: $COLLECTOR_CLIENT_PORT, $COLLECTOR_PORT or 9090)";

    public static void main(String[] args) throws IOException {

        if (args.length > 0 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
            System.out.println(USAGE);
            System.exit(0);
        }

        // host and port come from the arguments, falling back to COLLECTOR_CLIENT_HOST/PORT
        String serverAddress = args.length > 0 ? args[0] : CollectorConfiguration.CLIENT_SERVER_HOST;
        int serverPort = CollectorConfiguration.CLIENT_SERVER_PORT;
        if (args.length > 1) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                System.err.println("Invalid port: " + args[1] + "\n" + USAGE);
                System.exit(1);
            }
        }

        logger.info("Connecting to collector at " + serverAddress + ":" + serverPort);
        try (Socket socket = new Socket(serverAddress, serverPort);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String answer = input.readLine();
            logger.info("Collected:\n" + answer);
        }
        System.exit(0);
    }
    
}//EoC
