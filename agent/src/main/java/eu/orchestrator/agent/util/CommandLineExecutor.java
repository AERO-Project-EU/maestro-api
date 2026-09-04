package eu.orchestrator.agent.util;

import eu.orchestrator.agent.Agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 22/3/2019
 */
public class CommandLineExecutor {

    private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());

    private CommandLineExecutor() {
    }

    /**
     * This is a bash command executor with pipes.
     *
     */
    public static String multiLine(String[] command) {
        StringBuffer output = new StringBuffer();
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            if (null == line) {
                BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String lineError = "";
                while ((lineError = readerError.readLine()) != null) {
                    output.append(lineError + "\n");
                }
            }
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Exception : ", exception);
            return exception.getMessage();
        }
        return output.toString();
    }

    /**
     * This is a bash command executor for single commands.
     *
     */
    public static void singleLine(String command) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(command);
            process.waitFor();
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Exception :", exception);

        } catch (InterruptedException exception) {
            LOGGER.log(Level.SEVERE, "Exception :", exception);
            Thread.currentThread().interrupt();

        }
    }

}
