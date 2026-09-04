package eu.orchestrator.backend.service.elasticity;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.service.component.ComponentNodeService;
import eu.orchestrator.backend.transfer.ProfileMetricTO;
import eu.orchestrator.backend.transfer.ProfileTO;
import eu.orchestrator.backend.transfer.ProfilingResultTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ProfileDAO;
import eu.orchestrator.repository.dao.ProfileMetricDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.Profile;
import eu.orchestrator.repository.domain.ProfileMetric;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.entities.physiognomica.PhysiognomicaModel;
import eu.orchestrator.transfer.entities.physiognomica.PhysiognomicaPeriodModel;
import eu.orchestrator.transfer.entities.ui.UIEnum;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class ProfileService {

    private static final Logger logger = Logger.getLogger(ProfileService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();

    @Value("${analytic.engine.server.url}")
    private String analyticEngineURL;

    @Value("${prometheus.server.url}")
    private String prometheusServerURL;

    @Value("${ui.server.url}")
    private String uiURL;

    @Value("${analytic.server.server.url}")
    private String analyticServerURL;

    @Value("${analytic.server.server.domain}")
    private String analyticServerDomain;

    @Value("${ui.server.url}")
    private String uiServerURL;

    @Autowired
    private ProfileDAO profileDAO;

    @Autowired
    private ProfileMetricDAO profileMetricDAO;

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeService componentNodeService;

    @Autowired
    private ApplicationInstanceDAO applicationInstanceDAO;


    public List<UIEnum> fetchProfileAlgorithm() {
        RestTemplate restTemplate = new RestTemplate();
        logger.log(Level.INFO, "----->" + analyticEngineURL + "/list");
        ResponseEntity responseEntity = restTemplate.getForEntity(analyticEngineURL + "/list", String.class);
        JSONArray jsonArray = new JSONArray(responseEntity.getBody().toString());
        List<UIEnum> allAlgorithms = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            UIEnum uiEnum = new UIEnum();
            uiEnum.setName(object.getString("name"));
            uiEnum.setFriendlyName(object.getString("name").toLowerCase().replaceAll("_", " "));
            allAlgorithms.add(uiEnum);
        }
        return allAlgorithms;
    }

    public Page fetchProfileById(Long id, Pageable pageable, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) && existingApplicationInstance.getStatus()
                .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && (authenticatedUser.isAdmin() || existingApplicationInstance.getOrganization().getId()
                .equals(authenticatedUser.getOrganization().getId()))) {

            Page<Profile> page = null;

            if (pageable.getPageSize() > 100) {
                page = profileDAO.findAllByApplicationInstance(existingApplicationInstance,
                        PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
            } else {
                page = profileDAO.findAllByApplicationInstance(existingApplicationInstance, pageable);
            }

            List<ProfileTO> profileTOS = new ArrayList<>();

            if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {

                final User loginUser = authenticatedUser;
                page.getContent().stream().forEach(profile -> {

                    ProfileTO profileTO = new ProfileTO();
                    BeanUtils.copyProperties(profile, profileTO);

                    profileTO.setAllowDelete(profile.hasDeleteAllowance(loginUser));
                    profileTO.setAllowEdit(profile.hasEditAllowance(loginUser));

                    profileTOS.add(profileTO);
                });

            }
            return new PageImpl<>(profileTOS, pageable, page.getTotalElements());
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public Profile fetchByHexID(String profileHexID) {
        Optional<Profile> profileOP = profileDAO.findByHexID(profileHexID);
        return profileOP.orElse(null);
    }

    public void save(Profile profile) {
        profileDAO.save(profile);
    }

    public void createProfileById(Long id, ProfileTO profileTO, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) && existingApplicationInstance.getStatus()
                .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && (existingApplicationInstance.hasEditAllowance(authenticatedUser))) {

            boolean requiredFields =
                    null != profileTO && null != profileTO.getName() && !profileTO.getName()
                            .isEmpty()
                            && null != profileTO.getAlgorithm() && !profileTO.getAlgorithm().isEmpty()
                            && null != profileTO.getStep() && profileTO.getStep() > 0
                            && null != profileTO.getStartTime()
                            && null != profileTO.getEndTime();

            boolean componentNotExist = false;

            if (requiredFields) {
                Optional<Profile> profileOP = profileDAO.findByApplicationInstanceAndName(existingApplicationInstance, profileTO.getName());

                if (profileOP.isPresent()) {
                    throw new GenericBusinessException(GenericMessage.PROFILE_ALREADY_EXISTS.getCode(), GenericMessage.PROFILE_ALREADY_EXISTS);
                }
                if (!StringUtils.isAlphanumeric(profileTO.getName())) {
                    throw new GenericBusinessException(GenericMessage.PROFILE_NAME_ALPHANUMERIC.getCode(), GenericMessage.PROFILE_NAME_ALPHANUMERIC);
                }

                try {
           /* DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = utcFormat.parse(profileTO.getEndTime().toString());
            System.out.println("endTime from UI: " + profileTO.getEndTime() );
            System.out.println("startTime from UI: " + profileTO.getStartTime() );
            System.out.println("current now machine date :" + new Date() );
            System.out.println("existing deployed TIme on UTC: " + existingApplicationInstance.getDateDeployed());*/

                    //check if time range is correct
                    if (profileTO.getStartTime().compareTo(profileTO.getEndTime()) >= 0) {
                        throw new GenericBusinessException(GenericMessage.PROFILE_NO_VALID_TIME_RANGE.getCode(), GenericMessage.PROFILE_NO_VALID_TIME_RANGE);
                    }

                    List<ProfileMetricTO> existingProfileMetrics = profileTO.getProfileMetrics();

          /*  if (profileTO.getAlgorithm().compareTo(profileAlgorithm.TimeSeriesDecomposition.toString()) == 0 && existingProfileMetrics.size() != 1 ){

              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                  body(new RestResponseSPA(GenericMessage.PROFILE_ALGORITHM_UNSUPPORTED_INPUT.getCode(),
                      GenericMessage.PROFILE_ALGORITHM_UNSUPPORTED_INPUT.getMessage(request)));

            } else if (profileTO.getAlgorithm().compareTo(profileAlgorithm.LinearRegression.toString()) == 0 && existingProfileMetrics.size() != 2  ) {

              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                  body(new RestResponseSPA(GenericMessage.PROFILE_ALGORITHM_UNSUPPORTED_INPUT.getCode(),
                      GenericMessage.PROFILE_ALGORITHM_UNSUPPORTED_INPUT.getMessage(request)));
            }*/

                    Profile profile = new Profile();
                    profile.setApplicationInstance(existingApplicationInstance);
                    profile.setName(profileTO.getName());
                    profile.setStep(profileTO.getStep());
                    profile.setAlgorithm(profileTO.getAlgorithm());
                    profile.setUser(authenticatedUser);
                    profile.setHexID(Util.createRandomHEXString());
                    profile.setEndTime(profileTO.getEndTime());
                    profile.setStartTime(profileTO.getStartTime());
                    profile.setDateCreated(new Date());
                    profile.setLastModified(new Date());
                    profile.setProfileMetrics(null);
                    profile.setStatus(Profile.ProfileStatus.PROCESS.getFriendlyName());
                    profile = profileDAO.save(profile);

                    for (ProfileMetricTO metric : existingProfileMetrics) {

                        ComponentNode componentNode = componentNodeService.fetchComponentNodeByHexId(metric.getComponentNodeHexId());

                        //check if component node exist
                        if (NullCheckUtil.isEmpty(componentNode)) {
                            componentNotExist = true;
                            throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_IS_NOT_EXIST.getCode(),
                                    GenericMessage.COMPONENT_NODE_IS_NOT_EXIST);
                        }

                        ProfileMetric profileMetric = new ProfileMetric();
                        profileMetric.setComponentNodeName(componentNode.getName());
                        profileMetric.setComponentNodeHexID(componentNode.getHexID());
                        profileMetric.setMetric(metric.getMetric());
                        profileMetric.setDimension(metric.getDimension());
                        profileMetric.setDateCreated(new Date());
                        profileMetric.setLastModified(new Date());
                        profileMetric.setProfile(profile);

                        profileMetricDAO.save(profileMetric);
                    }

                    if (existingApplicationInstance.getProfiles().contains(profile)) {
                        existingApplicationInstance.getProfiles().add(profile);
                        existingApplicationInstance.setLastModified(new Date());
                        applicationInstanceDAO.save(existingApplicationInstance);
                    }

                    PhysiognomicaPeriodModel periodModel = new PhysiognomicaPeriodModel();
                    periodModel.setStart(profile.getStartTime().toInstant().toString());
                    periodModel.setEnd(profile.getEndTime().toInstant().toString());

                    List<PhysiognomicaPeriodModel> periodModelList = new ArrayList<>();
                    periodModelList.add(periodModel);

                    //Make the call to physiognomica
                    PhysiognomicaModel physiognomicaModel = new PhysiognomicaModel();
                    physiognomicaModel.setPeriods(periodModelList);
                    physiognomicaModel.setName(profile.getAlgorithm());
                    physiognomicaModel.setStep(profile.getStep() + "m");
                    physiognomicaModel.setVendor("Maestro");
                    physiognomicaModel.setCallback(uiURL + "/api/v1/callback/profile/" + profile.getHexID());

                    //TODO[tip] replace the following IP for local development

                    List<ProfileMetric> profileMetrics = profileMetricDAO.findAllByProfile(profile);
                    List<String> metrics = new ArrayList<>();

                    for (ProfileMetric profileMetric : profileMetrics) {
                        if (profileMetric.getMetric().compareTo("inin_ping_rtt_ms") == 0) {
                            String temp = profileMetric.getMetric() + "{" + profileMetric.getDimension() + "}";
                            metrics.add(temp);
                        } else {
                            String temp = "AVG(netdata:";
                            temp = temp + existingApplicationInstance.getApplication().getHexID() + ":";
                            temp = temp + existingApplicationInstance.getHexID() + ":";
                            temp = temp + profileMetric.getComponentNodeHexID() + profileMetric.getMetric() + "{dimension='" + profileMetric.getDimension()
                                    + "'})";
                            metrics.add(temp);
                        }
                    }

                    physiognomicaModel.setMetrics(metrics);

                    String physiognomicaModelAsString = objectMapper.writeValueAsString(physiognomicaModel);
                    logger.info("Rest call on " + analyticEngineURL + "/analytic_service with Object: " + physiognomicaModelAsString);

                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.postForObject(analyticEngineURL + "/analytic_service", physiognomicaModelAsString, String.class);

                } catch (Exception e) {

                    logger.log(Level.SEVERE, e.getMessage());

                    Optional<Profile> existingProfileOP = profileDAO.findByApplicationInstanceAndName(existingApplicationInstance, profileTO.getName());

                    if (existingProfileOP.isPresent()) {
                        Profile existingProfile = existingProfileOP.get();

                        List<ProfileMetric> profileMetrics = profileMetricDAO.findAllByProfile(existingProfile);

                        profileMetrics.stream().forEach(profileMetric -> {
                            profileMetricDAO.delete(profileMetric);
                        });

                        if (existingApplicationInstance.getProfiles().contains(existingProfile)) {
                            existingApplicationInstance.getProfiles().remove(existingProfile);
                            existingApplicationInstance.setLastModified(new Date());
                            applicationInstanceDAO.save(existingApplicationInstance);
                        }

                        profileDAO.delete(existingProfile);
                    }

                    if (componentNotExist) {
                        throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_IS_NOT_EXIST.getCode(),
                                GenericMessage.COMPONENT_NODE_IS_NOT_EXIST);
                    }

                    throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(),
                            GenericMessage.GENERIC_ERROR);
                }

            } else {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                        GenericMessage.REQUIRED_FIELDS_MISSING);
            }
        }
        throw new NotAuthorizedException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(),
                GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
    }

    public void deleteProfileById(Long id, Long profileID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        Optional<Profile> existingProfileOP = profileDAO.findById(profileID);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) && existingProfileOP.isPresent()
                && existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && existingApplicationInstance.getApplicationInstanceID()
                .equals(existingProfileOP.get().getApplicationInstance().getApplicationInstanceID())
                && (existingApplicationInstance.hasEditAllowance(authenticatedUser))) {
            try {
                Profile existingProfile = existingProfileOP.get();
                List<ProfileMetric> profileMetrics = profileMetricDAO.findAllByProfile(existingProfile);
                profileMetrics.forEach(profileMetric -> {
                    profileMetricDAO.delete(profileMetric);
                });
                if (existingApplicationInstance.getProfiles().contains(existingProfile)) {
                    existingApplicationInstance.getProfiles().remove(existingProfile);
                    existingApplicationInstance.setLastModified(new Date());
                    applicationInstanceDAO.save(existingApplicationInstance);
                }
                profileDAO.delete(existingProfile);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }

        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public ProfilingResultTO fetchResult(Long id, Long profileID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        Optional<Profile> existingProfileOP = profileDAO.findById(profileID);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) && existingProfileOP.isPresent()
                && existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && existingApplicationInstance.getApplicationInstanceID()
                .equals(existingProfileOP.get().getApplicationInstance().getApplicationInstanceID())
                && (existingApplicationInstance.hasEditAllowance(authenticatedUser))) {

            try {
                Profile existingProfile = existingProfileOP.get();

                if (existingProfile.getStatus().equals(Profile.ProfileStatus.COMPLETE.getFriendlyName())) {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity responseEntity = restTemplate.getForEntity(
                            analyticEngineURL + "/results/" + existingProfile.getHexID(), String.class);
                    JSONObject entity = new JSONObject(responseEntity.getBody().toString());
                    JSONArray resultsList = entity.getJSONArray("results");
                    String htmlTResult = "";

                    for (int i = 0; i < resultsList.length(); i++) {
                        JSONObject relust = resultsList.getJSONObject(i);
                        String type = relust.getString("type");
                        String url = relust.getString("result");
                        if (type.equals("html")) {
                            htmlTResult = url;
                        }
                    }

                    if (null != analyticServerDomain && !analyticServerDomain.isEmpty()) {
                        htmlTResult = htmlTResult.replace("http://tng-analytics-rserver", analyticServerDomain);
                    } else {
                        htmlTResult = htmlTResult.replace("http://tng-analytics-rserver", analyticServerURL);
                    }

                    ProfilingResultTO profilingResultTO = new ProfilingResultTO();
                    profilingResultTO.setAlgorithm(existingProfile.getAlgorithm());
                    profilingResultTO.setStep(existingProfile.getStep().toString());
                    profilingResultTO.setStartTime(existingProfile.getStartTime().toString());
                    profilingResultTO.setEndTime(existingProfile.getEndTime().toString());
                    profilingResultTO.setOutputURl(htmlTResult);

                    logger.log(Level.INFO, "------------------- {}", htmlTResult);

                    return profilingResultTO;
                }
                return null;
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    //TODO need to deleted !
    public List<String> fetchProfilingURLsById(Long id, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) && existingApplicationInstance.getStatus()
                .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && (authenticatedUser.isAdmin()
                || existingApplicationInstance.getOrganization().getId().equals(authenticatedUser.getOrganization().getId()))) {
            try {

                List<String> profilingURLs = new ArrayList<>();
                String token;
                String physiognomicaURL = analyticEngineURL + "/ocpu/library/Physiognomica/R/getMaestroPrometheusMetrics";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("prometheous_metrics_per_graph",
                        "'" + uiServerURL + "/api/v1/external/applicationInstance/"
                                + existingApplicationInstance
                                .getHexID() + "/metrics'");

                HttpEntity<MultiValueMap<String, String>> physiognomicaRequest1 = new HttpEntity<>(map, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(physiognomicaURL, physiognomicaRequest1, String.class);

                if (null != response && null != response.getStatusCode() && response.getStatusCode().is2xxSuccessful()) {

                    token = response.getBody().substring(10);
                    token = token.substring(0, token.indexOf("/"));

                    if (!token.isEmpty()) {
                        MultiValueMap<String, String> map2 = new LinkedMultiValueMap<>();
                        map2.add("prometheus_url", "'" + prometheusServerURL + "'");
                        map2.add("MyData", token);

                        HttpEntity<MultiValueMap<String, String>> physiognomicaRequest2 = new HttpEntity<>(map2, headers);
                        physiognomicaURL = analyticEngineURL + "/ocpu/library/Physiognomica/R/enrichMaestroPrometheusMetricsWithDimensions";
                        ResponseEntity<String> response2 = restTemplate.postForEntity(physiognomicaURL, physiognomicaRequest2, String.class);

                        if (null != response2 && null != response2.getStatusCode() && response2.getStatusCode().is2xxSuccessful()) {

                            String newToken = response2.getBody().substring(10);
                            newToken = newToken.substring(0, newToken.indexOf("/"));

                            if (!newToken.isEmpty()) {
                                MultiValueMap<String, String> map3 = new LinkedMultiValueMap<String, String>();
                                map3.add("prometheus_url", "'" + prometheusServerURL + "'");
                                map3.add("metrics_list", newToken);
                                map3.add("step", "'3m'");
                                Instant now = Instant.now();
                                Instant plus5min = now.plus(5, ChronoUnit.MINUTES);
                                map3.add("end", "'" + plus5min.toString() + "'");
                                Instant minus1Hour = plus5min.minus(1, ChronoUnit.HOURS);
                                map3.add("start", "'" + minus1Hour.toString() + "'");
                                HttpEntity<MultiValueMap<String, String>> physiognomicaRequest3 = new HttpEntity<>(map3, headers);
                                physiognomicaURL = analyticEngineURL + "/ocpu/library/Physiognomica/R/getCorrelogram";
                                ResponseEntity<String> response3 = restTemplate.postForEntity(physiognomicaURL, physiognomicaRequest3, String.class);

                                if (null != response3 && null != response3.getStatusCode() && response3.getStatusCode().is2xxSuccessful()) {
                                    String newToken2 = response3.getBody().substring(10);
                                    newToken2 = newToken2.substring(0, newToken2.indexOf("/"));
                                    profilingURLs.add(analyticEngineURL + "/ocpu/tmp/" + newToken2 + "/files/correlogram.svg");
                                    profilingURLs.add(analyticEngineURL + "/ocpu/tmp/" + newToken2 + "/files/metrics_appendix.csv");
                                }
                            }
                        }
                    }
                }

                if (!profilingURLs.isEmpty()) {
                    profilingURLs.sort((o1, o2) -> {
                        int f = o1.compareTo(o2);
                        return f;
                    });
                    return profilingURLs;
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

}
