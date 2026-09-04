/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.orchestrator.policy.engine.api.rest;

import eu.orchestrator.policy.engine.model.Drools;
import eu.orchestrator.policy.engine.model.PrometheusMetricAlert;
import eu.orchestrator.policy.engine.service.DroolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@RestController
@RequestMapping("/api/v1")
public class ImporterRestController {

  private static final Logger LOGGER = Logger.getLogger(ImporterRestController.class.getName());

  @Autowired
  private DroolsService droolsService;

  @Autowired
  private Drools drools;

  /**
   * Endpoint to insert
   */
  @RequestMapping(value = "", method = RequestMethod.POST)
  public void getMonitoringAlert(@RequestBody Object object) {
    HashMap prometheusAlertMap = (HashMap) object;

    //get alert metric name
    String groupLabels = prometheusAlertMap.get("groupLabels").toString();
    int startFrom = groupLabels.indexOf('=');
    int endAt = groupLabels.lastIndexOf('}');
    groupLabels = groupLabels.substring(startFrom + 1, endAt);

    //get expression
    String commonAnnotations = prometheusAlertMap.get("commonAnnotations").toString();
    startFrom = commonAnnotations.indexOf('=');
    endAt = commonAnnotations.lastIndexOf('}');
    commonAnnotations = commonAnnotations.substring(startFrom + 1, endAt);

    String[] temp = commonAnnotations.split(":");
    PrometheusMetricAlert prometheusMetricAlert = new PrometheusMetricAlert();
    prometheusMetricAlert.setAlertName(groupLabels);
    prometheusMetricAlert.setGraphHexID(temp[0]);
    prometheusMetricAlert.setGraphInstanceHexID(temp[1]);

    String hashKey = prometheusMetricAlert.getGraphHexID() + ":" + prometheusMetricAlert.getGraphInstanceHexID() + ":" + prometheusMetricAlert.getAlertName();
    insertIntoWorkingMemory(hashKey, prometheusMetricAlert);
  }

  @RequestMapping(value = "/input", method = RequestMethod.POST)
  public void getMonitoringAlert(@RequestBody PrometheusMetricAlert prometheusMetricAlert) {
    String hashKey = prometheusMetricAlert.getGraphHexID() + ":" + prometheusMetricAlert.getGraphInstanceHexID() + ":" + prometheusMetricAlert.getAlertName();
    insertIntoWorkingMemory(hashKey, prometheusMetricAlert);
  }

  private void insertIntoWorkingMemory(String hashKey, PrometheusMetricAlert prometheusMetricAlert) {
    if (droolsService.isKieSessionActive(hashKey)) {
      LOGGER.info("-------- Insert Fact --------");
      LOGGER.info(prometheusMetricAlert.toString());
      droolsService.insertFactsIntoWorkingMemory(prometheusMetricAlert, hashKey);
      LOGGER.info("-----------------------------");

      //TODO  fire until halt
      LOGGER.info("-------- Fire Rule --------");
      droolsService.fireRulesAll(hashKey);
      LOGGER.info("---------------------------");

    } else {
      LOGGER.info("-------- Graph undeployed --------");
      LOGGER.info("KieSession expired !");
    }
  }
}
