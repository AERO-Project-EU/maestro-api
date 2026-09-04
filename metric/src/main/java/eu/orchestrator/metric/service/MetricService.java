package eu.orchestrator.metric.service;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.metric.dto.MetricExpressionTo;
import eu.orchestrator.metric.dto.MetricForConversion;
import eu.orchestrator.metric.dto.MetricTo;
import eu.orchestrator.metric.dto.RestResponseSpa;
import eu.orchestrator.metric.dto.response.MetricResponse;
import eu.orchestrator.metric.dto.response.RainbowMetric;
import eu.orchestrator.metric.dto.response.RainbowMetricResponse;
import eu.orchestrator.metric.enums.MetricEnum;
import eu.orchestrator.metric.service.metric_clients.MetricClient;
import eu.orchestrator.metric.service.metric_clients.MetricClientFactory;
import eu.orchestrator.metric.service.converter.MetricConverter;
import eu.orchestrator.metric.service.converter.MetricConverterFactory;
import eu.orchestrator.repository.dao.AnalyticDAO;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.domain.Analytic;
import eu.orchestrator.repository.domain.AnalyticExpression;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.transfer.entities.kubernetes.NamespaceDto;
import eu.orchestrator.transfer.util.Util;
import eu.orchestrator.transfer.util.Util.Mode;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class MetricService {

    private static final Logger logger = Logger.getLogger(MetricService.class.getName());


    @Autowired
    private AnalyticDAO analyticDAO;

    @Autowired
    private ApplicationInstanceDAO applicationInstanceDAO;

    @Autowired
    private ComponentNodeDAO componentNodeDAO;

    @Autowired
    private ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    private MetricConverterFactory metricConverterFactory;

    @Autowired
    private MetricClientFactory metricClientFactory;

    @Value("${project.name}")
    private String projectName;

    // Base URI of the orchestrator REST API this service calls back into.
    @Value("${metric.rest.uri:http://localhost:8080/api/v1}")
    private String restCallUri;

    // Port of the Rainbow metric listing endpoint on each node.
    @Value("${metric.rainbow.list.port:50000}")
    private String rainbowListPort;


    public Analytic fetchById(Long id) {
        Optional<Analytic> analyticOptional = analyticDAO.findById(id);
        return analyticOptional.orElse(null);
    }

    public MetricTo fetchMetricToById(Long id, Long applicationInstanceId) {
        Optional<ApplicationInstance> existingApplicationInstanceOP = applicationInstanceDAO.findById(applicationInstanceId);
        ApplicationInstance applicationInstance = existingApplicationInstanceOP.orElse(null);
        if (applicationInstance == null) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }
        Analytic analytic = fetchById(id);
        if (analytic != null) {
            MetricTo metricTo = new MetricTo();
            BeanUtils.copyProperties(analytic, metricTo);
            return metricTo;
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public Page fetchMetricsByApplicationInstanceId(Long applicationInstanceId, String name, Pageable pageable) {
        Optional<ApplicationInstance> existingApplicationInstanceOP = applicationInstanceDAO.findById(applicationInstanceId);
        ApplicationInstance applicationInstance = existingApplicationInstanceOP.orElse(null);
        if (applicationInstance == null) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }

        Page<Analytic> page;
        if (name == null) {
            if (pageable.getPageSize() > 100) {
                page = analyticDAO.findAllByApplicationInstance(applicationInstance, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
            } else {
                page = analyticDAO.findAllByApplicationInstance(applicationInstance, pageable);
            }
        } else {
            if (pageable.getPageSize() > 100) {
                page = analyticDAO.findAllByApplicationInstanceAndName(applicationInstance, name,
                        PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
            } else {
                page = analyticDAO.findAllByApplicationInstanceAndName(applicationInstance, name, pageable);
            }
        }

        List<MetricTo> metricToList = new ArrayList<>();

        if (null != page && !page.getContent().isEmpty()) {
            page.getContent().forEach(metric -> {
                MetricTo metricTo = convertAnalyticToMetricDto(metric);
                metricToList.add(metricTo);
            });
            return new PageImpl<>(metricToList, pageable, page.getTotalElements());
        } else {
            return null;
        }
    }

    public void createMetricByApplicationInstanceId(Long applicationInstanceId, MetricTo metricTo, String auth) {
        Optional<ApplicationInstance> existingApplicationInstanceOP = applicationInstanceDAO.findById(applicationInstanceId);
        ApplicationInstance applicationInstance = existingApplicationInstanceOP.orElse(null);
        if (applicationInstance == null) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }

        if (NullCheckUtil.isEmpty(metricTo)) {
            throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
        }

        Analytic analytic = convertMetricToAnalytic(metricTo, applicationInstance);

        String namespace = fetchNamespaceByApplicationInstanceId(applicationInstanceId, auth);
        MetricForConversion metricForConversion = new MetricForConversion();
        metricForConversion.setAnalytic(analytic);
        metricForConversion.setNamespace(namespace);

        MetricConverter metricConverter = metricConverterFactory.getConverter(projectName);
        String metricJson = metricConverter.convert(metricForConversion);

        String endpointWithPort = applicationInstance.getProvider().getEndpoint();

        logger.info("PROJECT NAME: " + projectName);
        MetricClient metricClient = metricClientFactory.getClient(projectName);

        try {
            metricClient.apply(metricJson, analytic.getHexID(), endpointWithPort);
            analyticDAO.save(analytic);
        } catch (RestClientException restClientException) {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        } catch (DataAccessException dataAccessException) {
            metricClient.delete(analytic.getHexID(), endpointWithPort);
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }

    }

    public void deleteMetricByApplicationInstanceId(Long id) {
        Analytic analytic = fetchById(id);
        if (analytic == null) {
            throw new GenericBusinessException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
        }

        ApplicationInstance applicationInstance = analytic.getApplicationInstance();
        if (applicationInstance == null) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }
        String endpointWithPort = applicationInstance.getProvider().getEndpoint();

        MetricClient metricClient = metricClientFactory.getClient(projectName);

        try {
            metricClient.delete(analytic.getHexID(), endpointWithPort);
        } catch (Exception ignored) {
        }
        analyticDAO.delete(analytic);
    }

    public List<MetricResponse> fetchMetricsByComponentNodeHexID(Long applicationInstanceId, String componentNodeHexID) {
        Optional<ApplicationInstance> existingApplicationInstanceOP = applicationInstanceDAO.findById(applicationInstanceId);
        ApplicationInstance applicationInstance = existingApplicationInstanceOP.orElse(null);
        if (applicationInstance == null) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }

        Optional<ComponentNode> componentNodeOP = componentNodeDAO.findByHexID(componentNodeHexID);
        ComponentNode componentNode = componentNodeOP.orElse(null);
        if (componentNode == null) {
            throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_IS_NOT_EXIST.getCode(), GenericMessage.COMPONENT_NODE_IS_NOT_EXIST);
        }

        Optional<ComponentNodeInstance> componentNodeInstanceOP = componentNodeInstanceDAO
                .findByComponentNodeAndApplicationInstance(componentNode, applicationInstance);
        ComponentNodeInstance componentNodeInstance = componentNodeInstanceOP.orElse(null);
        if (componentNodeInstance == null) {
            throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_INSTANCE_NOT_EXIST.getCode(), GenericMessage.COMPONENT_NODE_INSTANCE_NOT_EXIST);
        }
        String endpoint = applicationInstance.getProvider().getEndpoint();
        //TODO if RAINBOW or else
        String podNameForRequest = componentNodeInstance.getName().toLowerCase(Locale.ROOT) + "-" + componentNodeInstance.getHexID();
        return fetchMetricsFromK8s(podNameForRequest, endpoint);
    }

    private List<AnalyticExpression> convertMetricExpressionListToAnalyticExpressionList(List<MetricExpressionTo> metricExpressionToList, Analytic analytic,
            ApplicationInstance applicationInstance) {
        List<AnalyticExpression> analyticExpressionList = new ArrayList<>();
        if (NullCheckUtil.isEmpty(metricExpressionToList)) {
            return analyticExpressionList;
        }
        for (MetricExpressionTo me : metricExpressionToList) {
            analyticExpressionList.add(convertMetricExpressionToAnalyticExpression(me, analytic, applicationInstance));
        }
        return analyticExpressionList;
    }

    private AnalyticExpression convertMetricExpressionToAnalyticExpression(MetricExpressionTo metricExpressionTo, Analytic analytic,
            ApplicationInstance applicationInstance) {
        Optional<ComponentNode> componentNodeOptional = componentNodeDAO.findByHexID(metricExpressionTo.getComponentNodeHexID());
        ComponentNode componentNode = componentNodeOptional.orElse(null);
        if (componentNode == null) {
            throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_IS_NOT_EXIST.getCode(), GenericMessage.COMPONENT_NODE_IS_NOT_EXIST);
        }
        Optional<ComponentNodeInstance> componentNodeInstanceOptional
                = componentNodeInstanceDAO.findByComponentNodeAndApplicationInstance(componentNode, applicationInstance);
        ComponentNodeInstance componentNodeInstance = componentNodeInstanceOptional.orElse(null);
        if (componentNodeInstance == null) {
            throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_INSTANCE_NOT_EXIST.getCode(), GenericMessage.COMPONENT_NODE_INSTANCE_NOT_EXIST);
        }
        AnalyticExpression analyticExpression = new AnalyticExpression();
        analyticExpression.setAnalytic(analytic);
        analyticExpression.setComponentNodeInstance(componentNodeInstance);
        analyticExpression.setMetric(metricExpressionTo.getMetric());
        analyticExpression.setDimension(metricExpressionTo.getDimension());
        analyticExpression.setArithmeticOperator(metricExpressionTo.getArithmeticOperator());
        analyticExpression.setMetricOrder(metricExpressionTo.getMetricOrder());
        return analyticExpression;
    }

    private Analytic convertMetricToAnalytic(MetricTo metricTo, ApplicationInstance applicationInstance) {
        Analytic analytic = new Analytic();
        analytic.setApplicationInstance(applicationInstance);
        analytic.setName(metricTo.getName());
        analytic.setHexID(Util.generateRandomString(10, Mode.ALPHANUMERIC_IGNORE_CASE));
        analytic.setType(metricTo.getType());
        analytic.setFunction(metricTo.getFunction());
        analytic.setPeriodicity(metricTo.getPeriodicity());
        analytic.setWindow(metricTo.getWindow());
        if (NullCheckUtil.isNotEmpty(metricTo.getMetricExpressions())) {
            analytic.setAnalyticExpressions(
                    convertMetricExpressionListToAnalyticExpressionList(metricTo.getMetricExpressions(), analytic, applicationInstance));
        }
        return analytic;
    }

    private String fetchNamespaceByApplicationInstanceId(Long applicationInstanceId, String auth) {
        final String uri = restCallUri + MetricEnum.KUBERNETES_NAMESPACE_URI.getValue() + applicationInstanceId;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(MetricEnum.COOKIE.getValue(), auth);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<RestResponseSpa<NamespaceDto>> result
                = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<RestResponseSpa<NamespaceDto>>() {
        });
        String namespace = null;
        if (result.getBody() != null && result.getBody().getReturnobject() != null) {
            NamespaceDto namespaceDto = result.getBody().getReturnobject();
            namespace = namespaceDto.getNamespace();
        }
        return namespace;
    }

    private MetricTo convertAnalyticToMetricDto(Analytic analytic) {
        MetricTo metricTo = new MetricTo();
        metricTo.setId(analytic.getAnalyticId());
        metricTo.setName(analytic.getName());
        metricTo.setFunction(analytic.getFunction());
        metricTo.setPeriodicity(analytic.getPeriodicity());
        metricTo.setType(analytic.getType());
        metricTo.setWindow(analytic.getWindow());

        List<MetricExpressionTo> metricExpressionTos = analytic
                .getAnalyticExpressions()
                .stream()
                .map(analyticExpression -> {

                    MetricExpressionTo metricExpressionTo = new MetricExpressionTo();

                    metricExpressionTo.setId(analyticExpression.getAnalyticExpressionId());
                    metricExpressionTo.setComponentNodeHexID(analyticExpression.getComponentNodeInstance().getHexID());
                    metricExpressionTo.setArithmeticOperator(analyticExpression.getArithmeticOperator());
                    metricExpressionTo.setMetric(analyticExpression.getMetric());
                    metricExpressionTo.setDimension(analyticExpression.getDimension());
                    metricExpressionTo.setMetricOrder(analyticExpression.getMetricOrder());

                    return metricExpressionTo;

                }).collect(Collectors.toList());

        metricTo.setMetricExpressions(metricExpressionTos);

        return metricTo;
    }

    //RAINBOW
    private List<MetricResponse> fetchMetricsFromK8s(String podNameForRequest, String endpointWithPort) {
        String endpoint = endpointWithPort.substring(endpointWithPort.lastIndexOf("/") + 1);
        endpoint = endpoint.substring(0, endpoint.lastIndexOf("]") + 1);
        String uri = "http://" + endpoint + ":" + rainbowListPort + "/list";
        logger.info("Metric URL request: " + uri);
        String requestData = "{\"podName\": [\"%" + podNameForRequest + "%\"], \"nodes\": []}";
        logger.info("Metric DATA request: " + requestData);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> entity = new HttpEntity<>(requestData, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        String responseJson = responseEntity.getBody();

        RainbowMetricResponse rainbowMetricResponse = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            rainbowMetricResponse = mapper.readValue(responseJson, RainbowMetricResponse.class);
        } catch (IOException ex) {
            throw new GenericBusinessException(ex.getMessage(), GenericMessage.GENERIC_ERROR);
        }

        if (NullCheckUtil.isNotEmpty(rainbowMetricResponse) && NullCheckUtil.isNotEmpty(rainbowMetricResponse.getMetric())) {
            List<MetricResponse> metricResponseList = new ArrayList<>();
            for (RainbowMetric rr : rainbowMetricResponse.getMetric()) {
                if (MetricEnum.POD.getValue().equalsIgnoreCase(rr.getEntityType())) {
                    MetricResponse metricResponse = new MetricResponse();
                    metricResponse.setName(rr.getMetricID());
                    metricResponse.setDescription(rr.getDesc());
                    metricResponse.setUnits(rr.getUnits());
                    metricResponseList.add(metricResponse);
                }
            }
            return metricResponseList;
        } else {
            throw new GenericBusinessException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
        }
    }

}
