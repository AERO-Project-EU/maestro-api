package eu.orchestrator.elasticity.service;

import com.querydsl.core.types.dsl.BooleanExpression;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.elasticity.dto.ComputationTo;
import eu.orchestrator.elasticity.dto.ElasticityTo;
import eu.orchestrator.elasticity.dto.ExpressionTo;
import eu.orchestrator.elasticity.dto.MetricTo;
import eu.orchestrator.elasticity.dto.NestedDataTo;
import eu.orchestrator.elasticity.dto.OrClauseTo;
import eu.orchestrator.elasticity.enums.ElasticEnum;
import eu.orchestrator.elasticity.dto.RestResponseSpa;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;

import eu.orchestrator.repository.dao.rainbow.SloDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.rainbow.Computation;
import eu.orchestrator.repository.domain.rainbow.Expression;
import eu.orchestrator.repository.domain.rainbow.ExpressionGroup;
import eu.orchestrator.repository.domain.rainbow.Metric;
import eu.orchestrator.repository.domain.rainbow.NestedMetric;
import eu.orchestrator.repository.domain.rainbow.Slo;
import eu.orchestrator.repository.enums.ComputationNestedMetricType;
import eu.orchestrator.transfer.entities.rainbowk8s.Affinity;
import eu.orchestrator.transfer.entities.rainbowk8s.ApiVersionKindPair;
import eu.orchestrator.transfer.entities.rainbowk8s.Disjunct;
import eu.orchestrator.transfer.entities.rainbowk8s.DisjunctList;
import eu.orchestrator.transfer.entities.rainbowk8s.KeyOperatorPair;
import eu.orchestrator.transfer.entities.rainbowk8s.KeyOperatorPair.Builder;
import eu.orchestrator.transfer.entities.rainbowk8s.Node;
import eu.orchestrator.transfer.entities.rainbowk8s.NodeAffinity;
import eu.orchestrator.transfer.entities.rainbowk8s.NodeSelectorTerms;
import eu.orchestrator.transfer.entities.rainbowk8s.RequiredDuringSchedulingIgnoredDuringExecution;
import eu.orchestrator.transfer.entities.rainbowk8s.ServiceGraph;
import eu.orchestrator.transfer.entities.rainbowk8s.SloTo;
import eu.orchestrator.transfer.entities.rainbowk8s.SloConfig;
import eu.orchestrator.transfer.entities.rainbowk8s.SloTargetState;
import eu.orchestrator.transfer.entities.rainbowk8s.StabilizationWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import org.springframework.web.client.RestTemplate;

import static eu.orchestrator.repository.domain.rainbow.QSlo.slo;

@Service
@Transactional(rollbackOn = Exception.class)
public class ElasticityQueriesService {

    private static final Logger logger = Logger.getLogger(ElasticityQueriesService.class.getName());

    private final ApplicationInstanceDAO applicationInstanceDAO;
    private final SloDAO sloDAO;

    private final EntityManager em;

    private static final String HORIZONTAL_ELASTICITY_STRATEGY = "HorizontalElasticityStrategy";
    private static final String MIGRATION_ELASTICITY_STRATEGY = "MigrationElasticityStrategy";
    private static final String ELASTICITY_RAINBOW = "elasticity.k8s.rainbow-h2020.eu/v1";
    private static final String ELASTICITY_POLARIS = "elasticity.polaris-slo-cloud.github.io/v1";

    // Base URI of the orchestrator REST API this service calls back into.
    @Value("${elasticity.rest.uri:http://localhost:8080/api/v1}")
    private String restCallUri;

    @Autowired
    public ElasticityQueriesService(
            ApplicationInstanceDAO applicationInstanceDAO,
            SloDAO sloDAO,
            EntityManager em
    ) {
        this.applicationInstanceDAO = applicationInstanceDAO;
        this.sloDAO = sloDAO;
        this.em = em;
    }

