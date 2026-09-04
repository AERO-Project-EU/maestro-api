package eu.orchestrator.agent.service;

import eu.orchestrator.agent.Agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Konstantinos Theodosiou.
 */
public class IntrusionDetectionService {

    private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());
    String alertFilePath;
    AlertMode alertMode;
    BufferedReader bufferedReader = null;

    public enum AlertMode {

        FULL("Full"),
        FAST("Fast");

        private final String friendlyName;

        AlertMode(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public IntrusionDetectionService(String logPath, AlertMode alertMode) {
        this.alertFilePath = logPath;
        this.alertMode = alertMode;

        try {
            this.bufferedReader = new BufferedReader(new FileReader(this.alertFilePath));
        } catch (FileNotFoundException exception) {
            LOGGER.log(Level.SEVERE, "Exception : {0}", exception);
        }
    }

    public boolean updateRuleSetFile(String ruleSetPath, List<String> rules) {
        String tmpFile = "/tmp/tmp.rules";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            for (String rule : rules) {
                writer.write(rule + "\n");
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Exception : {0}", exception);
            return false;
        }

        File file = new File(tmpFile);
        File file2 = new File(ruleSetPath);
        boolean success = file.renameTo(file2);
        if (!success) {
            // File was not successfully renamed
            return false;
        }

        return true;
    }

    public String readNextAlert() {
        String alert = "";
        if (alertMode.getFriendlyName().equals(AlertMode.FULL.getFriendlyName())) {
            alert = readFull();
        } else {
            alert = readFast();
        }
        return alert;
    }

    private String readFull() {
        String alert = "";

        while (true) {
            try {
                String line = "";
                line = this.bufferedReader.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                line += "\n";
                alert += line;
            } catch (IOException exception) {
                LOGGER.log(Level.SEVERE, "Exception : {0}", exception);
                break;
            }
        }
        return alert;
    }

    private String readFast() {
        String alert = "";

        try {
            alert = this.bufferedReader.readLine();
            if (alert != null && !alert.isEmpty()) {
                alert += "\n";
            }

        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Exception : {0}", exception);
        }

        return alert;
    }
}