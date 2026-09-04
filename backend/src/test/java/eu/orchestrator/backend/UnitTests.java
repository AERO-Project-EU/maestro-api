package eu.orchestrator.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.orchestrator.repository.dao.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import eu.orchestrator.repository.domain.*;
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Rollback(true)
@Transactional
@TestExecutionListeners({TransactionalTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UnitTests {

    private static final Logger logger = Logger.getLogger(UnitTests.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    MockMvc mockMvc;

    @Before
    public void init() throws JsonProcessingException, JSONException {
        MockitoAnnotations.initMocks(this);

        headers.put("Content-Type", Arrays.asList("application/json"));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", "admin");
        jsonObject.put("password", "!admin!");

        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v1/auth/login"),
                HttpMethod.POST, entity, String.class);

        response.getHeaders().entrySet().stream().filter(header -> header.getKey().equals("Authorization")).forEach(header -> {

            token = header.getValue().get(0);

        });

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertTrue(token != null && !token.isEmpty());

        headers.put("Authorization", Arrays.asList(token));

    }

    // #TC01 - Create Component Descriptor
    @Test
    @Transactional
    @Rollback(true)
    public void tc01() {

        logger.info("#TC01 - Create Component Descriptor");

        Component testComponent = new Component();
        testComponent.setName("TestComponent");
        testComponent.setDockerImage("component:1.0.0");
        testComponent.setDockerRegistry("nexus:39580");
        testComponent.setPublicComponent(true);
        testComponent.setDateCreated(new Date());
        testComponent.setLastModified(new Date());
        testComponent.setElasticityController("NONE");
        testComponent.setArchitecture(Component.Architecture.X86.name());

        HttpEntity<Component> entity = new HttpEntity<>(testComponent, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/components"),
                HttpMethod.POST, entity, String.class);

        componentID = componentDAO.findByName("TestComponent").get().getId();

        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertTrue(null != componentID && componentID > 0);

        logger.info("#TC01 has been finished successfully!");

    }

    // #TC02 - Fetch Component Descriptor
    @Test
    @Transactional
    @Rollback(true)
    public void tc02() throws IOException, JSONException {

        componentID = componentDAO.findByName("TestComponent").get().getId();

        logger.info("#TC02 - Fetch Component Descriptor with ID " + componentID);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/components/" + componentID),
                HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        JSONObject jsonObject = new JSONObject(response.getBody());
        jsonObject.remove("_links");

        Component testComponent = objectMapper.readValue(jsonObject.toString(), Component.class);

        assertTrue(null != testComponent && null != testComponent.getName() && !testComponent.getName().isEmpty());

        logger.info("#TC02 has been finished successfully!");

    }

    // #TC03 - Update Component Descriptor
    @Test
    @Transactional
    @Rollback(true)
    public void tc03() throws IOException, JSONException {

        Component testComponent = componentDAO.findByName("TestComponent").get();
        componentID = testComponent.getId();

        logger.info("#TC03 - Update Component Descriptor with ID " + componentID);

        testComponent.setName("newTestComponent");

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(testComponent), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/components/" + componentID),
                HttpMethod.PATCH, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Component newTestComponent = componentDAO.findByName("newTestComponent").get();

        assertTrue(null != newTestComponent && null != newTestComponent.getName() && !newTestComponent.getName().isEmpty() && newTestComponent.getName().equals("newTestComponent"));

        logger.info("#TC03 has been finished successfully!");

    }

    // #TC04 - Delete Component Descriptor
    @Test
    @Transactional
    @Rollback(true)
    public void tc04() throws IOException, JSONException {

        Component testComponent = componentDAO.findByName("newTestComponent").get();
        componentID = testComponent.getId();

        logger.info("#TC04 - Delete Component Descriptor with ID " + componentID);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/components/" + componentID),
                HttpMethod.DELETE, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));

        logger.info("#TC04 has been finished successfully!");

    }

    // #TC05 - Create Application Graph
    @Test
    @Transactional
    @Rollback(true)
    public void tc05() throws IOException {

        logger.info("#TC05 - Create Application Graph");

        // Custom Metric  Application
        eu.orchestrator.repository.domain.Application customMetricApplication = new eu.orchestrator.repository.domain.Application();
        customMetricApplication.setName("TestApplication");
        customMetricApplication.setDateCreated(new Date());
        customMetricApplication.setUser(null);
        customMetricApplication.setPublicApplication(true);
        customMetricApplication.setLastModified(new Date());

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(customMetricApplication), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applications"),
                HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));

        applicationID = applicationDAO.findByName("TestApplication").get().getId();

        assertTrue(null != applicationID && applicationID > 0);

        logger.info("#TC05 has been finished successfully!");

    }

    // #TC06 - Fetch Application Graph
    @Test
    @Transactional
    @Rollback(true)
    public void tc06() throws IOException, JSONException {

        applicationID = applicationDAO.findByName("TestApplication").get().getId();

        logger.info("#TC06 - Fetch Application Graph with ID " + applicationID);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applications/" + applicationID),
                HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        JSONObject jsonObject = new JSONObject(response.getBody());
        jsonObject.remove("_links");

        eu.orchestrator.repository.domain.Application testApplication = objectMapper.readValue(jsonObject.toString(), eu.orchestrator.repository.domain.Application.class);

        assertTrue(null != testApplication && null != testApplication.getName() && !testApplication.getName().isEmpty());

        logger.info("#TC06 has been finished successfully!");

    }

    // #TC07 - Update Application Graph
    @Test
    @Transactional
    @Rollback(true)
    public void tc07() throws IOException, JSONException {

        eu.orchestrator.repository.domain.Application testApplication = applicationDAO.findByName("TestApplication").get();
        applicationID = testApplication.getId();

        logger.info("#TC07 - Update Application Graph with ID " + applicationID);

        testApplication.setName("newTestApplication");

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(testApplication), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applications/" + applicationID),
                HttpMethod.PATCH, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        eu.orchestrator.repository.domain.Application newTestApplication = applicationDAO.findByName("newTestApplication").get();

        assertTrue(null != newTestApplication && null != newTestApplication.getName() && !newTestApplication.getName().isEmpty() && newTestApplication.getName().equals("newTestApplication"));

        logger.info("#TC07 has been finished successfully!");

    }

    // #TC08 - Delete Application Graph
    @Test
    @Transactional
    @Rollback(true)
    public void tc08() throws IOException, JSONException {

        eu.orchestrator.repository.domain.Application testApplication = applicationDAO.findByName("newTestApplication").get();
        applicationID = testApplication.getId();

        logger.info("#TC08 - Delete Application Graph with ID " + applicationID);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applications/" + applicationID),
                HttpMethod.DELETE, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));

        logger.info("#TC08 has been finished successfully!");

    }

    // #TC13 - Register Telco Provider
    @Test
    @Transactional
    @Rollback(true)
    public void tc13() {

        logger.info("#TC13 - Register Telco Provider");

        Provider testProvider = new Provider();
        testProvider.setName("TestProvider");
        testProvider.setDateCreated(new Date());
        testProvider.setLastModified(new Date());
        testProvider.setDomain(null);
        testProvider.setEndpoint(null);
        testProvider.setProject(null);
        testProvider.setMeshIdentifier(null);
        testProvider.setDefaultProvider(false);
        testProvider.setEnabled(Boolean.TRUE);
        testProvider.setPassword("!telcoprovider!");
        testProvider.setUsername("telcoprovider");
        testProvider.setUser(null);
        testProvider.setRegions(null);
        testProvider.setInternalProvider(false);

        HttpEntity<Provider> entity = new HttpEntity<>(testProvider, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/providers"),
                HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));

        providerID = providerDAO.findByName("TestProvider").get().getProviderID();

        assertTrue(null != providerID && providerID > 0);

        logger.info("#TC13 has been finished successfully!");

    }

    // #TC14 - Fetch Telco Provider
    @Test
    @Transactional
    @Rollback(true)
    public void tc14() throws IOException, JSONException {

        providerID = providerDAO.findByName("TestProvider").get().getProviderID();

        logger.info("#TC14 - Fetch Telco Provider with ID " + providerID);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/providers/" + providerID),
                HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        JSONObject jsonObject = new JSONObject(response.getBody());
        jsonObject.remove("_links");

        Provider testProvider = objectMapper.readValue(jsonObject.toString(), Provider.class);

        assertTrue(null != testProvider && null != testProvider.getName() && !testProvider.getName().isEmpty());

        logger.info("#TC14 has been finished successfully!");

    }

    // #TC15 - Update Telco Provider
    @Test
    @Transactional
    @Rollback(true)
    public void tc15() throws IOException, JSONException {

        Provider testProvider = providerDAO.findByName("TestProvider").get();
        providerID = testProvider.getProviderID();

        logger.info("#TC15 - Update Telco Provider with ID " + providerID);

        testProvider.setName("newTestProvider");
        testProvider.setProviderType(null);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(testProvider), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/providers/" + providerID),
                HttpMethod.PATCH, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Provider newTestProvider = providerDAO.findByName("newTestProvider").get();

        assertTrue(null != newTestProvider && null != newTestProvider.getName() && !newTestProvider.getName().isEmpty() && newTestProvider.getName().equals("newTestProvider"));

        logger.info("#TC15 has been finished successfully!");

    }

    // #TC16 - De-register Telco Provider
    @Test
    @Transactional
    @Rollback(true)
    public void tc16() throws IOException, JSONException {

        Provider testProvider = providerDAO.findByName("newTestProvider").get();
        providerID = testProvider.getProviderID();

        logger.info("#TC16 - Delete Provider with ID " + providerID);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/providers/" + providerID),
                HttpMethod.DELETE, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));

        logger.info("#TC16 has been finished successfully!");

    }

    // #TC17 - Create Application Graph Instance
    @Test
    @Transactional
    @Rollback(true)
    public void tc17() throws JSONException {

        logger.info("#TC17 - Create Application Graph Instance");

        JSONObject testApplicationInstance = new JSONObject();
        testApplicationInstance.put("name", "TestApplicationInstance");
        testApplicationInstance.put("application", "/api/v2/applications/1");
        testApplicationInstance.put("provider", "/api/v2/providers/2");
        testApplicationInstance.put("overlay", true);

        HttpEntity<String> entity = new HttpEntity<>(testApplicationInstance.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applicationInstances"),
                HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));

        applicationInstanceID = applicationInstanceDAO.findByName("TestApplicationInstance").get().getApplicationInstanceID();

        assertTrue(null != applicationInstanceID && applicationInstanceID > 0);

        logger.info("#TC17 has been finished successfully!");

    }

    // #TC18 - Fetch Application Graph Instance
    @Test
    @Transactional
    @Rollback(true)
    public void tc18() throws IOException, JSONException {

        applicationInstanceID = applicationInstanceDAO.findByName("TestApplicationInstance").get().getApplicationInstanceID();

        logger.info("#TC18 - Application Graph Instance with ID " + applicationInstanceID);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applicationInstances/" + applicationInstanceID),
                HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        JSONObject jsonObject = new JSONObject(response.getBody());
        jsonObject.remove("_links");

        ApplicationInstance testApplicationInstance = objectMapper.readValue(jsonObject.toString(), ApplicationInstance.class);

        assertTrue(null != testApplicationInstance && null != testApplicationInstance.getName() && !testApplicationInstance.getName().isEmpty());

        logger.info("#TC18 has been finished successfully!");

    }

    // #TC19 - Update Application Graph Instance
    @Test
    @Transactional
    @Rollback(true)
    public void tc19() throws IOException, JSONException {

        ApplicationInstance testApplicationInstance = applicationInstanceDAO.findByName("TestApplicationInstance").get();
        applicationInstanceID = testApplicationInstance.getApplicationInstanceID();

        logger.info("#TC19 - Update Application Graph Instance with ID " + applicationInstanceID);

        JSONObject testApplicationInstanceObj = new JSONObject();
        testApplicationInstanceObj.put("applicationInstanceID", applicationInstanceID);
        testApplicationInstanceObj.put("name", "newTestApplicationInstance");
        testApplicationInstanceObj.put("application", "/api/v2/applications/1");
        testApplicationInstanceObj.put("provider", "/api/v2/providers/2");
        testApplicationInstanceObj.put("overlay", true);

        HttpEntity<String> entity = new HttpEntity<>(testApplicationInstanceObj.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applicationInstances/" + applicationInstanceID),
                HttpMethod.PATCH, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        ApplicationInstance newApplicationInstance = applicationInstanceDAO.findById(applicationInstanceID).get();

        assertTrue(null != newApplicationInstance && null != newApplicationInstance.getName() && !newApplicationInstance.getName().isEmpty());

        logger.info("#TC19 has been finished successfully!");

    }

    // #TC21 - Create Slice Intent
    // #TC28 - Request Slice
    @Test
    @Transactional
    @Rollback(true)
    public void tc2128() throws JSONException {

        logger.info("#TC21 - Create Slice Intent");

        applicationInstanceID = applicationInstanceDAO.findByName("newTestApplicationInstance").get().getApplicationInstanceID();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applicationInstances/" + applicationInstanceID + "/request/slice"),
                HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));

        logger.info("#TC21 has been finished successfully!");

    }

    // #TC22 - Fetch Slice Intent
    @Test
    @Transactional
    @Rollback(true)
    public void tc22() throws JSONException {

        logger.info("#TC22 - Fetch Slice Intent");

        applicationInstanceID = applicationInstanceDAO.findByName("newTestApplicationInstance").get().getApplicationInstanceID();
        sliceID = null != applicationInstanceDAO.findByName("newTestApplicationInstance").get().getSlice() ? applicationInstanceDAO.findByName("newTestApplicationInstance").get().getSlice().getId() : null;

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        if (null != sliceID) {

            ResponseEntity<String> response = restTemplate.exchange(
                    createURLWithPort("/api/v2/slices/" + sliceID),
                    HttpMethod.GET, entity, String.class);

            assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));

        }

        logger.info("#TC22 has been finished successfully!");

    }

    // #TC24 - Delete Slice Intent
    @Test
    @Transactional
    @Rollback(true)
    public void tc24() throws JSONException {

        logger.info("#TC24 - Delete Slice Intent");

        applicationInstanceID = applicationInstanceDAO.findByName("newTestApplicationInstance").get().getApplicationInstanceID();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applicationInstances/" + applicationInstanceID + "/request/cancellation"),
                HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));

        logger.info("#TC24 has been finished successfully!");

    }

    // #TC25 - Deploy Application Graph Instance
    // #TC29 - Execute Deployment
    @Test
    @Transactional
    @Rollback(true)
    public void tc2529() throws JSONException {

        logger.info("#TC25 - Deploy Application Graph Instance");

        applicationInstanceID = applicationInstanceDAO.findByName("newTestApplicationInstance").get().getApplicationInstanceID();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applicationInstances/" + applicationInstanceID + "/request/deployment"),
                HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));

        logger.info("#TC25 has been finished successfully!");

    }

    // #TC09 - Create Runtime Policy
    @Test
    @Transactional
    @Rollback(true)
    public void tc252909() throws IOException, JSONException {

        ApplicationInstance testApplicationInstance = applicationInstanceDAO.findByName("newTestApplicationInstance").get();
        applicationInstanceID = testApplicationInstance.getApplicationInstanceID();

        logger.info("#TC09 - Create Runtime Policy");

        RuntimePolicy testRuntimePolicy = new RuntimePolicy();
        testRuntimePolicy.setName("TestPolicy");
        testRuntimePolicy.setDateCreated(new Date());
        testRuntimePolicy.setLastModified(new Date());
        testRuntimePolicy.setPolicyPeriod(10 + "");
        testRuntimePolicy.setInertiaPeriod(1 + "");

        RuntimePolicyAction runtimePolicyAction = new RuntimePolicyAction();
        runtimePolicyAction.setComponentName("customApp");
        runtimePolicyAction.setType(RuntimePolicyAction.ActionType.info.name());
        runtimePolicyAction.setContext("hello");

        testRuntimePolicy.setActions(Arrays.asList(runtimePolicyAction));

        RuntimePolicyExpression runtimePolicyExpression = new RuntimePolicyExpression();
        runtimePolicyExpression.setComponentNodeName("customApp");
        runtimePolicyExpression.setFunction(RuntimePolicyExpression.FunctionType.NONE.name());
        runtimePolicyExpression.setMetric("testMetric");
        runtimePolicyExpression.setDimension("testDimension");
        runtimePolicyExpression.setOperand(RuntimePolicyExpression.OperandType.GREATER.name());
        runtimePolicyExpression.setThreshold("2");

        testRuntimePolicy.setExpressions(Arrays.asList(runtimePolicyExpression));


        HttpEntity<RuntimePolicy> entity = new HttpEntity<>(testRuntimePolicy, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applicationInstances/" + applicationInstanceID + "/runtimePolicies/elasticity"),
                HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.ACCEPTED));

        logger.info("#TC09 has been finished successfully!");

    }

    // #TC10 - Fetch Runtime Policy
    @Test
    @Transactional
    @Rollback(true)
    public void tc252910() throws JSONException {

        logger.info("#TC10 - Fetch Runtime Policy");

        ApplicationInstance testApplicationInstance = applicationInstanceDAO.findByName("newTestApplicationInstance").get();
        applicationInstanceID = testApplicationInstance.getApplicationInstanceID();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        if (null != sliceID) {

            ResponseEntity<String> response = restTemplate.exchange(
                    createURLWithPort("/api/v2/applicationInstances/" + applicationInstanceID + "/runtimePolicies/elasticity"),
                    HttpMethod.GET, entity, String.class);

            assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));

        }

        logger.info("#TC10 has been finished successfully!");

    }

    // #TC12 - Delete Runtime Policy
    @Test
    @Transactional
    @Rollback(true)
    public void tc252912() throws IOException, JSONException {

        ApplicationInstance testApplicationInstance = applicationInstanceDAO.findByName("newTestApplicationInstance").get();
        applicationInstanceID = testApplicationInstance.getApplicationInstanceID();

        runtimePolicyID = runtimePolicyDAO.findByName("TestPolicy").get().getId();

        logger.info("#TC12 - Delete Runtime Policy with ID " + runtimePolicyID);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applicationInstances/" + applicationInstanceID +"/runtimePolicies/elasticity/" + runtimePolicyID),
                HttpMethod.DELETE, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.ACCEPTED));

        logger.info("#TC12 has been finished successfully!");

    }

    // #TC27 - Un-deploy Application Graph Instance
    // #TC31 - Terminate Slice
    @Test
    @Transactional
    @Rollback(true)
    public void tc2731() throws JSONException {

        logger.info("#TC27 - Un-deploy Application Graph Instance");

        applicationInstanceID = applicationInstanceDAO.findByName("newTestApplicationInstance").get().getApplicationInstanceID();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applicationInstances/" + applicationInstanceID + "/request/undeployment"),
                HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));

        logger.info("#TC27 has been finished successfully!");

    }

    // #TC20 - Delete Application Graph Instance
    @Test
    @Transactional
    @Rollback(true)
    public void tc900() throws IOException, JSONException {

        ApplicationInstance testApplicationInstance = applicationInstanceDAO.findByName("newTestApplicationInstance").get();
        applicationInstanceID = testApplicationInstance.getApplicationInstanceID();

        logger.info("#TC20 - Delete Application Graph Instance with ID " + applicationInstanceID);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v2/applicationInstances/" + applicationInstanceID),
                HttpMethod.DELETE, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));

        logger.info("#TC20 has been finished successfully!");

    }

    // #TC11 - Update Runtime Policy
    // TODO NOT IMPLEMENTED YET

    /////////////////////////////////////////////////////////////////

    // #TC23 - Update Slice Intent
    // TODO NOT IMPLEMENTED YET

    // #TC26 - Modify Runtime Policy
    // TODO NOT IMPLEMENTED YET

    // #TC30 - Modify Slice
    // TODO NOT IMPLEMENTED YET

    /////////////////////////////////////////////////////////////////

    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);

    HttpHeaders headers = new HttpHeaders();

    private String token;

    @Autowired
    ComponentDAO componentDAO;

    @Autowired
    ApplicationDAO applicationDAO;

    @Autowired
    ProviderDAO providerDAO;

    @Autowired
    ProviderTypeDAO providerTypeDAO;

    @Autowired
    ApplicationInstanceDAO applicationInstanceDAO;

    @Autowired
    RuntimePolicyDAO runtimePolicyDAO;

    private Long componentID;
    private Long applicationID;
    private Long providerID;
    private Long applicationInstanceID;
    private Long sliceID;
    private Long runtimePolicyID;

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

}
