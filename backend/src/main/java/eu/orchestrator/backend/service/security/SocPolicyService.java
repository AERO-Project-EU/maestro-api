package eu.orchestrator.backend.service.security;

import eu.orchestrator.backend.config.SocConfig;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.exception.NotFoundException;
import eu.orchestrator.backend.kafka.Sender;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.SocPolicyTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.repository.dao.SocPolicyDAO;
import eu.orchestrator.repository.dao.SocPolicyDroolsExpressionDAO;
import eu.orchestrator.repository.dao.SocPolicyDroolsExpressionHeaderDAO;
import eu.orchestrator.repository.dao.SocPolicyInputKafkaStreamDAO;
import eu.orchestrator.repository.dao.SocPolicyInputKafkaStreamFieldModelDAO;
import eu.orchestrator.repository.dao.SocPolicyKafkaExpressionDAO;
import eu.orchestrator.repository.dao.SocPolicyOutputDroolsActionDAO;
import eu.orchestrator.repository.dao.SocPolicyOutputDroolsActionHeaderDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.SocPolicy;
import eu.orchestrator.repository.domain.SocPolicyDroolsExpression;
import eu.orchestrator.repository.domain.SocPolicyDroolsExpressionHeader;
import eu.orchestrator.repository.domain.SocPolicyInputKafkaStream;
import eu.orchestrator.repository.domain.SocPolicyInputKafkaStreamFieldModel;
import eu.orchestrator.repository.domain.SocPolicyKafkaExpression;
import eu.orchestrator.repository.domain.SocPolicyOutputDroolsAction;
import eu.orchestrator.repository.domain.SocPolicyOutputDroolsActionHeader;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.entities.soc.DroolsExpression;
import eu.orchestrator.transfer.entities.soc.FieldModel;
import eu.orchestrator.transfer.entities.soc.InputKafkaStream;
import eu.orchestrator.transfer.entities.soc.KafkaConfig;
import eu.orchestrator.transfer.entities.soc.KafkaExpression;
import eu.orchestrator.transfer.entities.soc.OutputDroolsAction;
import eu.orchestrator.transfer.entities.soc.SocRuleModel;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;

