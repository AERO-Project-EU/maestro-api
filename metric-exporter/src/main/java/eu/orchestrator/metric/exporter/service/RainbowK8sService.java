package eu.orchestrator.metric.exporter.service;

import com.google.gson.Gson;

import eu.orchestrator.metric.exporter.constant.RainbowK8sMetricIds;
import eu.orchestrator.transfer.entities.rainbowk8s.dto.RainbowMonitoringRequestDto;
import eu.orchestrator.transfer.entities.rainbowk8s.dto.RainbowMonitoringResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RainbowK8sService {

    private static final Logger logger = Logger.getLogger(RainbowK8sService.class.getName());

    private static final String PORT = "50000";
    private static final String PATH = "/get";

    public RainbowMonitoringResponseDto getMonitoringValues(String url, String name) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .postForEntity(buildUrl(url), buildBody(name), String.class);

        return new Gson().fromJson(response.getBody(), RainbowMonitoringResponseDto.class);
    }

    private String buildUrl(String url) {
        String uri = "http://[" + url + "]:" + PORT + PATH;
        logger.info("RainbowK8sService Request URL " + uri);
        return uri;
    }

    private RainbowMonitoringRequestDto buildBody(String name) {
        List<String> metricID = new ArrayList<>(1);
        List<String> metricIds = Stream.of(RainbowK8sMetricIds.values())
                .map(RainbowK8sMetricIds::getValue)
                .collect(Collectors.toList());
        metricID.addAll(metricIds);

        List<String> podName = new ArrayList<>(1);
        podName.add("%" + name + "%");

        List<String> entityType = new ArrayList<>(1);
        entityType.add("POD");

        List<String> nodes = new ArrayList<>(0);
        logger.info("RainbowK8sService - Values for body request: MetricId: " + metricID + " podName: " + podName + " entityType: " + entityType + " nodes: "
                + nodes);
        return new RainbowMonitoringRequestDto(metricID, podName, entityType, nodes);
    }

}
