package eu.orchestrator.backend.service.elasticity;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.backend.kafka.Sender;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.service.component.ComponentNodeService;
import eu.orchestrator.backend.transfer.RuntimePolicyActionTO;
import eu.orchestrator.backend.transfer.RuntimePolicyExpressionTO;
import eu.orchestrator.backend.transfer.RuntimePolicyTO;
import eu.orchestrator.backend.util.PrometheusUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.elasticity.adapter.traefikLB.TraefikLoadBalancerBackendAdapter;
import eu.orchestrator.elasticity.adapter.traefikLambdaProxy.TraefikLambdaProxyBackendAdapter;
import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkBackend;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ElasticityHistoryDAO;
import eu.orchestrator.repository.dao.RuntimePolicyActionDAO;
import eu.orchestrator.repository.dao.RuntimePolicyDAO;
import eu.orchestrator.repository.dao.RuntimePolicyExpressionDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ElasticityHistory;
import eu.orchestrator.repository.domain.RuntimePolicy;
import eu.orchestrator.repository.domain.RuntimePolicyAction;
import eu.orchestrator.repository.domain.RuntimePolicyExpression;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.entities.agent.ActionsCommands;
import eu.orchestrator.transfer.entities.policyEngine.ActionType;
import eu.orchestrator.transfer.entities.policyEngine.DroolsAction;
import eu.orchestrator.transfer.entities.policyEngine.PolicyModel;
import eu.orchestrator.transfer.entities.policyEngine.PolicyType;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 29/8/2019
 */
@Service
@Transactional(rollbackOn = Exception.class)
public class ElasticityService {