    public Page getSlosByApplicationInstanceId(ElasticityTo fElasticityTo, Pageable pageable, Long applicationInstanceId, String auth) {
        ApplicationInstance applicationInstance = getApplicationInstance(applicationInstanceId);

        BooleanExpression predicate = slo.eq(slo);

        if (null != fElasticityTo && null != fElasticityTo.getName() && !fElasticityTo.getName().isEmpty()) {
            predicate = predicate.and(slo.name.containsIgnoreCase(fElasticityTo.getName()));
        }

        predicate = predicate.and(slo.applicationInstance.eq(applicationInstance));

        Page<Slo> page;
        List<ElasticityTo> elasticityTos = new ArrayList<>();

        if (pageable.getPageSize() > 25) {
            page = sloDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 25, pageable.getSort()));
        } else {
            page = sloDAO.findAll(predicate, pageable);
        }

        page.getContent().forEach(slo -> {
            ElasticityTo elasticityTo = new ElasticityTo();
            elasticityTo.setId(slo.getId());
            elasticityTo.setName(slo.getName());
            elasticityTo.setElasticityStrategy(slo.getElasticityStrategy());
            elasticityTo.setDateCreated(slo.getCreatedAt());
            elasticityTos.add(elasticityTo);
        });

        return new PageImpl<>(elasticityTos, pageable, page.getTotalElements());
    }

    public ElasticityTo getSlo(Long sloId, Long applicationInstanceId, String auth) {
        ApplicationInstance applicationInstance = getApplicationInstance(applicationInstanceId);

        Optional<Slo> sloOptional = sloDAO.findByIdAndApplicationInstance(sloId, applicationInstance);

        if (!sloOptional.isPresent()) {
            throw new GenericBusinessException(GenericMessage.SLO_NOT_FOUND.getCode(), GenericMessage.SLO_NOT_FOUND);
        }

        Slo slo = sloOptional.get();

        return sloToElasticityTo(slo);

    }

    public void createSloByApplicationInstanceId(Long applicationInstanceId, ElasticityTo elasticityTo, String auth) {
        Optional<ApplicationInstance> existingApplicationInstanceOP = applicationInstanceDAO.findById(applicationInstanceId);
        ApplicationInstance applicationInstance = existingApplicationInstanceOP.orElse(null);
        if (applicationInstance == null) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }

        if (NullCheckUtil.isEmpty(elasticityTo)) {
            throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
        }

        ComponentNodeInstance componentNodeInstance = getComponentNodeInstanceByApplicationAndComponentNodeHexId(applicationInstance,
                elasticityTo.getMetricComponentNodeHexID());

        logger.info("Elasticity - ElasticityQueriesService - application instance id: " + applicationInstanceId);
        ServiceGraph serviceGraph = fetchRainbowServiceGraphByApplicationInstanceId(applicationInstanceId, auth);
        if (serviceGraph == null) {
            throw new GenericBusinessException(GenericMessage.SERVICE_GRAPH_NOT_FOUND.getCode(), GenericMessage.SERVICE_GRAPH_NOT_FOUND);
        }

        Node node = searchNodeInServiceGraphByComponentNodeHexId(serviceGraph, componentNodeInstance.getHexID());

        Slo slo = buildAndStoreSlo(elasticityTo, applicationInstance);
        SloTo sloTo = sloDomainToSloTo(slo);

        Node updatedNode = addSloInNode(node, sloTo);

        List<Node> nodes = replaceNodeInNodeList(serviceGraph, updatedNode);

        serviceGraph.getSpec().setNodes(nodes);

        applyRainbowServiceGraph(applicationInstanceId, serviceGraph, auth);
    }

    public void deleteSloByApplicationInstanceId(Long applicationInstanceId, Long sloId, String auth) {
        Optional<ApplicationInstance> existingApplicationInstanceOP = applicationInstanceDAO.findById(applicationInstanceId);
        ApplicationInstance applicationInstance = existingApplicationInstanceOP.orElse(null);
        if (applicationInstance == null) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }

        Optional<Slo> sloOptional = sloDAO.findByIdAndApplicationInstance(sloId, applicationInstance);
        Slo slo = sloOptional.orElse(null);

        if (slo == null) {
            throw new GenericBusinessException(GenericMessage.SLO_NOT_FOUND.getCode(), GenericMessage.SLO_NOT_FOUND);
        }

        ServiceGraph serviceGraph = fetchRainbowServiceGraphByApplicationInstanceId(applicationInstanceId, auth);

        if (serviceGraph == null) {
            throw new GenericBusinessException(GenericMessage.SERVICE_GRAPH_NOT_FOUND.getCode(), GenericMessage.SERVICE_GRAPH_NOT_FOUND);
        }

        ComponentNodeInstance componentNodeInstance = getComponentNodeInstanceByApplicationAndComponentNodeHexId(applicationInstance,
                slo.getComponentNodeHexId());
        Node node = searchNodeInServiceGraphByComponentNodeHexId(serviceGraph, componentNodeInstance.getHexID());

        Node updatedNode = removeSloFromNodeBySloName(node, slo.getName());

        List<Node> nodes = replaceNodeInNodeList(serviceGraph, updatedNode);

        serviceGraph.getSpec().setNodes(nodes);

        applyRainbowServiceGraph(applicationInstanceId, serviceGraph, auth);

        sloDAO.delete(slo);
    }

    private ServiceGraph fetchRainbowServiceGraphByApplicationInstanceId(Long applicationInstanceId, String auth) {
        final String uri = restCallUri + ElasticEnum.KUBERNETES_RAINBOW_SERVICE_GRAPH.getValue() + applicationInstanceId;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(ElasticEnum.COOKIE.getValue(), auth);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<RestResponseSpa<ServiceGraph>> result
                = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<RestResponseSpa<ServiceGraph>>() {
        });
        ServiceGraph serviceGraph = null;
        if (result.getBody() != null && result.getBody().getReturnobject() != null) {
            serviceGraph = result.getBody().getReturnobject();
        }
        return serviceGraph;
    }

    private void applyRainbowServiceGraph(Long applicationInstanceId, ServiceGraph serviceGraph, String auth) {
        final String uri = restCallUri + ElasticEnum.KUBERNETES_RAINBOW_SERVICE_GRAPH.getValue() + applicationInstanceId + "/apply";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(ElasticEnum.COOKIE.getValue(), auth);

        HttpEntity<ServiceGraph> entity = new HttpEntity<>(serviceGraph, headers);
        restTemplate.exchange(uri, HttpMethod.POST, entity, new ParameterizedTypeReference<RestResponseSpa<String>>() {
        });
    }

    private SloTo sloDomainToSloTo(Slo slo) {
        SloTo sloTo = new SloTo();

        // SLO Name
        sloTo.setName(slo.getFullName());

        // SLO Type
        String sloTypeApiVersion = "slo.k8s.rainbow-h2020.eu/v1";
        String sloTypeKind = "CustomStreamSightSloMapping";
        ApiVersionKindPair sloType = new ApiVersionKindPair(sloTypeApiVersion, sloTypeKind);
        sloTo.setSloType(sloType);

        // SLO Elasticity Strategy
        ApiVersionKindPair elasticityStrategy = new ApiVersionKindPair();

        if (HORIZONTAL_ELASTICITY_STRATEGY.equalsIgnoreCase(slo.getElasticityStrategy())) {
            elasticityStrategy.setApiVersion(ELASTICITY_POLARIS);
        } else {
            elasticityStrategy.setApiVersion(ELASTICITY_RAINBOW);
        }

        elasticityStrategy.setKind(slo.getElasticityStrategy());
        sloTo.setElasticityStrategy(elasticityStrategy);

        // SLO Config
        SloConfig sloConfig = new SloConfig();

        // SLO Config > Streams
        Map<String, String> streams = new HashMap<>();
        for (Metric metric : slo.getMetrics()) {
            streams.put(metric.getName(), buildStream(metric));
        }
        sloConfig.setStreams(streams);

        // SLO Config > Insights
        Map<String, String> insights = new HashMap<>();
        for (Computation computation : slo.getComputations()) {
            insights.put(computation.getName(), buildInsight(slo.getMetrics(), computation));
        }
        sloConfig.setInsights(insights);

        // SLO Config > Target State
        SloTargetState sloTargetState = new SloTargetState();
        List<DisjunctList> disjunctListList = new ArrayList<>();
        for (ExpressionGroup expressionGroup : slo.getExpressions()) {
            DisjunctList list = new DisjunctList();
            List<Disjunct> disjuncts = new ArrayList<>();
            for (Expression expression : expressionGroup.getOrClauses()) {
                Disjunct disjunct = new Disjunct();
                disjunct.setInsight(expression.getComputation().getName());
                disjunct.setTargetValue(expression.getTargetValue());
                disjunct.setTolerance(expression.getTolerance());
                disjunct.setHigherIsBetter(expression.isHigherBetter());
                disjuncts.add(disjunct);
                list.setDisjuncts(disjuncts);
            }
            disjunctListList.add(list);
        }
        sloTargetState.setConjuncts(disjunctListList);

        sloConfig.setTargetState(sloTargetState);
        sloConfig.setElasticityStrategyTolerance(10);

        sloTo.setSloConfig(sloConfig);

        // SLO Static Elasticity Strategy Config (Migration Elasticity Strategy only)
        if (MIGRATION_ELASTICITY_STRATEGY.equalsIgnoreCase(slo.getElasticityStrategy())) {
            Builder keyOperatorPairBuilder = new KeyOperatorPair.Builder();

            KeyOperatorPair keyOperatorPair = keyOperatorPairBuilder
                    .key("kubernetes.io/arch")
                    .operator("In")
                    .values(Stream.of("arm64").collect(Collectors.toList()))
                    .build();
            Set<KeyOperatorPair> keyOperatorPairSet = new HashSet<>();
            keyOperatorPairSet.add(keyOperatorPair);

            NodeSelectorTerms nodeSelectorTerms = new NodeSelectorTerms();
            nodeSelectorTerms.setMatchExpressions(keyOperatorPairSet);

            RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution = new RequiredDuringSchedulingIgnoredDuringExecution();
            requiredDuringSchedulingIgnoredDuringExecution.setNodeSelectorTerms(Stream.of(nodeSelectorTerms).collect(Collectors.toList()));

            NodeAffinity nodeAffinity = new NodeAffinity();
            nodeAffinity.setRequiredDuringSchedulingIgnoredDuringExecution(requiredDuringSchedulingIgnoredDuringExecution);

            Affinity affinity = new Affinity();
            affinity.setNodeAffinity(nodeAffinity);

            Map<String, Affinity> staticElasticityStrategyConfig = new HashMap<>();
            staticElasticityStrategyConfig.put("baseAffinity", affinity);
            staticElasticityStrategyConfig.put("alternativeAffinity", affinity);
            sloTo.setStaticElasticityStrategyConfig(staticElasticityStrategyConfig);

        }

        // SLO Stabilization Window
        StabilizationWindow stabilizationWindow = new StabilizationWindow();
        stabilizationWindow.setScaleUpSeconds(10);
        stabilizationWindow.setScaleDownSeconds(10);
        sloTo.setStabilizationWindow(stabilizationWindow);

        return sloTo;
    }

    private Slo buildAndStoreSlo(ElasticityTo elasticityTo, ApplicationInstance applicationInstance) {
        if (elasticityTo.getId() != 0) {
            sloDAO.deleteById(elasticityTo.getId());
        }

        Slo slo = new Slo();
        slo.setName(elasticityTo.getName());
        slo.setElasticityStrategy(elasticityTo.getElasticityStrategy());
        slo.setApplicationInstance(applicationInstance);
        slo.setComponentNodeHexId(elasticityTo.getMetricComponentNodeHexID());

        elasticityTo.getMetrics().forEach(
                metricTo -> slo.addMetric(metricToToMetric(metricTo))
        );

        elasticityTo.getComputations().forEach(
                computationTo -> slo.addComputation(computationToToComputation(computationTo, slo.getMetrics()))
        );

        elasticityTo.getExpressions().forEach(
                expressionTo -> slo.addExpressionGroup(expressionToToExpressionGroup(expressionTo, slo.getComputations()))
        );

        sloDAO.save(slo);
        return slo;
    }

    private String buildStream(Metric metric) {
        return String.format(
                "stream from storageLayer(periodicity=1000, metricID=\"%s\", entityType=\"POD\", podNamespace=\"${namespace}\", podName=\"${podName}\" );",
                metric.getType());
    }

    private String buildInsight(Set<Metric> metrics, Computation computation) {
        StringBuilder stringBuilder = new StringBuilder();
        for (NestedMetric nestedMetric : computation.getNestedMetrics()) {
            Metric metric = findMetricToByName(nestedMetric.getMetric().getName(), metrics);

            if ("metric".equalsIgnoreCase(nestedMetric.getType().name())) {
                String q =
                        String.format(
                                "%s(\"%s\" FROM (%s), %ss)",
                                "NONE".equalsIgnoreCase(metric.getFunction()) ? "" : metric.getFunction().toLowerCase(),
                                metric.getType(),
                                metric.getName(),
                                metric.getWindowTime()
                        );
                stringBuilder.append(q);
            } else if ("constant".equalsIgnoreCase(nestedMetric.getType().name())) {
                stringBuilder.append(nestedMetric.getValue());
            } else {
                throw new RuntimeException("Unknown nested data type");
            }

            if (null != nestedMetric.getOperand() && !nestedMetric.getOperand().isEmpty()) {
                stringBuilder.append(" ").append(nestedMetric.getOperand()).append(" ");
            }

        }
        return String.format("compute (%s) EVERY %s SECONDS;", stringBuilder, computation.getInterval());
    }

    private Metric findMetricToByName(String name, Set<Metric> metrics) {
        Optional<Metric> metricToOptional = metrics.stream().filter(metric -> name.equalsIgnoreCase(metric.getName())).findFirst();
        if (!metricToOptional.isPresent()) {
            throw new RuntimeException("Metric doesn't exist");
        }

        return metricToOptional.get();
    }

    private ElasticityTo sloToElasticityTo(Slo slo) {
        ElasticityTo elasticityTo = new ElasticityTo();
        elasticityTo.setId(slo.getId());
        elasticityTo.setMetricComponentNodeHexID(slo.getComponentNodeHexId());
        elasticityTo.setName(slo.getName());
        elasticityTo.setElasticityStrategy(slo.getElasticityStrategy());
        elasticityTo.setDateCreated(slo.getCreatedAt());
        elasticityTo.setMetrics(
                slo.getMetrics()
                        .stream()
                        .map(this::metricToMetricTo)
                        .collect(Collectors.toList())
        );
        elasticityTo.setComputations(
                slo.getComputations()
                        .stream()
                        .map(this::computationToComputationTo)
                        .collect(Collectors.toList())
        );
        elasticityTo.setExpressions(
                slo.getExpressions()
                        .stream()
                        .map(this::expressionsToExpressionTo)
                        .collect(Collectors.toList()));
        return elasticityTo;
    }

    private MetricTo metricToMetricTo(Metric metric) {
        MetricTo metricTo = new MetricTo();
        metricTo.setName(metric.getName());
        metricTo.setMetric(metric.getType());
        metricTo.setFunction(metric.getFunction());
        metricTo.setWindowTime(metric.getWindowTime());
        return metricTo;
    }

    private Metric metricToToMetric(MetricTo metricTo) {
        Metric metric = new Metric();
        metric.setName(metricTo.getName());
        metric.setType(metricTo.getMetric());
        metric.setFunction(metricTo.getFunction());
        metric.setWindowTime(metricTo.getWindowTime());
        return metric;
    }

    private ComputationTo computationToComputationTo(Computation computation) {
        ComputationTo computationTo = new ComputationTo();
        computationTo.setName(computation.getName());
        computationTo.setEvery(computation.getInterval());
        computationTo.setNestedData(
                computation.getNestedMetrics()
                        .stream()
                        .map(this::nestedMetricsToNestedDataTo)
                        .collect(Collectors.toList())
        );
        return computationTo;
    }

    private Computation computationToToComputation(ComputationTo computationTo, Set<Metric> metrics) {
        Computation computation = new Computation();
        computation.setName(computationTo.getName());
        computation.setInterval(computationTo.getEvery());
        computationTo.getNestedData()
                .forEach(nestedDataTo -> computation.addNestedMetric(nestedDataToToNestedMetric(nestedDataTo, metrics)));
        return computation;
    }

    private NestedDataTo nestedMetricsToNestedDataTo(NestedMetric nestedMetric) {
        NestedDataTo nestedDataTo = new NestedDataTo();
        if (nestedMetric.getMetric() != null) {
            nestedDataTo.setMetricName(nestedMetric.getMetric().getName());
        }
        nestedDataTo.setComputationTime(0);
        nestedDataTo.setType(nestedMetric.getType().name());

        nestedDataTo.setValue(null);
        if (null != nestedMetric.getValue()) {
            nestedDataTo.setValue(String.valueOf(nestedMetric.getValue()));
        }

        nestedDataTo.setOperand(nestedMetric.getOperand());
        return nestedDataTo;
    }

    private NestedMetric nestedDataToToNestedMetric(NestedDataTo nestedDataTo, Set<Metric> metrics) {
        NestedMetric nestedMetric = new NestedMetric();

        Metric metric = metrics.stream()
                .filter(m -> nestedDataTo.getMetricName().equalsIgnoreCase(m.getName()))
                .findFirst()
                .orElse(null);

        nestedMetric.setMetric(metric);
        nestedMetric.setType(ComputationNestedMetricType.valueOf(nestedDataTo.getType().toUpperCase()));
        nestedMetric.setValue(Double.parseDouble(!nestedDataTo.getValue().isEmpty() ? nestedDataTo.getValue() : "0"));
        nestedMetric.setOperand(nestedMetric.getOperand());
        return nestedMetric;
    }

    private ExpressionTo expressionsToExpressionTo(ExpressionGroup expressionGroup) {
        ExpressionTo expressionTo = new ExpressionTo();
        expressionTo.setOrClauses(
                expressionGroup.getOrClauses()
                        .stream()
                        .map(this::expressionToOrClauseTo)
                        .collect(Collectors.toList())
        );
        return expressionTo;
    }

    private ExpressionGroup expressionToToExpressionGroup(ExpressionTo expressionTo, Set<Computation> computations) {
        ExpressionGroup expressionGroup = new ExpressionGroup();
        expressionTo.getOrClauses().forEach(
                orClauseTo -> expressionGroup.addOrClause(orClauseToToExpression(orClauseTo, computations))
        );

        return expressionGroup;
    }

    private OrClauseTo expressionToOrClauseTo(Expression expression) {
        OrClauseTo orClauseTo = new OrClauseTo();
        orClauseTo.setComputationName(expression.getComputation().getName());
        orClauseTo.setTargetValue(expression.getTargetValue());
        orClauseTo.setTolerance(expression.getTolerance());
        orClauseTo.setHigherIsBetter(expression.isHigherBetter());
        return orClauseTo;
    }

    private Expression orClauseToToExpression(OrClauseTo orClauseTo, Set<Computation> computations) {
        Expression expression = new Expression();
        Computation computation = computations.stream()
                .filter(c -> orClauseTo.getComputationName().equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElse(null);

        expression.setComputation(computation);
        expression.setTargetValue(orClauseTo.getTargetValue());
        expression.setTolerance(orClauseTo.getTolerance());
        expression.setHigherBetter(orClauseTo.isHigherIsBetter());
        return expression;
    }

    private ApplicationInstance getApplicationInstance(Long id) {
        Optional<ApplicationInstance> applicationInstanceOptional = applicationInstanceDAO.findById(id);

        if (!applicationInstanceOptional.isPresent()) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }

        return applicationInstanceOptional.get();
    }

    private ComponentNodeInstance getComponentNodeInstanceByApplicationAndComponentNodeHexId(ApplicationInstance applicationInstance,
            String componentNodeHexId) {
        Optional<ComponentNode> componentNodeOptional = applicationInstance
                .getApplication()
                .getComponentNodes()
                .stream()
                .filter(
                        cn -> cn.getHexID().equals(componentNodeHexId)
                )
                .findFirst();

        if (!componentNodeOptional.isPresent()) {
            throw new GenericBusinessException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
        }

        ComponentNode componentNode = componentNodeOptional.get();

        Optional<ComponentNodeInstance> componentNodeInstanceOptional = applicationInstance
                .getComponentNodeInstances()
                .stream()
                .filter(cni -> cni.getApplicationInstance().equals(applicationInstance) && cni.getComponentNode().equals(componentNode))
                .findFirst();

        if (!componentNodeInstanceOptional.isPresent()) {
            throw new GenericBusinessException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
        }

        return componentNodeInstanceOptional.get();
    }

    private Node searchNodeInServiceGraphByComponentNodeHexId(ServiceGraph serviceGraph, String componentNodeHexId) {
        Optional<Node> nodeOptional = serviceGraph
                .getSpec()
                .getNodes()
                .stream()
                .filter(n -> n.getName().contains(componentNodeHexId))
                .findFirst();

        if (!nodeOptional.isPresent()) {
            throw new GenericBusinessException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
        }

        return nodeOptional.get();
    }

    private List<Node> replaceNodeInNodeList(ServiceGraph serviceGraph, Node node) {
        List<Node> nodes = serviceGraph
                .getSpec()
                .getNodes()
                .stream()
                .filter(n -> !n.getName().equals(node.getName()))
                .collect(Collectors.toList());
        nodes.add(node);
        return nodes;
    }

    private Node addSloInNode(Node node, SloTo sloTo) {
        List<SloTo> sloTos = node.getSlos();
        sloTos = sloTos
                .stream()
                .filter(serviceGraphSlo -> !serviceGraphSlo.getName().equalsIgnoreCase(sloTo.getName()))
                .collect(Collectors.toList());
        sloTos.add(sloTo);

        node.setSlos(sloTos);
        return node;
    }

    private Node removeSloFromNodeBySloName(Node node, String sloName) {
        List<SloTo> sloTos = node.getSlos();
        sloTos = sloTos
                .stream()
                .filter(serviceGraphSlo -> !serviceGraphSlo.getName().equalsIgnoreCase(sloName))
                .collect(Collectors.toList());

        node.setSlos(sloTos);
        return node;
    }

}
