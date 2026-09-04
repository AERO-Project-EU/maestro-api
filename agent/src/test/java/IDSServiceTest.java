import eu.orchestrator.agent.service.IntrusionDetectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * @author Konstantinos Theodosiou
 */
public class IDSServiceTest {

    public static void main(String[] args) {

        //String topicName = "prometheus-results";
        String logPath = "/data/snort/logs/alert";

        IntrusionDetectionService intrusionDetectionService = new IntrusionDetectionService(logPath, IntrusionDetectionService.AlertMode.FAST);

        String rulePath = "/data/snort/config/rules/local.rules";
        List<String> rules = new ArrayList<>();
        rules.add("alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"Pinging...\";sid:1000004;)\n");

        intrusionDetectionService.updateRuleSetFile(rulePath, rules);

        Scanner scanner = new Scanner(System.in);
        String alert = "";
        try {
            while (true) {
                System.out.print("Press enter to get the next possible alert: \n");

                scanner.nextLine();
                System.out.println();
                alert = intrusionDetectionService.readNextAlert();

                if (alert == null || alert.isEmpty()) {
                    System.out.println("\033[1;32mThere isn't a new Alert!\033[0m");
                } else {
                    System.out.println("New Alert: \n\033[1;31m" + alert + "\033[0m");
                }
            }
        } catch (IllegalStateException | NoSuchElementException exception) {
            // System.in has been closed
            System.out.println("System.in was closed; exiting");
        }
    }
}