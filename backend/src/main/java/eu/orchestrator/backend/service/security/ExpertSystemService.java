package eu.orchestrator.backend.service.security;

import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.SpiderRuleDAO;
import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceIP;
import eu.orchestrator.repository.domain.SpiderRule;
import eu.orchestrator.transfer.entities.spider.RuleDto;
import eu.orchestrator.transfer.entities.spider.RulesDto;
import eu.orchestrator.transfer.entities.spider.ScoreDto;
import eu.orchestrator.transfer.entities.spider.puzzletransfer.ActionDto;
import eu.orchestrator.transfer.entities.spider.puzzletransfer.ConditionDto;
import eu.orchestrator.transfer.entities.spider.puzzletransfer.DeletePoliciesDto;
import eu.orchestrator.transfer.entities.spider.puzzletransfer.DroolsConfigDto;
import eu.orchestrator.transfer.entities.spider.puzzletransfer.ExpertSystemDto;
import eu.orchestrator.transfer.entities.spider.puzzletransfer.ExpertSystemResponseDto;
import eu.orchestrator.transfer.entities.spider.puzzletransfer.PoliciesDto;
import eu.orchestrator.transfer.entities.spider.puzzletransfer.PoliciesResponseDto;
import eu.orchestrator.transfer.entities.spider.puzzletransfer.QueryDto;
import eu.orchestrator.transfer.entities.spider.puzzletransfer.StreamDto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.transaction.Transactional;


@Service
@Transactional(rollbackOn = Exception.class)
public class ExpertSystemService {

    private static final Logger logger = Logger.getLogger(ExpertSystemService.class.getName());
    private static final String PUBLIC_IP = "PublicIP";

    private static final RestTemplate restTemplate = new RestTemplate();

    @Value("${expert-system.server.url}")
    private String expertSystemUrl;

    @Autowired
    ApplicationInstanceService applicationInstanceService;

    @Autowired
    SpiderRuleDAO spiderRuleDAO;


    public void convertAndSendRulesToExpertSystem(Long applicationInstanceId, RulesDto rules) {
        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceId);
        if (applicationInstance == null) {
            logger.log(Level.SEVERE, "Expert System Service No Application found");
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(),
                    GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }

        if (NullCheckUtil.isEmpty(rules.getRules())) {
            logger.log(Level.SEVERE, "Expert System Service No Rules received");
            throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING);
        }

        logger.log(Level.INFO, "Expert System Service Rules Received");
        for (RuleDto rl : rules.getRules()) {
            if (NullCheckUtil.isEmpty(rl.getRule()) || NullCheckUtil.isEmpty(rl.getSgi()) || NullCheckUtil.isEmpty(rl.getType()) || NullCheckUtil.isEmpty(
                    rl.getStep())
                    || NullCheckUtil.isEmpty(rl.getPoints())) {
                logger.log(Level.SEVERE, "Expert System No Rules or Sgi or Type or Points or Step found");
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                        GenericMessage.REQUIRED_FIELDS_MISSING);
            }
            rl.setRule(replaceRulePlaceholder(applicationInstance, rl.getRule()));
        }
        sendRulesToExpertSystem(rules, applicationInstance);
    }

    public void deleteRulesFromExpertSystem(ApplicationInstance applicationInstance) {
        logger.log(Level.INFO, "Expert System Service delete Rules");
        try {
            List<SpiderRule> spiderRuleList = spiderRuleDAO.findAllByApplicationInstance(applicationInstance);
            DeletePoliciesDto deletePoliciesDto = new DeletePoliciesDto();
            List<String> dpolicies = new ArrayList<>();
            for (SpiderRule sr : spiderRuleList) {
                dpolicies.add(sr.getPolicyId());
            }
            deletePoliciesDto.setPolicies(dpolicies);

            HttpEntity delEntity = new HttpEntity(deletePoliciesDto, null);
            ResponseEntity<String> responseDelEntity = restTemplate.exchange(expertSystemUrl + "/api/v1/policy/mass-delete", HttpMethod.POST, delEntity,
                    String.class);

            if (!responseDelEntity.getStatusCode().is2xxSuccessful()) {
                logger.log(Level.SEVERE, "Expert System Service Error on delete rules REST to ES");
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR);
            }

            spiderRuleDAO.deleteAll(spiderRuleList);
        } catch (Exception ex) {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR);
        }
    }

    private void sendRulesToExpertSystem(RulesDto rules, ApplicationInstance applicationInstance) {
        logger.log(Level.INFO, "Expert System Service send Rules");
        PoliciesDto policies = convertRulesToExpertSystemPolicies(rules, applicationInstance);
        try {
            HttpEntity entity = new HttpEntity(policies, null);
            logger.log(Level.INFO, "Expert System Service ES URL");
            ResponseEntity<PoliciesResponseDto> responseEntity = restTemplate.exchange(expertSystemUrl + "/api/v1/mass-policies", HttpMethod.POST, entity,
                    PoliciesResponseDto.class);
            PoliciesResponseDto policiesResponseDto = responseEntity.getBody();
            saveSpiderRules(policiesResponseDto, applicationInstance);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Expert System Service Error on send rules REST to ES" + ex.getMessage());
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR);
        }
    }

    private void saveSpiderRules(PoliciesResponseDto policiesResponseDto, ApplicationInstance applicationInstance) {
        logger.log(Level.INFO, "Expert System Service save Rules");
        if (null == policiesResponseDto || null == policiesResponseDto.getPolicies()) {
            logger.log(Level.SEVERE, "Expert System Service save Rules No Policies found");
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
        try {
            List<SpiderRule> spiderRuleList = new ArrayList<>();
            for (ExpertSystemResponseDto es : policiesResponseDto.getPolicies()) {
                SpiderRule spiderRule = new SpiderRule();
                spiderRule.setApplicationInstance(applicationInstance);
                spiderRule.setPolicyId(es.getId());
                spiderRule.setName(es.getName());
                spiderRule.setDateCreated(new Date());
                spiderRule.setLastModified(new Date());
                spiderRuleList.add(spiderRule);
            }
            spiderRuleDAO.saveAll(spiderRuleList);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Expert System Service Error on save rules to db");
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR);
        }
    }

    //(CREATE[^;]+;) regex
    private PoliciesDto convertRulesToExpertSystemPolicies(RulesDto rules, ApplicationInstance applicationInstance) {
        logger.log(Level.INFO, "Expert System Service Convert Rules to Expert System policies");
        PoliciesDto policies = new PoliciesDto();
        List<ExpertSystemDto> expertSystemList = new ArrayList<>();
        for (RuleDto rl : rules.getRules()) {
            ExpertSystemDto expertSystemDto = new ExpertSystemDto();

            expertSystemDto.setName("scenario-" + rl.getSgi() + "-" + rl.getStep());

            ConditionDto conditionDto = new ConditionDto();
            StreamDto streamDto = new StreamDto();
            QueryDto queryDto = new QueryDto();
            List<String> statements = new ArrayList<>();

            Pattern p = Pattern.compile("(CREATE[^;]+;)");
            Matcher m = p.matcher(rl.getRule());
            while (m.find()) {
                statements.add(Base64.getEncoder().encodeToString(m.group().getBytes()));
            }
            queryDto.setStatements(statements);
            streamDto.setKsql(queryDto);
            conditionDto.setStream(streamDto);

            DroolsConfigDto droolsConfigDto = new DroolsConfigDto();
            droolsConfigDto.setInertiaTime(5);

            conditionDto.setDrools(droolsConfigDto);

            expertSystemDto.setCondition(conditionDto);

            ActionDto actionDto = new ActionDto();
            actionDto.setMethod("POST");
            actionDto.setEndpoint("/api/v1/progress");
            ScoreDto scoreDto = new ScoreDto();
            scoreDto.setSgi(applicationInstance.getHexID());
            scoreDto.setType(rl.getType().toLowerCase(Locale.ROOT));
            scoreDto.setPoints(rl.getPoints());
            scoreDto.setStep(rl.getStep());

            String body;
            try {
                ObjectMapper mapper = new ObjectMapper();
                body = mapper.writeValueAsString(scoreDto);
            } catch (JsonProcessingException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR);
            }
            if (body == null) {
                logger.log(Level.SEVERE, "Expert System Service Error on body convertion for Expert System");
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR);
            }
            actionDto.setBody(Base64.getEncoder().encodeToString(body.getBytes()));

            expertSystemDto.setAction(actionDto);

            expertSystemList.add(expertSystemDto);
        }
        policies.setPolicies(expertSystemList);

        return policies;
    }