    private static final Logger logger = Logger.getLogger(ElasticityService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${ui.server.url}")
    String uiURL;
    @Autowired
    Sender kafkaSender;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private ComponentNodeService componentNodeService;
    @Autowired
    private RuntimePolicyDAO runtimePolicyDAO;
    @Autowired
    private RuntimePolicyActionDAO runtimePolicyActionDAO;
    @Autowired
    private RuntimePolicyExpressionDAO runtimePolicyExpressionDAO;
    @Autowired
    private ApplicationInstanceDAO applicationInstanceDAO;
    @Autowired
    private ElasticityHistoryDAO elasticityHistoryDAO;
    @Autowired
    private TraefikLoadBalancerBackendAdapter traefikLoadBalancerBackendAdapter;
    @Autowired
    private TraefikLambdaProxyBackendAdapter traefikLambdaProxyBackendAdapter;

    public ElasticityFrameworkBackend fetchElasticityBackendAdapter(ComponentNodeInstance componentNodeInstanceWorker) {
        if (componentNodeInstanceWorker.getComponentNode().getComponent().getElasticityController().
                equals(traefikLambdaProxyBackendAdapter.getElasticityType().getName())) {
            return traefikLambdaProxyBackendAdapter;
        } else if (componentNodeInstanceWorker.getComponentNode().getComponent().getElasticityController().
                equals(traefikLoadBalancerBackendAdapter.getElasticityType().getName())) {
            return traefikLoadBalancerBackendAdapter;
        } else {
            logger.log(Level.INFO, "Nothing it is");
            return null;
        }
    }


    public Boolean requiresElasticity(ComponentNodeInstance componentNodeInstanceWorker) {
        return componentNodeInstanceWorker.getComponentNode().getComponent().getElasticityController().
                equals(traefikLambdaProxyBackendAdapter.getElasticityType().getName())
                || componentNodeInstanceWorker.getComponentNode().getComponent().getElasticityController().
                equals(traefikLoadBalancerBackendAdapter.getElasticityType().getName());
    }

    public Page fetchRuntimePoliciesByTypeAndId(Long id, Pageable pageable, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) && existingApplicationInstance.getStatus()
                .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && (authenticatedUser.isAdmin() || existingApplicationInstance.getOrganization().getId()
                .equals(authenticatedUser.getOrganization().getId()))) {

            List<RuntimePolicyTO> runtimePolicyTOs = new ArrayList<>();
            Page<RuntimePolicy> page;

            if (pageable.getPageSize() > 100) {
                page = runtimePolicyDAO.findAllByApplicationInstance(existingApplicationInstance,
                        PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
            } else {
                page = runtimePolicyDAO.findAllByApplicationInstance(existingApplicationInstance, pageable);
            }

            if (null != page && NullCheckUtil.isNotEmpty(page.getContent())) {
                final User loginUser = authenticatedUser;
                page.getContent().forEach(runtimePolicy -> {
                    RuntimePolicyTO runtimePolicyTO = new RuntimePolicyTO();
                    BeanUtils.copyProperties(runtimePolicy, runtimePolicyTO);

                    runtimePolicyTO.setAllowDelete(runtimePolicy.hasDeleteAllowance(loginUser));
                    runtimePolicyTO.setAllowEdit(false);
                    runtimePolicyTO.setUsername(runtimePolicy.getUser().getUsername());

                    // Copy extra fields
                    if (null != runtimePolicy.getActions() && !runtimePolicy.getActions().isEmpty()) {
                        List<RuntimePolicyActionTO> actions = new ArrayList<>();
                        runtimePolicy.getActions().forEach(runtimePolicyAction -> {
                            RuntimePolicyActionTO runtimePolicyActionTO = new RuntimePolicyActionTO();
                            BeanUtils.copyProperties(runtimePolicyAction, runtimePolicyActionTO);
                            actions.add(runtimePolicyActionTO);
                        });
                        runtimePolicyTO.setActions(actions);
                    } else {
                        runtimePolicyTO.setActions(null);
                    }

                    if (null != runtimePolicy.getExpressions() && !runtimePolicy.getExpressions().isEmpty()) {
                        List<RuntimePolicyExpressionTO> expressions = new ArrayList<>();
                        runtimePolicy.getExpressions().forEach(runtimePolicyExpression -> {
                            RuntimePolicyExpressionTO runtimePolicyExpressionTO = new RuntimePolicyExpressionTO();
                            BeanUtils.copyProperties(runtimePolicyExpression, runtimePolicyExpressionTO);
                            expressions.add(runtimePolicyExpressionTO);
                        });
                        runtimePolicyTO.setExpressions(expressions);
                    } else {
                        runtimePolicyTO.setExpressions(null);
                    }
                    runtimePolicyTOs.add(runtimePolicyTO);
                });

            }
            return new PageImpl<>(runtimePolicyTOs, pageable, page.getTotalElements());
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void createElasticityRuntimePolicyById(Long id, RuntimePolicy runtimePolicy, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) && existingApplicationInstance.getStatus()
                .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && (existingApplicationInstance.hasEditAllowance(authenticatedUser))) {

            boolean requiredFields =
                    null != runtimePolicy && null != runtimePolicy.getName() && !runtimePolicy.getName().isEmpty()
                            && null != runtimePolicy.getPolicyPeriod() && !runtimePolicy.getPolicyPeriod().isEmpty()
                            && null != runtimePolicy.getInertiaPeriod() && !runtimePolicy.getInertiaPeriod().isEmpty()
                            && null != runtimePolicy.getActions() && !runtimePolicy.getActions().isEmpty()
                            && null != runtimePolicy.getExpressions() && !runtimePolicy.getExpressions().isEmpty();

            if (requiredFields) {
                Optional<RuntimePolicy> runtimePolicyOP = runtimePolicyDAO.findByApplicationInstanceAndName(existingApplicationInstance,
                        runtimePolicy.getName());

                if (runtimePolicyOP.isPresent()) {
                    throw new GenericBusinessException(GenericMessage.RUNTIME_POLICY_ALREADY_EXISTS.getCode(), GenericMessage.RUNTIME_POLICY_ALREADY_EXISTS);
                }

                if (!StringUtils.isAlphanumeric(runtimePolicy.getName())) {
                    throw new GenericBusinessException(GenericMessage.RUNTIME_POLICY_NAME_ALPHANUMERIC.getCode(),
                            GenericMessage.RUNTIME_POLICY_NAME_ALPHANUMERIC);
                }

                try {
                    runtimePolicy.setHexID(Util.createRandomHEXString());
                    runtimePolicy.setDateCreated(new Date());
                    runtimePolicy.setLastModified(new Date());
                    runtimePolicy.setType(RuntimePolicy.Type.ELASTICITY.name());
                    runtimePolicy.setStatus(RuntimePolicy.RuntimePolicyStatus.PENDING.name());
                    runtimePolicy.setApplicationInstance(existingApplicationInstance);

                    List<RuntimePolicyAction> existingRuntimePolicyActions = runtimePolicy.getActions();
                    List<RuntimePolicyExpression> existingRuntimePolicyExpressions = runtimePolicy
                            .getExpressions();

                    for (RuntimePolicyExpression currentExpression : existingRuntimePolicyExpressions) {

                        if (existingRuntimePolicyExpressions.size() > 1 && (currentExpression.getFunction().isEmpty() || currentExpression.getFunction()
                                .equals(RuntimePolicyExpression.FunctionType.NONE.name()))) {
                            throw new GenericBusinessException(GenericMessage.RUNTIME_POLICY_WITH_MULTI_EXPRESSION_MUST_HAS_FUNCTION.getCode(),
                                    GenericMessage.RUNTIME_POLICY_WITH_MULTI_EXPRESSION_MUST_HAS_FUNCTION);
                        }

                        if (currentExpression.getDimension().isEmpty()) {
                            throw new GenericBusinessException(GenericMessage.RUNTIME_POLICY_MUST_HAS_DIMENSION.getCode(),
                                    GenericMessage.RUNTIME_POLICY_MUST_HAS_DIMENSION);
                        }

                        ComponentNode componentNode = componentNodeService.fetchComponentNodeByHexId(currentExpression.getComponentNodeHexID());

                        if (NullCheckUtil.isNotEmpty(componentNode)) {
                            Component component = componentNode.getComponent();
                            if (component.getElasticityController().compareTo("NONE") != 0 && (currentExpression.getFunction().isEmpty() || currentExpression
                                    .getFunction().equals(RuntimePolicyExpression.FunctionType.NONE.name()))) {
                                throw new GenericBusinessException(GenericMessage.RUNTIME_POLICY_ON_SCALABLE_COMPONENT_MUST_HAS_FUNCTION.getCode(),
                                        GenericMessage.RUNTIME_POLICY_ON_SCALABLE_COMPONENT_MUST_HAS_FUNCTION);
                            }
                        } else {
                            throw new GenericBusinessException(GenericMessage.RUNTIME_POLICY_COMPONENT_NODE_DOESNT_EXISTS.getCode(),
                                    GenericMessage.RUNTIME_POLICY_COMPONENT_NODE_DOESNT_EXISTS);
                        }
                    }

                    runtimePolicy.setActions(null);
                    runtimePolicy.setExpressions(null);
                    runtimePolicy.setUser(authenticatedUser);

                    runtimePolicyDAO.save(runtimePolicy);

                    List<RuntimePolicyAction> runtimePolicyActions = new ArrayList<>();

                    for (RuntimePolicyAction actionObj : existingRuntimePolicyActions) {

                        ComponentNode cn = componentNodeService.fetchComponentNodeByHexId(actionObj.getComponentHexID());

                        actionObj.setComponentHexID(cn.getHexID());
                        actionObj.setRuntimePolicy(runtimePolicy);
                        actionObj.setDateCreated(new Date());
                        actionObj.setLastModified(new Date());

                        runtimePolicyActionDAO.save(actionObj);
                        runtimePolicyActions.add(actionObj);

                    }

                    runtimePolicy.setActions(runtimePolicyActions);

                    // Parse expressions to Prometheus pattern
                    List<RuntimePolicyExpression> newRuntimePolicyExpressions = new ArrayList<>();

                    existingRuntimePolicyExpressions.forEach(runtimePolicyExpression -> {

                        ComponentNode cn = componentNodeService.fetchComponentNodeByHexId(runtimePolicyExpression.getComponentNodeHexID());

                        runtimePolicyExpression.setComponentNodeHexID(cn.getHexID());
                        runtimePolicyExpression.setRuntimePolicy(runtimePolicy);
                        runtimePolicyExpression.setDateCreated(new Date());
                        runtimePolicyExpression.setLastModified(new Date());
                        runtimePolicyExpressionDAO.save(runtimePolicyExpression);
                        newRuntimePolicyExpressions.add(runtimePolicyExpression);

                    });

                    PolicyModel policyModel = new PolicyModel();
                    policyModel.setPolicyHexID(runtimePolicy.getHexID());
                    policyModel.setCreation(true);
                    policyModel.setCallbackURL(uiURL + "/api/v1/callback/policy/" + runtimePolicy.getId());
                    policyModel.setGraphHexID(existingApplicationInstance.getApplication().getHexID());
                    policyModel.setGraphInstanceHexID(existingApplicationInstance.getHexID());
                    policyModel.setPolicyType(PolicyType.elasticity);
                    policyModel.setPolicyName(runtimePolicy.getName());

                    // TODO
                    policyModel.setPrometheusPolicyExpression(PrometheusUtil
                            .serializePrometheusExpression(newRuntimePolicyExpressions,
                                    existingApplicationInstance));
                    // TODO

                    policyModel.setPrometheusPolicyPeriod(runtimePolicy.getPolicyPeriod());
                    policyModel.setDroolsInertialPeriod(runtimePolicy.getInertiaPeriod());

                    List<DroolsAction> droolsActions = new ArrayList<>();

                    for (RuntimePolicyAction action : runtimePolicy.getActions()) {

                        DroolsAction droolsAction = new DroolsAction();
                        droolsAction.setComponentHexID(action.getComponentHexID());
                        droolsAction.setComponentName(action.getComponentName());

                        String context = null;

                        switch (action.getType()) {
                            case "scaleOut":
                            case "scaleIn":
                                context = action.getWorkers() + "";
                                break;
                            default:
                                context = action.getContext();
                                break;
                        }

                        droolsAction.setContext(context);
                        droolsAction.setRuleAction(ActionType.valueOf(action.getType()));
                        droolsAction.setTopicName(null != action.getTopicName() ? action.getTopicName() : null);
                        droolsAction.setTopicPort(null != action.getTopicPort() ? action.getTopicPort() : null);
                        droolsAction.setTopicUrl(null != action.getTopicURL() ? action.getTopicURL() : null);
                        droolsActions.add(droolsAction);
                    }

                    policyModel.setDroolsActions(new ArrayList<>(droolsActions));

                    String policyModelAsString = objectMapper.writeValueAsString(policyModel);

                    kafkaSender.sendElasticityPolicy(policyModelAsString);

                    existingApplicationInstance.setLastModified(new Date());
                    applicationInstanceDAO.save(existingApplicationInstance);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    if (runtimePolicyDAO.existsById(runtimePolicy.getId())) {
                        if (null != runtimePolicyActionDAO.findAllByRuntimePolicy(runtimePolicy)
                                && !runtimePolicyActionDAO.findAllByRuntimePolicy(runtimePolicy).isEmpty()) {
                            runtimePolicyActionDAO.deleteAll(runtimePolicyActionDAO.findAllByRuntimePolicy(runtimePolicy));
                        }
                        runtimePolicyDAO.delete(runtimePolicy);
                    }
                    throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
                }
            } else {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }
        }
    }

