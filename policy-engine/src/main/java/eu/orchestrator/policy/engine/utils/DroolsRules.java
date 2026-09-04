package eu.orchestrator.policy.engine.utils;

import eu.orchestrator.policy.engine.configuration.DroolsConfiguration;
import eu.orchestrator.policy.engine.configuration.KafkaConfiguration;
import eu.orchestrator.transfer.entities.policyEngine.ActionType;
import eu.orchestrator.transfer.entities.policyEngine.DroolsAction;
import eu.orchestrator.transfer.entities.policyEngine.PolicyModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * @author Panagiotis Parthenis
 */
public class DroolsRules {

  private static final Logger LOGGER = Logger.getLogger(DroolsRules.class.getName());

  private static final String RULE_START = "package eu.orchestrator.policy.engine.model;\n" +
      "import \teu.orchestrator.policy.engine.action.ActionHandler;\n" +
      "\n" +
      "declare PrometheusMetricAlert\n" +
      "   @role( event )\n" +
      "end\n\n" +
      "rule \"@RULENAME 1\"\n" +
      "when\n" +
      "  $e1:PrometheusMetricAlert( graphHexID == \"@GRAPHID\" && graphInstanceHexID == \"@GRAPHINSTANCEID\" && alertName == \"@ALERTNAME\" ) from entry-point \"MonitoringStream\"\n" +
      "  not(PrometheusMetricAlert( graphHexID == \"@GRAPHID\" && graphInstanceHexID == \"@GRAPHINSTANCEID\" && alertName == \"@ALERTNAME\" , this before[ 2s,@INERTIA ] $e1) from entry-point \"MonitoringStream\") \n" +
      "then\n" +
      "  System.out.println(\"Trigger action\");\n";

  private static final String RULE_END = "end\n\n" +
      "\n" +
      "rule \"@RULENAME 2\"\n" +
      "when\n" +
      "  $e1: PrometheusMetricAlert( graphHexID == \"@GRAPHID\" && graphInstanceHexID == \"@GRAPHINSTANCEID\" && alertName == \"@ALERTNAME\" ) from entry-point \"MonitoringStream\"\n" +
      "  $e2: PrometheusMetricAlert( graphHexID == \"@GRAPHID\" && graphInstanceHexID == \"@GRAPHINSTANCEID\" && alertName == \"@ALERTNAME\", this after[ 2s,@INERTIA ] $e1 ) from entry-point \"MonitoringStream\" \n" +
      "then\n" +
      "  System.out.println(\"Ignore action\");\n" +
      "  retract($e2);\n" +
      "end\n\n";

  /**
   * This method add a rule into a drools drl file. If the file not exist then it created
   *
   * @param droolsConfiguration
   * @param kafkaConfiguration
   * @param policyModel
   * @return
   * @throws IOException
   */
  public static String createRule(DroolsConfiguration droolsConfiguration, KafkaConfiguration kafkaConfiguration, PolicyModel policyModel) throws IOException {
    String fileName = policyModel.getGraphHexID() + "_" + policyModel.getGraphInstanceHexID() + "_" + policyModel.getPolicyHexID();
    File droolsFile = new File(droolsConfiguration.getFilePath() + fileName + ".drl");
    String currentRule = "";

    if (droolsFile.exists()) {
      LOGGER.info("File : " + droolsFile.getAbsolutePath() + " All Ready exist!");
    } else {
      currentRule = RULE_START;
    }

    for (DroolsAction droolsAction : policyModel.getDroolsActions()) {
      if ((droolsAction.getRuleAction() != ActionType.pushIntoKafkaTopic) && (droolsAction.getRuleAction() != ActionType.pushIntoRabbitMQQueue)) {
        droolsAction.setTopicUrl(kafkaConfiguration.getUrl());
        droolsAction.setTopicPort(kafkaConfiguration.getPort());
        droolsAction.setTopicName(kafkaConfiguration.getOrchestratorTopicName());
      }

      if (droolsAction.getRuleAction() == ActionType.info) {
        droolsAction.setTopicName(kafkaConfiguration.getInfoTopicName());
      }

      currentRule = currentRule + "  insertLogical(new ActionHandler(\"" + droolsAction.getTopicUrl() + "\", \"" + droolsAction.getTopicPort() + "\",\"" + droolsAction.getTopicName() + "\",\"" + policyModel.getGraphHexID() + ":" + policyModel.getGraphInstanceHexID() + ":" + droolsAction.getComponentHexID() + "\",\"" + droolsAction.getRuleAction().name() + "\",\"" + droolsAction.getContext() + "\"));\n";
    }

    currentRule = currentRule + RULE_END;
    currentRule = currentRule.replaceAll("@RULENAME", policyModel.getPolicyName());
    currentRule = currentRule.replaceAll("@INERTIA", policyModel.getDroolsInertialPeriod() + "m");
    currentRule = currentRule.replaceAll("@METRICEXPRESSION", policyModel.getPrometheusPolicyExpression());
    currentRule = currentRule.replaceAll("@ALERTNAME", policyModel.getPolicyHexID());
    currentRule = currentRule.replaceAll("@GRAPHID", policyModel.getGraphHexID());
    currentRule = currentRule.replaceAll("@GRAPHINSTANCEID", policyModel.getGraphInstanceHexID());

    if (droolsFile.exists()) {
      LOGGER.info("File : " + droolsFile.getAbsolutePath() + " All Ready exist!");
    } else {

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(droolsFile))) {
        writer.write(currentRule);
      } catch (Exception e) {
        LOGGER.info("Cannot write file");
      }

      LOGGER.info("File : " + droolsFile.getAbsolutePath() + " created!");
    }

    return droolsFile.getAbsolutePath();
  }

  /**
   * This method delete drools file.
   *
   * @throws IOException
   */
  public static void deleteAllRule(String filePath) {
    Path path = Paths.get(filePath);
    try {
      Files.delete(path);
    } catch (IOException e) {
      LOGGER.info("Cannot Deleted");
    }
  }

  /**
   * This method checks if drools file (.drl) exist.
   *
   * @param droolsConfiguration
   * @param policyModel
   * @throws IOException
   */
  public static boolean checkIfContainsRulesName(DroolsConfiguration droolsConfiguration, PolicyModel policyModel) {
    String fileName = policyModel.getGraphHexID() + "_" + policyModel.getGraphInstanceHexID() + "_" + policyModel.getPolicyHexID();
    File droolsFile = new File(droolsConfiguration.getFilePath() + fileName + ".drl");
    return droolsFile.exists();

  }
}