//    private void sendAndSaveRule(ExpertSystemDto expertSystemDto, RuleDto ruleDto) {
//        try {
//            HttpEntity entity = new HttpEntity(expertSystemDto, null);
//            ResponseEntity<ExpertSystemResponseDto> responseEntity = restTemplate.exchange(expertSystemUrl + "/api/v1/policy", HttpMethod.POST, entity,
//                    ExpertSystemResponseDto.class);
//            ExpertSystemResponseDto expertSystemResponseDto = responseEntity.getBody();
//            saveRule(expertSystemResponseDto, ruleDto);
//        } catch (Exception ex) {
//            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(),
//                    GenericMessage.GENERIC_ERROR);
//        }
//    }
//
//    private void saveRule(ExpertSystemResponseDto expertSystemResponseDto, RuleDto ruleDto) {
//        SpiderRule spiderRule = new SpiderRule();
//        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(ruleDto.getSgi());
//        spiderRule.setApplicationInstance(applicationInstance);
//        spiderRule.setPolicyId(expertSystemResponseDto.getId());
//        spiderRule.setName(expertSystemResponseDto.getName());
//        spiderRule.setDateCreated(new Date());
//        spiderRule.setLastModified(new Date());
//        spiderRuleDAO.save(spiderRule);
//    }


    /*
       Placeholders are:
       @CIDR_COMPOMENT_NAME@ = Network IP range of the component network (i.e 10.25.0.0)
       @IP_COMPONENT_NAME@ = component IP
       @NODENAME_COMPONENT_NAME = the host name of the VM of component

       REGEX
       @(IP_.+)@
       @(NODENAME_.+)@
       @(CIDR_.+)@

     */
    private String replaceRulePlaceholder(ApplicationInstance applicationInstance, String rule) {
        logger.log(Level.INFO, "Expert System Service Replace Rules with maestro info");
        byte[] decodedBytes = Base64.getDecoder().decode(rule);
        rule = new String(decodedBytes);

        SortedSet<ComponentNodeInstance> componentNodeInstances = applicationInstance.getComponentNodeInstances();
        rule = replaceIpPlaceHolder(rule, componentNodeInstances);
        rule = replaceCidrPlaceHolder(rule, componentNodeInstances);
        rule = replaceNodeNamePlaceHolder(rule, applicationInstance);

        return rule;
    }


    private String replaceIpPlaceHolder(String rule, SortedSet<ComponentNodeInstance> componentNodeInstances) {
        logger.log(Level.INFO, "Expert System Service Replace Rules with maestro IP");
        String ip = "@IP_";

        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile("@(IP_.+)@");
        Matcher m = p.matcher(rule);

        while (m.find()) {
            String tmpIp = m.group();

            String componentName = tmpIp.replace(ip, "").replace("@", "");
            ComponentNodeInstance cni = findCni(componentName, componentNodeInstances);
            if (cni == null) {
                logger.log(Level.SEVERE, "Expert System Service No CNI found");
                throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_INSTANCE_IS_NOT_EXISTS.getCode(),
                        GenericMessage.COMPONENT_NODE_INSTANCE_IS_NOT_EXISTS);
            }
            ComponentNodeInstanceIP cniIp = cni.getComponentNodeInstanceIPs()
                    .stream().filter(x -> !x.getNetwork().equalsIgnoreCase(PUBLIC_IP)).findFirst().orElse(null);
            if (cniIp == null) {
                logger.log(Level.SEVERE, "Expert System Service No CNI IP found");
                throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_INSTANCE_IP_IS_NOT_EXISTS.getCode(),
                        GenericMessage.COMPONENT_NODE_INSTANCE_IP_IS_NOT_EXISTS);
            }

            m.appendReplacement(sb, cniIp.getIp());
        }
        m.appendTail(sb);
        rule = sb.toString();
        return rule;
    }

    private String replaceCidrPlaceHolder(String rule, SortedSet<ComponentNodeInstance> componentNodeInstances) {
        logger.log(Level.INFO, "Expert System Service Replace Rules with maestro CIDR");
        String cidr = "@CIDR_";

        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile("@(CIDR_.+)@");
        Matcher m = p.matcher(rule);

        while (m.find()) {
            String tmpCidr = m.group();

            String componentName = tmpCidr.replaceAll(cidr, "").replaceAll("@", "");
            ComponentNodeInstance cni = findCni(componentName, componentNodeInstances);
            if (cni == null) {
                logger.log(Level.SEVERE, "Expert System Service No CNI found");
                throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_INSTANCE_IS_NOT_EXISTS.getCode(),
                        GenericMessage.COMPONENT_NODE_INSTANCE_IS_NOT_EXISTS);
            }
            ComponentNodeInstanceIP cniIp = cni.getComponentNodeInstanceIPs()
                    .stream().filter(x -> !x.getNetwork().equalsIgnoreCase(PUBLIC_IP)).findFirst().orElse(null);
            if (cniIp == null) {
                logger.log(Level.SEVERE, "Expert System Service No CNI IP found");
                throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_INSTANCE_IP_IS_NOT_EXISTS.getCode(),
                        GenericMessage.COMPONENT_NODE_INSTANCE_IP_IS_NOT_EXISTS);
            }
            String ipWithNoFinalPart = cniIp.getIp().replaceAll("(.*\\.)\\d+$", "$1");
            ipWithNoFinalPart = ipWithNoFinalPart + "0";
            m.appendReplacement(sb, ipWithNoFinalPart);
        }
        m.appendTail(sb);
        rule = sb.toString();
        return rule;
    }

    private String replaceNodeNamePlaceHolder(String rule, ApplicationInstance applicationInstance) {
        logger.log(Level.INFO, "Expert System Service Replace Rules with maestro Node Name");
        String nodeName = "@NODENAME_";

        Application application = applicationInstance.getApplication();
        SortedSet<ComponentNodeInstance> componentNodeInstances = applicationInstance.getComponentNodeInstances();

        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile("@(NODENAME_.+)@");
        Matcher m = p.matcher(rule);

        while (m.find()) {
            String tmpNode = m.group();

            String componentName = tmpNode.replace(nodeName, "").replace("@", "");
            ComponentNodeInstance cni = findCni(componentName, componentNodeInstances);
            if (cni == null) {
                logger.log(Level.SEVERE, "Expert System Service No CNI IP found");
                throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_INSTANCE_IS_NOT_EXISTS.getCode(),
                        GenericMessage.COMPONENT_NODE_INSTANCE_IS_NOT_EXISTS);
            }
            //appHexId-appInstanceHexId-componentNodeHexId-componentNodeInstanceHexID
            String finalNodeName = application.getHexID().toLowerCase(Locale.ROOT) + "-" + applicationInstance.getHexID().toLowerCase(Locale.ROOT)
                    + "-" + cni.getComponentNode().getHexID() + "-" + cni.getHexID();

            m.appendReplacement(sb, finalNodeName);
        }
        m.appendTail(sb);
        rule = sb.toString();
        return rule;
    }

    private ComponentNodeInstance findCni(String componentName, SortedSet<ComponentNodeInstance> componentNodeInstances) {
        logger.log(Level.INFO, "Expert System Service Find CNI");
        String cniRegex = "(" + componentName + ".+)|(" + componentName + ")";
        Pattern cniP = Pattern.compile(cniRegex);
        for (ComponentNodeInstance cni : componentNodeInstances) {
            Matcher m = cniP.matcher(cni.getName());
            if (m.matches()) {
                return cni;
            }
        }
        return null;
    }

}
