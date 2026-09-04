package eu.orchestrator.backend.rest.support.external;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.rest.applicationinstance.ApplicationInstanceController;
import eu.orchestrator.backend.rest.security.SocPolicyController;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.service.application.ApplicationService;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.resourceprovider.ProviderService;
import eu.orchestrator.backend.service.security.ExpertSystemService;
import eu.orchestrator.backend.transfer.ApplicationInstanceDeploymentTo;
import eu.orchestrator.backend.transfer.ApplicationInstanceGraphTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.entities.spider.RulesDto;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

import static eu.orchestrator.repository.domain.QProvider.provider;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 28/1/21
 */
@RestController
@RequestMapping("/api/v1/wrapper")
@SuppressWarnings("Duplicates")
public class WrapperController {

    private static final Logger logger = Logger.getLogger(WrapperController.class.getName());
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)

    @Autowired
    private AuthService authService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private ExpertSystemService expertSystemService;

    @Autowired
    private ApplicationInstanceController applicationInstanceController;

    @Autowired
    private SocPolicyController socPolicyController;

    @Value("${server.port}")
    private String backendPort;


    @GetMapping(value = "/application/deploy/{id}/{name}")
    public ResponseEntity<RestResponseSPA<Serializable>> deployApplicationWrapper(@PathVariable Long id, @PathVariable String name,
            HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0 && null != name && !name.isEmpty();
        if (requiredFields) {
            try {
                User authenticatedUser = authService.getAuthenticatedUser();
                Application exApplication = applicationService.fetchApplicationById(id);
                if (exApplication != null && (authenticatedUser.isAdmin() || exApplication.getPublicApplication()
                        || exApplication.getOrganization().equals(authenticatedUser.getOrganization()))) {
                    // Try to fetch an application instace with the same name
                    ApplicationInstance exApplicationInstance;
                    if (!authenticatedUser.isAdmin()) {
                        exApplicationInstance = applicationInstanceService.fetchApplicationInstanceByNameAndOrganization(name,
                                authenticatedUser.getOrganization());
                    } else {
                        exApplicationInstance = applicationInstanceService.fetchApplicationInstanceByName(name);
                    }
                    //  Check if there is already an application instance with the requested name exists
                    if (null != exApplicationInstance) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_ALREADY_EXISTS.getCode(),
                                        GenericMessage.APPLICATION_INSTANCE_ALREADY_EXISTS.getMessage(request)));
                    } else {
                        BooleanExpression predicate = provider.eq(provider).and(provider.providerID.notIn(-1L, -2L))
                                .and(provider.internalProvider.eq(false)).and(provider.defaultProvider.eq(true))
                                .and(provider.organization.eq(authenticatedUser.getOrganization()));
                        Provider provider = providerService.fetchByOne(predicate);
                        if (null == provider) {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(new RestResponseSPA<>(GenericMessage.DEFAULT_PROVIDER_MISSING.getCode(),
                                            GenericMessage.DEFAULT_PROVIDER_MISSING.getMessage(request)));
                        }

                        // Create Application Instance object
                        ApplicationInstance applicationInstance = new ApplicationInstance();
                        applicationInstance.setProvider(provider);
                        applicationInstance.setApplication(exApplication);
                        applicationInstance.setName(name);
                        applicationInstance.setOverlay(false);
                        ResponseEntity response = applicationInstanceController.create(applicationInstance, request);

                        if (response.getStatusCode().value() == HttpStatus.CREATED.value()) {
                            if (!response.hasBody()) {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA(GenericMessage.GENERIC_ERROR.getCode(),
                                        GenericMessage.GENERIC_ERROR.getMessage(request)));
                            }
                            RestResponseSPA restResponseSpa = (RestResponseSPA) response.getBody();
                            if (restResponseSpa.getReturnobject() == null) {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA(GenericMessage.GENERIC_ERROR.getCode(),
                                        GenericMessage.GENERIC_ERROR.getMessage(request)));
                            }
                            // Enable SOC for all the components
                            ApplicationInstanceGraphTO applicationInstanceGraphTo = (ApplicationInstanceGraphTO) restResponseSpa.getReturnobject();
                            response = socPolicyController
                                    .updateSocForAllComponentNodeInstances(applicationInstanceGraphTo.getApplicationInstanceID(), true, request);
                            if (response.getStatusCode().value() != HttpStatus.OK.value()) {
                                return response;
                            }
                            HttpHeaders headers = new HttpHeaders();
                            Enumeration<String> headerNames = request.getHeaderNames();
                            if (headerNames != null) {
                                while (headerNames.hasMoreElements()) {
                                    String headerName = headerNames.nextElement();
                                    headers.add(headerName, request.getHeader(headerName));
                                }
                            }
                            //Check if Volumes are Set
                            String volumeCheckUri = "http://localhost:" + backendPort + "/api/v1/applicationinstance/volume/check/"
                                    + applicationInstanceGraphTo.getApplicationInstanceID();
                            RestTemplate volumeRestTemplate = new RestTemplate();
                            HttpEntity volumeEntity = new HttpEntity(headers);
                            ResponseEntity volumeResponseEntity = volumeRestTemplate.exchange(volumeCheckUri, HttpMethod.GET, volumeEntity, String.class);
                            if (null == volumeResponseEntity || volumeResponseEntity.getStatusCode() != HttpStatus.OK) {
                                return volumeResponseEntity;
                            }
                            // Request for Deployment
                            String requestDemploymentUri = "http://localhost:" + backendPort + "/api/v1/applicationinstance/"
                                    + applicationInstanceGraphTo.getApplicationInstanceID() + "/request/deployment";
                            RestTemplate restTemplate = new RestTemplate();
                            HttpEntity entity = new HttpEntity(headers);
                            ResponseEntity responseEntity = restTemplate.exchange(requestDemploymentUri, HttpMethod.POST, entity, String.class);
                            if (null == responseEntity || responseEntity.getStatusCode() != HttpStatus.ACCEPTED) {
                                return responseEntity;
                            }
                            ApplicationInstanceDeploymentTo applicationInstanceDeploymentTo = new ApplicationInstanceDeploymentTo(
                                    applicationInstanceGraphTo.getApplicationInstanceID(), applicationInstanceGraphTo.getHexID());
                            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                                    GenericMessage.GENERIC_SUCCESS.getMessage(request), applicationInstanceDeploymentTo));
                        }
                        return response;
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new RestResponseSPA<>(GenericMessage.APPLICATION_NOT_AUTHORIZED.getCode(),
                                    GenericMessage.APPLICATION_NOT_AUTHORIZED.getMessage(request)));
                }
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @PostMapping(value = "/application/rules/{applicationInstanceID}/add")
    public ResponseEntity<RestResponseSPA<Serializable>> addRulesWrapper(@PathVariable(value = "applicationInstanceID") Long applicationInstanceId,
            @RequestBody RulesDto rules, HttpServletRequest request) {
        try {
            expertSystemService.convertAndSendRulesToExpertSystem(applicationInstanceId, rules);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }
}