import com.google.gson.Gson;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class SocPolicyService {

    private static final Logger logger = Logger.getLogger(SocPolicyService.class.getName());

    @Value("${kafka.topic.agent-soc-configuration}")
    private String socAgentTopic;

    @Value("${soc.server.host}")
    private String socHost;

    @Value("${soc.server.broker-port}")
    private String socPort;

    @Value("${ui.server.url}")
    private String uiURL;

    @Autowired
    private SocConfig socConfig;

    @Autowired
    private Sender kafkaSender;

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private SocPolicyDAO socPolicyDAO;

    @Autowired
    private SocPolicyInputKafkaStreamDAO socPolicyInputKafkaStreamDAO;

    @Autowired
    private SocPolicyInputKafkaStreamFieldModelDAO socPolicyInputKafkaStreamFieldModelDAO;

    @Autowired
    private SocPolicyKafkaExpressionDAO socPolicyKafkaExpressionDAO;

    @Autowired
    private SocPolicyOutputDroolsActionHeaderDAO socPolicyOutputDroolsActionHeaderDAO;

    @Autowired
    private SocPolicyDroolsExpressionDAO socPolicyDroolsExpressionDAO;

    @Autowired
    private SocPolicyDroolsExpressionHeaderDAO socPolicyDroolsExpressionHeaderDAO;

    @Autowired
    private SocPolicyOutputDroolsActionDAO socPolicyOutputDroolsActionDAO;


    public Page fetchSocPolicyByApplicationInstanceId(Long id, Pageable pageable, User authenticatedUser) {
        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        if (applicationInstance == null) {
            throw new NotAuthorizedException(GenericMessage.APPLICATION_NOT_EXIST.getCode(), GenericMessage.APPLICATION_NOT_EXIST);
        }
        Page<SocPolicy> page;
        if (pageable.getPageSize() > 100) {
            page = socPolicyDAO.findAllByApplicationInstance(applicationInstance, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = socPolicyDAO.findAllByApplicationInstance(applicationInstance, pageable);
        }
        List<SocPolicyTO> securityPolicyTOS = new ArrayList<>();
        if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
            final User loginUser = authenticatedUser;
            page.getContent().forEach(socPolicy -> {
                SocPolicyTO socPolicyTO = new SocPolicyTO();
                socPolicyTO.setAllowEdit(false);
                socPolicyTO.setAllowDelete(socPolicy.hasDeleteAllowance(loginUser));
                socPolicyTO.setId(socPolicy.getId());
                socPolicyTO.setName(socPolicy.getName());
                socPolicyTO.setDateCreated(socPolicy.getDateCreated());
                socPolicyTO.setUsername(socPolicy.getUser().getUsername());
                socPolicyTO.setStatus(SocPolicy.RuntimePolicyStatus.valueOf(socPolicy.getStatus()).getFriendlyName());
                securityPolicyTOS.add(socPolicyTO);
            });
        }
        return new PageImpl<>(securityPolicyTOS, pageable, page.getTotalElements());
    }

    public SocPolicy fetchByHexID(String policyHexID) {
        Optional<SocPolicy> socPolicyOptional = socPolicyDAO.findByHexID(policyHexID);
        return socPolicyOptional.orElse(null);
    }

    public void save(SocPolicy socPolicy) {
        socPolicyDAO.save(socPolicy);
    }

    public void createSocPolicyByApplicationInstanceId(Long id, SocPolicy socPolicy, User authenticatedUser) {
        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        if (applicationInstance == null) {
            throw new NotAuthorizedException(GenericMessage.APPLICATION_NOT_EXIST.getCode(), GenericMessage.APPLICATION_NOT_EXIST);
        }
        // store socPolicy
        SocPolicy socPolicyDB = new SocPolicy();
        String socPolicyHexID = Util.createRandomHEXString();
        socPolicyDB.setHexID(socPolicyHexID);
        socPolicyDB.setApplicationInstance(applicationInstance);
        socPolicyDB.setDateCreated(new Date());
        socPolicyDB.setLastModified(new Date());
        socPolicyDB.setUser(authenticatedUser);
        socPolicyDB.setDroolsInertiaPeriodInSecond(socPolicy.getDroolsInertiaPeriodInSecond());
        socPolicyDB.setKafkaRuleExpression(socPolicy.getKafkaRuleExpression());
        socPolicyDB.setName(socPolicy.getName());
        socPolicyDB.setStatus(SocPolicy.RuntimePolicyStatus.PENDING.name());
        socPolicyDB = socPolicyDAO.save(socPolicyDB);
        // store InputStream
        List<InputKafkaStream> inputKafkaStreamList = new ArrayList<>();
        for (SocPolicyInputKafkaStream socPolicyInputKafkaStream : socPolicy.getInputStreamList()) {
            if (socPolicyInputKafkaStream.getInputTopic().isEmpty()) {
                continue;
            }
            SocPolicyInputKafkaStream socPolicyInputKafkaStreamDB = new SocPolicyInputKafkaStream();
            socPolicyInputKafkaStreamDB.setInputTopic(socPolicyInputKafkaStream.getInputTopic());
            socPolicyInputKafkaStreamDB.setDateCreated(new Date());
            socPolicyInputKafkaStreamDB.setLastModified(new Date());
            socPolicyInputKafkaStreamDB.setSocPolicy(socPolicyDB);
            socPolicyInputKafkaStreamDB = socPolicyInputKafkaStreamDAO.save(socPolicyInputKafkaStreamDB);

            List<FieldModel> fieldModels = new ArrayList<>();
            for (SocPolicyInputKafkaStreamFieldModel socPolicyInputKafkaStreamFieldModel : socPolicyInputKafkaStream.getFieldModelList()) {
                if (socPolicyInputKafkaStreamFieldModel.getName().isEmpty()) {
                    continue;
                }
                socPolicyInputKafkaStreamFieldModel.setDateCreated(new Date());
                socPolicyInputKafkaStreamFieldModel.setLastModified(new Date());
                socPolicyInputKafkaStreamFieldModel.setSocPolicyInputKafkaStream(socPolicyInputKafkaStreamDB);
                socPolicyInputKafkaStreamFieldModelDAO.save(socPolicyInputKafkaStreamFieldModel);

                FieldModel fieldModel = new FieldModel();
                fieldModel.setFieldName(socPolicyInputKafkaStreamFieldModel.getName());
                fieldModel.setFieldType(FieldModel.FieldType.valueOf(socPolicyInputKafkaStreamFieldModel.getType()));
                fieldModels.add(fieldModel);
            }
            InputKafkaStream inputKafkaStream = new InputKafkaStream();
            inputKafkaStream.setInputTopic(socPolicyInputKafkaStreamDB.getInputTopic());
            inputKafkaStream.setFieldModelList(fieldModels);
            inputKafkaStreamList.add(inputKafkaStream);
        }
        // store KafkaExpression
        List<KafkaExpression> kafkaExpressionList = new ArrayList<>();
        for (SocPolicyKafkaExpression socPolicyKafkaExpression : socPolicy.getKafkaExpressionList()) {
            if (socPolicyKafkaExpression.getInputTopic().isEmpty()) {
                continue;
            }
            SocPolicyKafkaExpression socPolicyKafkaExpressionDB = new SocPolicyKafkaExpression();
            socPolicyKafkaExpressionDB.setDateCreated(new Date());
            socPolicyKafkaExpressionDB.setLastModified(new Date());
            socPolicyKafkaExpressionDB.setSocPolicy(socPolicyDB);
            socPolicyKafkaExpressionDB.setInputTopic(socPolicyKafkaExpression.getInputTopic());
            if (socPolicyKafkaExpression.getLogical().isEmpty()) {
                socPolicyKafkaExpressionDB.setContext(socPolicyKafkaExpression.getContext());
                socPolicyKafkaExpressionDB.setFieldName(socPolicyKafkaExpression.getFieldName());
                socPolicyKafkaExpressionDB.setOperand(socPolicyKafkaExpression.getOperand());
            } else {
                socPolicyKafkaExpressionDB.setLogical(socPolicyKafkaExpression.getLogical());
            }
            socPolicyKafkaExpressionDB = socPolicyKafkaExpressionDAO.save(socPolicyKafkaExpressionDB);

            KafkaExpression kafkaExpression = new KafkaExpression();
            BeanUtils.copyProperties(socPolicyKafkaExpressionDB, kafkaExpression);
            if (null != socPolicyKafkaExpressionDB.getLogical()) {
                kafkaExpression.setLogical(KafkaExpression.Logical.valueOf(socPolicyKafkaExpressionDB.getLogical()));
            }
            if (null != socPolicyKafkaExpressionDB.getOperand()) {
                kafkaExpression.setOperand(KafkaExpression.Operand.valueOf(socPolicyKafkaExpressionDB.getOperand()));
            }
            kafkaExpressionList.add(kafkaExpression);
        }

        // store DroolExpressionList
        List<DroolsExpression> droolsExpressionList = new ArrayList<>();
        for (SocPolicyDroolsExpression socPolicyDroolsExpression : socPolicy.getDroolsExpressionList()) {
            if (socPolicyDroolsExpression.getType().isEmpty()) {
                continue;
            }
            SocPolicyDroolsExpression socPolicyDroolsExpressionDB = new SocPolicyDroolsExpression();
            socPolicyDroolsExpressionDB.setDateCreated(new Date());
            socPolicyDroolsExpressionDB.setLastModified(new Date());
            socPolicyDroolsExpressionDB.setApplicationHexID(applicationInstance.getApplication().getHexID());
            socPolicyDroolsExpressionDB.setApplicationInstanceHexID(applicationInstance.getHexID());
            socPolicyDroolsExpressionDB.setComponentNodeHexID(
                    socPolicyDroolsExpression.getComponentNodeHexID().isEmpty() ? null : socPolicyDroolsExpression.getComponentNodeHexID());
            socPolicyDroolsExpressionDB.setComponentNodeInstanceHexID(
                    socPolicyDroolsExpression.getComponentNodeInstanceHexID().isEmpty() ? null : socPolicyDroolsExpression.getComponentNodeInstanceHexID());
            socPolicyDroolsExpressionDB
                    .setBrokerTopicName(socPolicyDroolsExpression.getBrokerTopicName().isEmpty() ? null : socPolicyDroolsExpression.getBrokerTopicName());
            socPolicyDroolsExpressionDB.setRestMethod(socPolicyDroolsExpression.getRestMethod().isEmpty() ? null : socPolicyDroolsExpression.getRestMethod());
            socPolicyDroolsExpressionDB.setContext(socPolicyDroolsExpression.getContext());
            socPolicyDroolsExpressionDB.setUrl(socPolicyDroolsExpression.getUrl());
            socPolicyDroolsExpressionDB.setType(socPolicyDroolsExpression.getType());
            socPolicyDroolsExpressionDB.setSocPolicy(socPolicyDB);
            socPolicyDroolsExpressionDB = socPolicyDroolsExpressionDAO.save(socPolicyDroolsExpressionDB);

            Map<String, String> header = new HashMap<>();
            for (SocPolicyDroolsExpressionHeader socPolicyDroolsExpressionHeader : socPolicyDroolsExpression.getHeader()) {
                if (socPolicyDroolsExpressionHeader.getName().isEmpty()) {
                    continue;
                }
                socPolicyDroolsExpressionHeader.setLastModified(new Date());
                socPolicyDroolsExpressionHeader.setLastModified(new Date());
                socPolicyDroolsExpressionHeader.setSocPolicyDroolsExpression(socPolicyDroolsExpressionDB);
                socPolicyDroolsExpressionHeaderDAO.save(socPolicyDroolsExpressionHeader);
                header.put(socPolicyDroolsExpressionHeader.getName(), socPolicyDroolsExpressionHeader.getValue());
            }
            DroolsExpression droolsExpression = new DroolsExpression();
            BeanUtils.copyProperties(socPolicyDroolsExpressionDB, droolsExpression);
            droolsExpression.setHeader(header.isEmpty() ? null : header);
            droolsExpression.setGraphHexID(applicationInstance.getApplication().getHexID());
            droolsExpression.setGraphInstanceHexID(applicationInstance.getHexID());
            droolsExpression.setType(DroolsExpression.Type.valueOf(socPolicyDroolsExpressionDB.getType()));
            if (null != socPolicyDroolsExpressionDB.getRestMethod()) {
                droolsExpression.setRestMethod(DroolsExpression.RestMethod.valueOf(socPolicyDroolsExpressionDB.getRestMethod()));
            }
            droolsExpressionList.add(droolsExpression);
        }
        // store OutputDroolAction
        List<OutputDroolsAction> outputDroolsActionList = new ArrayList<>();
        for (SocPolicyOutputDroolsAction socPolicyOutputDroolsAction : socPolicy.getOutputDroolsActionList()) {
            if (socPolicyOutputDroolsAction.getType().isEmpty()) {
                continue;
            }
            SocPolicyOutputDroolsAction socPolicyOutputDroolsActionDB = new SocPolicyOutputDroolsAction();
            socPolicyOutputDroolsActionDB.setDateCreated(new Date());
            socPolicyOutputDroolsActionDB.setLastModified(new Date());
            socPolicyOutputDroolsActionDB.setApplicationHexID(applicationInstance.getApplication().getHexID());
            socPolicyOutputDroolsActionDB.setApplicationInstanceHexID(applicationInstance.getHexID());
            socPolicyOutputDroolsActionDB.setComponentNodeHexID(
                    socPolicyOutputDroolsAction.getComponentNodeHexID().isEmpty() ? null : socPolicyOutputDroolsAction.getComponentNodeHexID());
            socPolicyOutputDroolsActionDB.setComponentNodeInstanceHexID(
                    socPolicyOutputDroolsAction.getComponentNodeInstanceHexID().isEmpty() ? null : socPolicyOutputDroolsAction.getComponentNodeInstanceHexID());
            socPolicyOutputDroolsActionDB.setType(socPolicyOutputDroolsAction.getType());

            if (socPolicyOutputDroolsAction.getType().equals(SocPolicyOutputDroolsAction.Type.SOC_ACTION.name())) {
                socPolicyOutputDroolsAction.setBrokerTopicName(socAgentTopic);
                socPolicyOutputDroolsAction.setUrl(socHost + ":" + socPort);
            }
            socPolicyOutputDroolsActionDB
                    .setBrokerTopicName(socPolicyOutputDroolsAction.getBrokerTopicName().isEmpty() ? null : socPolicyOutputDroolsAction.getBrokerTopicName());
            socPolicyOutputDroolsActionDB.setUrl(socPolicyOutputDroolsAction.getUrl());
            socPolicyOutputDroolsActionDB
                    .setRestMethod(socPolicyOutputDroolsAction.getRestMethod().isEmpty() ? null : socPolicyOutputDroolsAction.getRestMethod());
            socPolicyOutputDroolsActionDB
                    .setSocAction(socPolicyOutputDroolsAction.getSocAction().isEmpty() ? null : socPolicyOutputDroolsAction.getSocAction());
            socPolicyOutputDroolsActionDB.setContext(socPolicyOutputDroolsAction.getContext());
            socPolicyOutputDroolsActionDB.setSocPolicy(socPolicyDB);
            socPolicyOutputDroolsActionDB = socPolicyOutputDroolsActionDAO.save(socPolicyOutputDroolsActionDB);

            Map<String, String> header = new HashMap<>();
            for (SocPolicyOutputDroolsActionHeader socPolicyOutputDroolsActionHeader : socPolicyOutputDroolsAction.getHeader()) {
                if (socPolicyOutputDroolsActionHeader.getName().isEmpty()) {
                    continue;
                }
                socPolicyOutputDroolsActionHeader.setLastModified(new Date());
                socPolicyOutputDroolsActionHeader.setLastModified(new Date());
                socPolicyOutputDroolsActionHeader.setSocPolicyOutputDroolsAction(socPolicyOutputDroolsActionDB);
                socPolicyOutputDroolsActionHeaderDAO.save(socPolicyOutputDroolsActionHeader);
                header.put(socPolicyOutputDroolsActionHeader.getName(), socPolicyOutputDroolsActionHeader.getValue());
            }

            OutputDroolsAction droolsAction = new OutputDroolsAction();
            BeanUtils.copyProperties(socPolicyOutputDroolsActionDB, droolsAction);
            droolsAction.setHeader(header.isEmpty() ? null : header);
            droolsAction.setGraphHexID(applicationInstance.getApplication().getHexID());
            droolsAction.setGraphInstanceHexID(applicationInstance.getHexID());
            droolsAction.setType(OutputDroolsAction.Type.valueOf(socPolicyOutputDroolsActionDB.getType()));
            if (null != socPolicyOutputDroolsActionDB.getRestMethod()) {
                droolsAction.setRestMethod(OutputDroolsAction.RestMethod.valueOf(socPolicyOutputDroolsActionDB.getRestMethod()));
            }
            if (null != socPolicyOutputDroolsActionDB.getSocAction()) {
                droolsAction.setSocAction(OutputDroolsAction.SocAction.valueOf(socPolicyOutputDroolsActionDB.getSocAction()));
            }
            outputDroolsActionList.add(droolsAction);
        }
        SocRuleModel socRuleModel = new SocRuleModel();
        socRuleModel.setHexID(socPolicyHexID);
        socRuleModel.setKafkaRuleExpression(socPolicy.getKafkaRuleExpression());
        socRuleModel.setInputStreamList(inputKafkaStreamList);
        socRuleModel.setKafkaExpressionList(kafkaExpressionList);
        socRuleModel.setDroolsExpressionList(droolsExpressionList);
        socRuleModel.setOutputDroolsActionList(outputDroolsActionList);
        socRuleModel.setDroolsInertiaPeriodInSecond(Integer.decode(socPolicyDB.getDroolsInertiaPeriodInSecond()));
        socRuleModel.setCallbackURLManager(uiURL + "/api/v1/callback/policy/soc/manager/" + socPolicyHexID);
        socRuleModel.setCallbackURLDrools(uiURL + "/api/v1/callback/policy/soc/drools/" + socPolicyHexID);
        socRuleModel.setCrudOperation(SocRuleModel.CrudOperation.CREATE);

        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.setKafkaHost(socConfig.getHost());
        kafkaConfig.setKafkaKSQLPort(socConfig.getKsqlPort());
        kafkaConfig.setKafkaBrokerPort(socConfig.getBrokerPort());
        socRuleModel.setKafkaConfig(kafkaConfig);
        Gson gson = new Gson();
        String message = gson.toJson(socRuleModel);
        logger.log(Level.INFO, message);
        kafkaSender.sendSocPolicy(message);
    }

    public void deleteSocPolicyByApplicationInstanceId(Long id, Long socPolicyID) {
        Optional<SocPolicy> socPolicyOptional = socPolicyDAO.findById(socPolicyID);
        if (!socPolicyOptional.isPresent()) {
            throw new NotFoundException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
        }
        SocPolicy socPolicy = socPolicyOptional.get();
        // store OutputDroolAction
        List<OutputDroolsAction> outputDroolsActionList = new ArrayList<>();
        for (SocPolicyOutputDroolsAction socPolicyOutputDroolsAction : socPolicy.getOutputDroolsActionList()) {
            if (socPolicyOutputDroolsAction.getType().isEmpty()) {
                continue;
            }
            SocPolicyOutputDroolsAction socPolicyOutputDroolsActionDB = new SocPolicyOutputDroolsAction();
            socPolicyOutputDroolsActionDB.setDateCreated(new Date());
            socPolicyOutputDroolsActionDB.setLastModified(new Date());
            socPolicyOutputDroolsActionDB.setApplicationHexID(socPolicy.getApplicationInstance().getApplication().getHexID());
            socPolicyOutputDroolsActionDB.setApplicationInstanceHexID(socPolicy.getApplicationInstance().getHexID());
            socPolicyOutputDroolsActionDB.setComponentNodeHexID(StringUtils.isEmpty(
                    socPolicyOutputDroolsAction.getComponentNodeHexID()) ? null : socPolicyOutputDroolsAction.getComponentNodeHexID());
            socPolicyOutputDroolsActionDB.setComponentNodeInstanceHexID(StringUtils.isEmpty(
                    socPolicyOutputDroolsAction.getComponentNodeInstanceHexID()) ? null : socPolicyOutputDroolsAction.getComponentNodeInstanceHexID());
            socPolicyOutputDroolsActionDB.setBrokerTopicName(
                    StringUtils.isEmpty(socPolicyOutputDroolsAction.getBrokerTopicName()) ? null : socPolicyOutputDroolsAction.getBrokerTopicName());
            socPolicyOutputDroolsActionDB
                    .setRestMethod(StringUtils.isEmpty(socPolicyOutputDroolsAction.getRestMethod()) ? null : socPolicyOutputDroolsAction.getRestMethod());
            socPolicyOutputDroolsActionDB
                    .setSocAction(StringUtils.isEmpty(socPolicyOutputDroolsAction.getSocAction()) ? null : socPolicyOutputDroolsAction.getSocAction());
            socPolicyOutputDroolsActionDB.setContext(socPolicyOutputDroolsAction.getContext());
            socPolicyOutputDroolsActionDB.setUrl(socPolicyOutputDroolsAction.getUrl());
            socPolicyOutputDroolsActionDB.setType(socPolicyOutputDroolsAction.getType());
            socPolicyOutputDroolsActionDB.setSocPolicy(socPolicy);

            Map<String, String> header = new HashMap<>();
            for (SocPolicyOutputDroolsActionHeader socPolicyOutputDroolsActionHeader : socPolicyOutputDroolsAction.getHeader()) {
                if (socPolicyOutputDroolsActionHeader.getName().isEmpty()) {
                    continue;
                }
                socPolicyOutputDroolsActionHeader.setLastModified(new Date());
                socPolicyOutputDroolsActionHeader.setLastModified(new Date());
                socPolicyOutputDroolsActionHeader.setSocPolicyOutputDroolsAction(socPolicyOutputDroolsActionDB);
                header.put(socPolicyOutputDroolsActionHeader.getName(), socPolicyOutputDroolsActionHeader.getValue());
            }
            OutputDroolsAction droolsAction = new OutputDroolsAction();
            BeanUtils.copyProperties(socPolicyOutputDroolsActionDB, droolsAction);
            droolsAction.setHeader(header.isEmpty() ? null : header);
            droolsAction.setGraphHexID(socPolicy.getApplicationInstance().getApplication().getHexID());
            droolsAction.setGraphInstanceHexID(socPolicy.getApplicationInstance().getHexID());
            droolsAction.setType(OutputDroolsAction.Type.valueOf(socPolicyOutputDroolsActionDB.getType()));
            if (null != socPolicyOutputDroolsActionDB.getRestMethod()) {
                droolsAction.setRestMethod(OutputDroolsAction.RestMethod.valueOf(socPolicyOutputDroolsActionDB.getRestMethod()));
            }
            if (null != socPolicyOutputDroolsActionDB.getSocAction()) {
                droolsAction.setSocAction(OutputDroolsAction.SocAction.valueOf(socPolicyOutputDroolsActionDB.getSocAction()));
            }
            outputDroolsActionList.add(droolsAction);
        }
        SocRuleModel socRuleModel = new SocRuleModel();
        socRuleModel.setHexID(socPolicyOptional.get().getHexID());
        socRuleModel.setOutputDroolsActionList(outputDroolsActionList);
        socRuleModel.setCrudOperation(SocRuleModel.CrudOperation.DELETE);

//    SocRuleModel socRuleModel = new SocRuleModel();
//    socRuleModel.setHexID(socPolicyOptional.get().getHexID());
//    socRuleModel.setCrudOperation(SocRuleModel.CrudOperation.DELETE);

        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.setKafkaHost(socConfig.getHost());
        kafkaConfig.setKafkaKSQLPort(socConfig.getKsqlPort());
        kafkaConfig.setKafkaBrokerPort(socConfig.getBrokerPort());
        socRuleModel.setKafkaConfig(kafkaConfig);

        Gson gson = new Gson();
        String message = gson.toJson(socRuleModel);
        logger.log(Level.INFO, message);
        kafkaSender.sendSocPolicy(message);

        List<SocPolicyInputKafkaStream> socPolicyInputKafkaStreamList = socPolicyInputKafkaStreamDAO.findAllBySocPolicy(socPolicyOptional.get());
        socPolicyInputKafkaStreamList.forEach(socPolicyInputKafkaStream -> {
            List<SocPolicyInputKafkaStreamFieldModel> socPolicyInputKafkaStreamFieldModelList = socPolicyInputKafkaStreamFieldModelDAO
                    .findAllBySocPolicyInputKafkaStream(socPolicyInputKafkaStream);
            socPolicyInputKafkaStreamFieldModelList.forEach(socPolicyInputKafkaStreamFieldModel -> {
                socPolicyInputKafkaStreamFieldModelDAO.deleteById(socPolicyInputKafkaStreamFieldModel.getId());
            });
            socPolicyInputKafkaStreamDAO.deleteById(socPolicyInputKafkaStream.getId());
        });
        List<SocPolicyKafkaExpression> kafkaExpressionList = socPolicyKafkaExpressionDAO.findAllBySocPolicy(socPolicyOptional.get());
        kafkaExpressionList.stream().forEach(kafkaExpression -> {
            socPolicyKafkaExpressionDAO.deleteById(kafkaExpression.getId());
        });
        List<SocPolicyDroolsExpression> socPolicyDroolsExpressionList = socPolicyDroolsExpressionDAO.findAllBySocPolicy(socPolicyOptional.get());
        socPolicyDroolsExpressionList.stream().forEach(socPolicyDroolsExpression -> {
            List<SocPolicyDroolsExpressionHeader> socPolicyDroolsExpressionHeaderList = socPolicyDroolsExpressionHeaderDAO
                    .findAllBySocPolicyDroolsExpression(socPolicyDroolsExpression);
            socPolicyDroolsExpressionHeaderList.stream().forEach(socPolicyDroolsExpressionHeader -> {
                socPolicyDroolsExpressionHeaderDAO.deleteById(socPolicyDroolsExpressionHeader.getId());
            });
            socPolicyDroolsExpressionDAO.deleteById(socPolicyDroolsExpression.getId());
        });
        List<SocPolicyOutputDroolsAction> socPolicyOutputDroolsActionList = socPolicyOutputDroolsActionDAO.findAllBySocPolicy(socPolicyOptional.get());
        socPolicyOutputDroolsActionList.stream().forEach(socPolicyOutputDroolsAction -> {
            List<SocPolicyOutputDroolsActionHeader> socPolicyOutputDroolsActionHeaderList = socPolicyOutputDroolsActionHeaderDAO
                    .findAllBySocPolicyOutputDroolsAction(socPolicyOutputDroolsAction);
            socPolicyOutputDroolsActionHeaderList.stream().forEach(socPolicyDroolsExpressionHeader -> {
                socPolicyOutputDroolsActionHeaderDAO.deleteById(socPolicyDroolsExpressionHeader.getId());
            });
            socPolicyOutputDroolsActionDAO.deleteById(socPolicyOutputDroolsAction.getId());
        });
        socPolicyDAO.deleteById(socPolicyOptional.get().getId());
    }

    public void updateSocForAllComponentNodeInstances(Long id, Boolean flag) {
        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        if (applicationInstance != null) {
            List<ComponentNodeInstance> componentNodeInstancesList
                    = componentNodeInstanceService.fetchAllComponentNodeInstancesByApplicationInstance(applicationInstance);
            componentNodeInstancesList.forEach(componentNodeInstance -> {
                componentNodeInstance.setStatusSOC(flag);
                componentNodeInstanceService.saveComponentNodeInstance(componentNodeInstance);
            });
        } else {
            throw new NotFoundException(GenericMessage.APPLICATION_NOT_EXIST.getCode(), GenericMessage.APPLICATION_NOT_EXIST);
        }
    }
}
