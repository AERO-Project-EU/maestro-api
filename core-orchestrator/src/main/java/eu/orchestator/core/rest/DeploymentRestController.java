package eu.orchestator.core.rest;

import eu.orchestator.core.configuration.AmazonConfig;
import eu.orchestator.core.configuration.BackendConfig;
import eu.orchestator.core.configuration.ConsulConfig;
import eu.orchestator.core.configuration.GeneralConfig;
import eu.orchestator.core.configuration.IntrusionDetectionConfig;
import eu.orchestator.core.configuration.KafkaConfig;
import eu.orchestator.core.configuration.RelayConfig;
import eu.orchestator.core.configuration.SchedulerConfig;
import eu.orchestator.core.configuration.UbiDellConfig;
import eu.orchestator.core.configuration.VirtualizationManagerConfig;
import eu.orchestator.core.loops.ControlLoop;
import eu.orchestrator.collector.Collector;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.response.BasicResponseCode;
import eu.orchestrator.transfer.response.RestResponse;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/v1/deploy")
public class DeploymentRestController {

    private static final Logger logger = LogManager.getLogger(DeploymentRestController.class.getName());
    private static final Collector metricCollector = new Collector(38013);

    @Autowired
    private ConsulConfig consulConfig;

    @Autowired
    private AmazonConfig amazonConfig;

    @Autowired
    private UbiDellConfig ubidellConfig;

    @Autowired
    private RelayConfig relayConfig;

    @Autowired
    private IntrusionDetectionConfig idsConfig;

    @Autowired
    private VirtualizationManagerConfig virtualizationManagerConfig;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private BackendConfig backendConfig;

    @Autowired
    private SchedulerConfig schedulerConfig;

    @Resource(name = "elasticityFrameworkAdapters")
    List elasticityFrameworkAdapters;

    @Autowired
    private GeneralConfig nfsConfig;

    @RequestMapping(method = RequestMethod.POST)
    public RestResponse deployGraph(@RequestBody OrchestratorApplicationInstance composeObject) {
        if (null != composeObject) {
            logger.debug("\n\ndeploy() invoked: \t" + new Gson().toJson(composeObject) + "\n\n");

            //TODO make it as Thread or JPPF Task
            ControlLoop controlLoop = new ControlLoop(composeObject, consulConfig, idsConfig, virtualizationManagerConfig, kafkaConfig, backendConfig,
                    amazonConfig, ubidellConfig, relayConfig, elasticityFrameworkAdapters, nfsConfig, true, metricCollector);
            schedulerConfig.threadPoolTaskScheduler().execute(controlLoop);
            return new RestResponse(BasicResponseCode.SUCCESS, "Deployment in process....");
        }

        return new RestResponse(BasicResponseCode.EXCEPTION, "Error occurred! Please try again!");
    }

    @RequestMapping(value = "/cold", method = RequestMethod.POST)
    public RestResponse coldDeployGraph(@RequestBody OrchestratorApplicationInstance composeObject) {
        if (null != composeObject) {
            logger.debug("\n\nRestart the control loop for the graph: \t" + composeObject.getGraphID() + ":" + composeObject.getGraphInstanceID() + "\n\n");

            //TODO make it as Thread or JPPF Task
            ControlLoop controlLoop = new ControlLoop(composeObject, consulConfig, idsConfig, virtualizationManagerConfig, kafkaConfig, backendConfig,
                    amazonConfig, ubidellConfig, relayConfig, elasticityFrameworkAdapters, nfsConfig, false, metricCollector);

            schedulerConfig.threadPoolTaskScheduler().execute(controlLoop);
            return new RestResponse(BasicResponseCode.SUCCESS, "Deployment in process....");
        }

        return new RestResponse(BasicResponseCode.EXCEPTION, "Error occurred! Please try again!");
    }
}