    public void deleteElasticityRuntimePolicyById(Long id, Long policyID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        Optional<RuntimePolicy> existingRuntimePolicyOP = runtimePolicyDAO.findById(policyID);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) && existingRuntimePolicyOP.isPresent()
                && existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && existingApplicationInstance.getApplicationInstanceID()
                .equals(existingRuntimePolicyOP.get().getApplicationInstance().getApplicationInstanceID())
                && (existingApplicationInstance.hasEditAllowance(authenticatedUser))) {

            try {
                RuntimePolicy existingRuntimePolicy = existingRuntimePolicyOP.get();
                List<RuntimePolicyAction> runtimePolicyActions = runtimePolicyActionDAO.findAllByRuntimePolicy(existingRuntimePolicy);

                if (null != runtimePolicyActions && !runtimePolicyActions.isEmpty()) {
                    runtimePolicyActions.forEach(runtimePolicyAction -> {
                        runtimePolicyActionDAO.delete(runtimePolicyAction);
                    });
                }
                PolicyModel policyModel = new PolicyModel();
                policyModel.setPolicyHexID(existingRuntimePolicy.getHexID());
                policyModel.setCreation(false);
                policyModel.setGraphHexID(existingApplicationInstance.getApplication().getHexID());
                policyModel.setGraphInstanceHexID(existingApplicationInstance.getHexID());
                policyModel.setPolicyName(existingRuntimePolicy.getName());

                String policyModelAsString = objectMapper.writeValueAsString(policyModel);
                kafkaSender.sendElasticityPolicy(policyModelAsString);
                runtimePolicyDAO.delete(existingRuntimePolicy);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }

        }
    }

    public void createSecurityRuntimePolicyById(Long id, List<RuntimePolicy> runtimePolicies, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance)
                && existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && (existingApplicationInstance.hasEditAllowance(authenticatedUser))) {

            List<RuntimePolicy> newRuntimePolicies = new ArrayList<>();
            if (null != runtimePolicies && !runtimePolicies.isEmpty()) {
                List<RuntimePolicy> existingRuntimePolicies = runtimePolicyDAO
                        .findAllByApplicationInstanceAndType(existingApplicationInstance,
                                RuntimePolicy.Type.SECURITY.name());

                for (RuntimePolicy runtimePolicy : runtimePolicies) {
                    boolean requiredFields = null != runtimePolicy.getPolicy() && !runtimePolicy.getPolicy().isEmpty();

                    if (requiredFields) {
                        if (null == runtimePolicy.getId() || !runtimePolicyDAO.findById(runtimePolicy.getId()).isPresent()) {

                            try {
                                ActionsCommands actionsCommands = objectMapper.readValue(runtimePolicy.getPolicy(), ActionsCommands.class);
                                if (null != actionsCommands) {
                                    runtimePolicy.setDateCreated(new Date());
                                    runtimePolicy.setType(RuntimePolicy.Type.SECURITY.name());
                                    runtimePolicy.setLastModified(new Date());
                                    runtimePolicy.setApplicationInstance(existingApplicationInstance);
                                    runtimePolicy.setUser(authenticatedUser);
                                    runtimePolicyDAO.save(runtimePolicy);
                                    newRuntimePolicies.add(runtimePolicy);
                                    String actionsCommandsAsString = objectMapper.writeValueAsString(actionsCommands);
                                    kafkaSender.sendSecurityPolicy(actionsCommandsAsString);
                                }
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
                            }
                        } else {
                            RuntimePolicy existingRuntimePolicy = runtimePolicyDAO.findById(runtimePolicy.getId()).get();
                            try {
                                ActionsCommands actionsCommands = objectMapper.readValue(runtimePolicy.getPolicy(), ActionsCommands.class);
                                if (null != actionsCommands) {
                                    existingRuntimePolicy.setPolicy(runtimePolicy.getPolicy());
                                    existingRuntimePolicy.setName(null != runtimePolicy.getName() ? runtimePolicy.getName() : null);
                                    existingRuntimePolicy.setLastModified(new Date());
                                    runtimePolicyDAO.save(existingRuntimePolicy);
                                    newRuntimePolicies.add(existingRuntimePolicy);
                                    String actionsCommandsAsString = objectMapper.writeValueAsString(actionsCommands);
                                    kafkaSender.sendSecurityPolicy(actionsCommandsAsString);
                                }
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
                            }
                        }
                    }
                }
                existingRuntimePolicies.forEach(existingRuntimePolicy -> {
                    if (newRuntimePolicies.stream().filter(runtimePolicy -> runtimePolicy.getId().equals(existingRuntimePolicy.getId()))
                            .collect(Collectors.toList()).isEmpty()) {
                        runtimePolicyDAO.delete(existingRuntimePolicy);
                    }
                });
            }
        }
    }

    public List<ElasticityHistory> fetchByApplicationInstanceAndAndComponentNodeOrderByIdDesc(ApplicationInstance applicationInstance,
            ComponentNode componentNode) {
        Optional<List<ElasticityHistory>> elasticityHistoryOp = elasticityHistoryDAO
                .findByApplicationInstanceAndAndComponentNodeOrderByIdDesc(applicationInstance, componentNode);
        return elasticityHistoryOp.orElse(new ArrayList<>());
    }
}
