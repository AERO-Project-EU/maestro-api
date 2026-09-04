package eu.orchestrator.policy.engine.utils;

import eu.orchestrator.policy.engine.configuration.PrometheusConfiguration;
import eu.orchestrator.transfer.entities.policyEngine.PolicyModel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Panagiotis Parthenis
 */
public class PrometheusRules {

  private PrometheusRules() {

  }

  private static final Logger LOGGER = Logger.getLogger(PrometheusRules.class.getName());

  private static final String RULE_CREATION = "groups:\n" +
      "- name: \"example\"\n" +
      "  rules: \n";

  private static final String RULE_BODY = "  - alert: \"@PROMETHEUSNAME\"\n" +
      "    expr: \"@PROMETHEUSEXPRESSION\"\n" +
      "    for: \"@PROMETHEUSPERIODICALLY\"\n" +
      "    labels:\n" +
      "      severity: \"page\"\n" +
      "    annotations:\n" +
      "      summary: \"@PROMETHEUSID\"\n";

  /**
   * This method add a RULE_BODY into a prometheus yaml file. If the file not exist then it created
   *
   * @param prometheusConfiguration
   * @param policyModel
   * @throws IOException
   */
  public static void createRule(PrometheusConfiguration prometheusConfiguration, PolicyModel policyModel) {
    String fileName = policyModel.getGraphHexID() + "_" + policyModel.getGraphInstanceHexID();
    File file = new File(prometheusConfiguration.getFilePath() + fileName + ".yml");
    String currentRule;

    if (file.exists()) {
      currentRule = RULE_BODY;
    } else {
      currentRule = RULE_CREATION + RULE_BODY;
    }

    if (policyModel.getPrometheusPolicyExpression().contains("\"")) {
      policyModel.setPrometheusPolicyExpression(policyModel.getPrometheusPolicyExpression().replace("\"", "'"));
    }

    currentRule = currentRule.replaceAll("@PROMETHEUSNAME", policyModel.getPolicyHexID());
    currentRule = currentRule.replaceAll("@PROMETHEUSEXPRESSION", policyModel.getPrometheusPolicyExpression());
    currentRule = currentRule.replaceAll("@PROMETHEUSPERIODICALLY", policyModel.getPrometheusPolicyPeriod() + "s");
    currentRule = currentRule.replaceAll("@PROMETHEUSID", policyModel.getGraphHexID() + ":" + policyModel.getGraphInstanceHexID());

    if (file.exists()) {

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
        writer.write(currentRule);
        LOGGER.log(Level.INFO,"File : {0} updated !", file.getAbsolutePath());

      } catch (Exception e) {
        LOGGER.log(Level.SEVERE,"File : {0} CANNOT updated !", file.getAbsolutePath());
      }

    } else {

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        writer.write(currentRule);
        LOGGER.log(Level.INFO,"File : {0} created !", file.getAbsolutePath());

      } catch (Exception e) {
        LOGGER.log(Level.SEVERE,"File : {0} CANNOT updated !", file.getAbsolutePath());
      }

    }
  }

  /**
   * This method add the prometheus file name into prometheus configuration.
   *
   * @param prometheusConfiguration
   * @param policyModel
   * @throws IOException
   */
  public static void updateConfigurationCreation(PrometheusConfiguration prometheusConfiguration, PolicyModel policyModel) throws IOException {
    String fileName = policyModel.getGraphHexID() + "_" + policyModel.getGraphInstanceHexID();
    fileName = fileName + ".yml";
    String content = new String(Files.readAllBytes(Paths.get(prometheusConfiguration.getFilePath() + "prometheus.yml")));

    if (!content.contains(fileName)) {
      content = content.replace("  - \"policy_rules.yml\"\n", "  - \"policy_rules.yml\"\n  - \"" + fileName + "\"\n");

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(prometheusConfiguration.getFilePath() + "prometheus.yml"))) {
        writer.write(content);
        LOGGER.log(Level.INFO,"File : {0} updated prometheus configuration !", fileName);

      } catch (Exception e) {
        LOGGER.log(Level.SEVERE,"File : {0} CANNOT updated prometheus configuration !", fileName);
      }
    }
  }

  /**
   * This method remove a RULE_BODY from a prometheus yml file.
   *
   * @param prometheusConfiguration
   * @param policyModel
   * @throws IOException
   */
  public static void deleteRule(PrometheusConfiguration prometheusConfiguration, PolicyModel policyModel) throws IOException {
    String fileName = policyModel.getGraphHexID() + "_" + policyModel.getGraphInstanceHexID();
    File inputFile = new File(prometheusConfiguration.getFilePath() + fileName + ".yml");
    File tempFile = new File(prometheusConfiguration.getFilePath() + "temp.yml");

    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile))) {
      try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {

        String currentLine;
        boolean persistenceLines = true;

        while ((currentLine = br.readLine()) != null) {
          if (currentLine.compareTo("  - alert: \"" + policyModel.getPolicyHexID() + "\"") == 0) {
            persistenceLines = false;
          } else {
            if (currentLine.contains("- alert")) {
              persistenceLines = true;
            }
          }
          if (persistenceLines) {
            bufferedWriter.write(currentLine + System.getProperty("line.separator"));
          }
        }
        bufferedWriter.flush();

        if (!tempFile.renameTo(inputFile)) {
          LOGGER.log(Level.SEVERE,"File : {0} cannot updated!", inputFile);
        }

      } catch (Exception e) {
        LOGGER.log(Level.SEVERE,"File Update Exception: {0} ", e);

      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE,"File Update Exception: {0} ", e);
    }
  }

  /**
   * This method delete prometheus yml file.
   *
   * @param prometheusConfiguration
   * @param policyModel
   * @throws IOException
   */
  public static void deleteAllRule(PrometheusConfiguration prometheusConfiguration, PolicyModel policyModel) {
    String fileName = policyModel.getGraphHexID() + "_" + policyModel.getGraphInstanceHexID();

    //delete prometheus RULE_BODY
    Path prometheusFilePath = Paths.get(prometheusConfiguration.getFilePath() + fileName + ".yml");

    try {
      Files.delete(prometheusFilePath);

    } catch (IOException e) {
      LOGGER.log(Level.SEVERE,"File Update Exception: {0} ", prometheusFilePath);

    }
  }



  /**
   * This method remove the prometheus file name into prometheus configuration.
   *
   * @param prometheusConfiguration
   * @param policyModel
   * @throws IOException
   */
  public static void updateConfigurationDeletion(PrometheusConfiguration prometheusConfiguration, PolicyModel
      policyModel) throws IOException {
    String fileName = policyModel.getGraphHexID() + "_" + policyModel.getGraphInstanceHexID();
    String content = new String(Files.readAllBytes(Paths.get(prometheusConfiguration.getFilePath() + "prometheus.yml")));

    if (content.contains(fileName)) {

      content = content.replace("  - \"" + fileName + ".yml\"\n", "");

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(prometheusConfiguration.getFilePath() + "prometheus.yml"))) {
        writer.write(content);
        LOGGER.log(Level.INFO,"File : {0} updated !", fileName);

      } catch (Exception e) {
        LOGGER.log(Level.SEVERE,"File Update Exception: {0} ", fileName);

      }
    }

  }

}
