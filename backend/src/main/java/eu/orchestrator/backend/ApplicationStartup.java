package eu.orchestrator.backend;

import eu.orchestrator.backend.util.Util;
import eu.orchestrator.elasticity.adapter.traefikLB.TraefikLoadBalancerBackendAdapter;
import eu.orchestrator.elasticity.adapter.traefikLambdaProxy.TraefikLambdaProxyBackendAdapter;
import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkBackend;
import eu.orchestrator.repository.dao.ApplicationDAO;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ComponentDAO;
import eu.orchestrator.repository.dao.ComponentNodeDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceHashDAO;
import eu.orchestrator.repository.dao.CountryDAO;
import eu.orchestrator.repository.dao.EnvironmentalVariableDAO;
import eu.orchestrator.repository.dao.GraphLinkDAO;
import eu.orchestrator.repository.dao.GraphLinkNodeDAO;
import eu.orchestrator.repository.dao.HealthCheckDAO;
import eu.orchestrator.repository.dao.IDRuleDAO;
import eu.orchestrator.repository.dao.IDRuleSetDAO;
import eu.orchestrator.repository.dao.InterfaceDAO;
import eu.orchestrator.repository.dao.LabelDAO;
import eu.orchestrator.repository.dao.MetricDAO;
import eu.orchestrator.repository.dao.OrganizationDAO;
import eu.orchestrator.repository.dao.PluginDAO;
import eu.orchestrator.repository.dao.ProviderDAO;
import eu.orchestrator.repository.dao.ProviderTypeDAO;
import eu.orchestrator.repository.dao.QIDAO;
import eu.orchestrator.repository.dao.RadioServiceTypeDAO;
import eu.orchestrator.repository.dao.RegionDAO;
import eu.orchestrator.repository.dao.RequirementDAO;
import eu.orchestrator.repository.dao.RuntimePolicyDAO;
import eu.orchestrator.repository.dao.SSHKeyDAO;
import eu.orchestrator.repository.dao.SocPolicyDAO;
import eu.orchestrator.repository.dao.SocPolicyDroolsExpressionDAO;
import eu.orchestrator.repository.dao.SocPolicyInputKafkaStreamDAO;
import eu.orchestrator.repository.dao.SocPolicyInputKafkaStreamFieldModelDAO;
import eu.orchestrator.repository.dao.SocPolicyKafkaExpressionDAO;
import eu.orchestrator.repository.dao.SocPolicyOutputDroolsActionDAO;
import eu.orchestrator.repository.dao.UserDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Component.Architecture;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstanceHash;
import eu.orchestrator.repository.domain.ComponentNodeInstanceHash.HashType;
import eu.orchestrator.repository.domain.Country;
import eu.orchestrator.repository.domain.EnvironmentalVariable;
import eu.orchestrator.repository.domain.GraphLink;
import eu.orchestrator.repository.domain.GraphLinkNode;
import eu.orchestrator.repository.domain.HealthCheck;
import eu.orchestrator.repository.domain.IDRule;
import eu.orchestrator.repository.domain.IDRuleSet;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.repository.domain.Interface.InterfaceType;
import eu.orchestrator.repository.domain.Label;
import eu.orchestrator.repository.domain.Metric;
import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.Plugin;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.ProviderType.ProviderName;
import eu.orchestrator.repository.domain.QI;
import eu.orchestrator.repository.domain.RadioServiceType;
import eu.orchestrator.repository.domain.Region;
import eu.orchestrator.repository.domain.Requirement;
import eu.orchestrator.repository.domain.SSHKey;
import eu.orchestrator.repository.domain.SocPolicy;
import eu.orchestrator.repository.domain.SocPolicyDroolsExpression;
import eu.orchestrator.repository.domain.SocPolicyInputKafkaStream;
import eu.orchestrator.repository.domain.SocPolicyInputKafkaStreamFieldModel;
import eu.orchestrator.repository.domain.SocPolicyKafkaExpression;
import eu.orchestrator.repository.domain.SocPolicyOutputDroolsAction;
import eu.orchestrator.repository.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Profile("!dev")
@Component
@Transactional
@SuppressWarnings("Duplicates")
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    static final Logger logger = Logger.getLogger(ApplicationStartup.class.getName());
    private static final String GR_ATHENS = "gr-athens";
    private static final String PROVIDER = "provider";
    private static final String DEFAULT = "default";
    private static final String ENDPOINT = "";  //TODO set the OpenStack keystone endpoint
    private static final String MAESTRO = "maestro";
    private static final String MAESTRO_PASS = "!maestro!";
    private static final String IMAGE_ID_1 = "a1fa20be-cb69-4e58-9845-54aabecf616d";
    private static final String IMAGE_ID_2 = "235783dd-fdaa-46a6-9ea7-68cb3429556b";
    private static final String MAESTRO_DOCKER_REGISTRY = "";
    private static final String MAESTRO_DOCKER_PASS = "!maestro!$";
    private static final String ADMIN_LOWER = "admin";
    private static final String ADMIN_CAMEL = "Admin";
    private static final String MESSAGE = "message";
    private static final String PYTHON_D = "python.d";
    private static final String MYSQL = "mysql";
    private static final String MATILDA = "matilda";
    private static final String MARIADB_CONST = "MariaDB";
    private static final String MONGODB_CONST = "mongodb";
    private static final String WORDPRESS_CONST = "wordpress";
    private static final String PORT = "27017";
    private static final String LOCALHOST = "http://localhost:80";
    private static final String LOCALHOST_5678 = "http://localhost:5678";
    private static final String HORIZONTAL = "HORIZONTAL";
    private static final String LAMBDA_FUNCTION = "LAMBDA_FUNCTION";
    private static final String SERVER_PORT = "SERVER_PORT";
    private static final String EXAMPLE_URL = "http://www.example.com";
    @Autowired
    UserDAO userDAO;
    @Autowired
    CountryDAO countryDAO;
    @Autowired
    OrganizationDAO organizationDAO;
    @Autowired
    ProviderTypeDAO providerTypeDAO;
    @Autowired
    QIDAO qiDAO;
    @Autowired
    RadioServiceTypeDAO radioServiceTypeDAO;
    @Autowired
    PluginDAO pluginDAO;
    @Autowired
    MetricDAO metricDAO;
    @Autowired
    IDRuleSetDAO idRuleSetDAO;
    @Autowired
    IDRuleDAO idRuleDAO;
    @Autowired
    ProviderDAO providerDAO;
    @Autowired
    SSHKeyDAO sshKeyDAO;
    @Autowired
    ComponentDAO componentDAO;
    @Autowired
    ComponentNodeInstanceDAO componentNodeInstanceDAO;
    @Autowired
    LabelDAO labelDAO;
    @Autowired
    RegionDAO regionDAO;
    @Autowired
    EnvironmentalVariableDAO environmentalVariableDAO;
    @Autowired
    InterfaceDAO interfaceDAO;
    @Autowired
    GraphLinkDAO graphLinkDAO;
    @Autowired
    ApplicationDAO applicationDAO;
    @Autowired
    ApplicationInstanceDAO applicationInstanceDAO;
    @Autowired
    RuntimePolicyDAO runtimePolicyDAO;
    @Autowired
    ComponentNodeDAO componentNodeDAO;
    @Autowired
    GraphLinkNodeDAO graphLinkNodeDAO;
    @Autowired
    RequirementDAO requirementDAO;
    @Autowired
    HealthCheckDAO healthCheckDAO;
    @Autowired
    ResourceLoader resourceLoader;
    @Autowired
    SocPolicyDAO socPolicyDAO;
    @Autowired
    ComponentNodeInstanceHashDAO componentNodeInstanceHashDAO;
    @Autowired
    SocPolicyInputKafkaStreamDAO socPolicyInputKafkaStreamDAO;
    @Autowired
    SocPolicyInputKafkaStreamFieldModelDAO socPolicyInputKafkaStreamFieldModelDAO;
    @Autowired
    SocPolicyKafkaExpressionDAO socPolicyKafkaExpressionDAO;
    @Autowired
    SocPolicyDroolsExpressionDAO socPolicyDroolsExpressionDAO;
    @Autowired
    SocPolicyOutputDroolsActionDAO socPolicyOutputDroolsActionDAO;
    @Autowired
    TraefikLoadBalancerBackendAdapter traefikLoadBalancer;
    @Autowired
    TraefikLambdaProxyBackendAdapter traefikLambdaProxy;
    @PersistenceContext
    EntityManager entityManager;
    @jakarta.annotation.Resource(name = "elasticityFrameworkAdapters")
    List<ElasticityFrameworkBackend> elasticityFrameworkAdapters;
    // Variables
    Organization organizationAdmin;
    User admin;
    List<Plugin> listOfDefaultPlugins;
    Plugin agentPlugin;
    Plugin squidPlugin;
    Plugin customMetricPlugin;
    Region regionEuWest;
    Region regionEuEast;
    Region regionUsWest;
    Region regionUsEast;
    ProviderType userDefinedProviderType;
    ProviderType policyDefinedProviderType;
    ProviderType gccProviderType;
    ProviderType awsProviderType;
    ProviderType the5gProviderType;
    ProviderType openstackProviderType;
    ProviderType kubernetesProviderType;
    ProviderType iotGatewayProviderType;
    ProviderType rainbowKubernetesProviderType;
    ProviderType _5gOssKubernetesProviderType;
    Provider policyDefinedProvider;
    Provider userDefinedProvider;
    Provider the5gTelcoProvider;
    Provider iotGatewayProvider;
    Provider awsProvider;
    Provider ubiDell;
    Provider ubiDellAstrid;
    Provider ubiDellSpider;
    Provider ubiDellUnicorn;
    Provider ubiDellMatilda;
    Provider ubiTestProvider;
    Provider rainbowKubernetesProvider;
    Label labelLB;
    Label labelSQLDB;
    Label labelNoSQL;
    Label labelDBManagement;
    Label labelCM;
    Label labelLambdaFunction;
    Label labelARMLambdaFunction;
    Label labelCustomComponent;
    Label labelCustomARMComponent;
    Label labelFunction;
    Label labelEcho;
    Interface interfaceSQL;
    Interface interfaceSQLppdr;
    Interface interfaceNoSQL;
    Interface interfaceHttp;
    Interface interfaceSambaA;
    Interface interfaceSambaB;
    Interface interfacePhpDashboard;
    Interface interfaceNoSQLOlistic;
    Interface interfaceSQLOlistic;
    Interface interfaceOpenVasAOlistic;
    Interface interfaceOpenVasBOlistic;
    Interface interfaceAppOlistic;
    Interface interfaceSumFunction;
    Interface interfaceDivisionFunction;
    Interface interfaceMulFunction;
    Interface interfaceMainAppFunction;
    Interface interfaceHttpEcho;
    GraphLink phpMyAdminGraphLink;
    GraphLink wordPressGraphLink;
    GraphLink phpDashboardGraphLinkSQLInterface;
    GraphLink phpDashboardGraphLinkSambaInterface;
    GraphLink olisticAppGraphLinkSQLInerface;
    GraphLink olisticAppGraphLinkNoSQLInerface;
    GraphLink olisticAppGraphLinkOpenvasInerface;
    GraphLink mainAppGraphLinkSumFunctionInterface;
    GraphLink mainAppGraphLinkMulFunctionInterface;
    GraphLink mainAppGraphLinkDivFunctionInterface;
    eu.orchestrator.repository.domain.Component loadBalancer;
    eu.orchestrator.repository.domain.Component lambdaProxy;
    eu.orchestrator.repository.domain.Component mariaDB;
    eu.orchestrator.repository.domain.Component mongoDB;
    eu.orchestrator.repository.domain.Component phpMyAdmin;
    eu.orchestrator.repository.domain.Component wordPress;
    eu.orchestrator.repository.domain.Component ppdrSamba;
    eu.orchestrator.repository.domain.Component ppdrDatabase;
    eu.orchestrator.repository.domain.Component ppdrPhpDashboard;
    eu.orchestrator.repository.domain.Component olisticMysql;
    eu.orchestrator.repository.domain.Component olisticMongo;
    eu.orchestrator.repository.domain.Component olisticOpenvas;
    eu.orchestrator.repository.domain.Component olisticApp;
    eu.orchestrator.repository.domain.Component sumFunction;
    eu.orchestrator.repository.domain.Component divFunction;
    eu.orchestrator.repository.domain.Component mulFunction;
    eu.orchestrator.repository.domain.Component mainAppFunction;
    eu.orchestrator.repository.domain.Component httpEcho;
    eu.orchestrator.repository.domain.Application dbms;
    eu.orchestrator.repository.domain.Application cms;
    eu.orchestrator.repository.domain.Application ppdrApplication;
    eu.orchestrator.repository.domain.Application olisticApplication;
    eu.orchestrator.repository.domain.Application functinPilotApplication;
    @Value("${token.signer.secret}")
    private String tokenSecret;
    @Value("${token.signer.secret}")
    private String secretToken;
    @Value("${initialization.applications.ppdr}")
    private Boolean initializationApplicationPpdr;
    @Value("${initialization.applications.olistic}")
    private Boolean initializationApplicationOlistic;
    @Value("${initialization.provider.telcoProvider}")
    private Boolean initializationProviderTelcoProvider;
    @Value("${initialization.provider.iotGatewayProvider}")
    private Boolean initializationProviderIotGatewayProvider;
    @Value("${initialization.provider.awsProvider}")
    private Boolean initializationProviderAwsProvider;
    @Value("${initialization.provider.ubiDell}")
    private Boolean initializationProviderUbiDell;
    @Value("${initialization.provider.ubiDellAstrid}")
    private Boolean initializationProviderUbiDellAstrid;
    @Value("${initialization.provider.ubiDellSpider}")
    private Boolean initializationProviderUbiDellSpider;
    @Value("${initialization.provider.ubiDellUnicorn}")
    private Boolean initializationProviderUbiDellUnicorn;
    @Value("${initialization.provider.ubiDellMatilda}")
    private Boolean initializationProviderUbiDellMatilda;
    @Value("${initialization.provider.ubiTestProvider}")
    private Boolean initializationProviderUbiTestProvider;
    @Value("${initialization.provider.rainbowKubernetesProvider}")
    private Boolean initializationRainbowKubernetesProvider;

    // Seed data for the demo users/plugins. All optional: leave unset to seed nothing sensitive.
    @Value("${initialization.seed.user-password-hash:}")
    private String seedUserPasswordHash;
    @Value("${ui.server.url:}")
    private String uiServerUrl;
    @Value("${initialization.seed.netdata-plugin-base-url:}")
    private String netdataPluginBaseUrl;

    @Value("${kubernetes.rainbow.endpoint:}")
    private String rainbowEndpoint;
    @Value("${kubernetes.rainbow.ca-certificate:}")
    private String rainbowCaCertificate;
    @Value("${kubernetes.rainbow.client-certificate:}")
    private String rainbowClientCertificate;
    @Value("${kubernetes.rainbow.client-key:}")
    private String rainbowClientKey;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

        // Check if initialization is needed
        if (!userDAO.findByUsername(ADMIN_LOWER).isPresent()) {

            logger.info("Initialization process has started...");

            // Initialize Countries
            initializeCountries();

            // Initialize Provider Types
            initializeProviderTypes();

            // Initialize QIs
            initializeQualityIdentifiers();

            // Initialize Radio Service Types
            initializeRadioServiceTypes();

            // Initialize organization
            initializeOrganization();

            // Initialize Users
            initializeUsers();

            // Initialize Plugins
            initializePlugins();

            // Initialize Python Plugins SQL MongoDB
            initializePythonPlugins();

            // Initialize Plugins
            initializeCustomPlugins();

            listOfDefaultPlugins = pluginDAO.findAllByDefaultPlugin(true);

            // Initialize Plugins
            initializeIDRuleSets();

            // Initialize Providers
            initializeProviders();

            // Initialize SSH Keys
            initializeSSHKeys();

            // Initialize Labels
            initializeLabels();

            // Initialize Components
            initializeComponents();

            // Initialize Applications
            initializeApplications();

            // Init hashes
            inintializeHashes();

            if (Boolean.TRUE.equals(initializationApplicationPpdr)) {
                //Initialize PPDR pilot
                initializePilotPPDR();
            }

            if (Boolean.TRUE.equals(initializationApplicationOlistic)) {
                //Initialize olistic pilot
                initializePilotOlistic();
            }

            initializePilotFunction();
            logger.info("Initialization process has been completed successfully!");

        } else {

            logger.info("Initialization process has been already completed!");

        }

        //init soc-rule
        //store();

    }

    private void store() {

        logger.info("onnn1");
        Optional<ApplicationInstance> applicationInstance = applicationInstanceDAO.findByHexID("FacSQc8uZ2");

        if (applicationInstance.isPresent()) {

            SocPolicy socPolicy = new SocPolicy();
            socPolicy.setHexID(Util.createRandomHEXString());
            socPolicy.setName("example1");
            socPolicy.setDroolsInertiaPeriodInSecond("10");
            socPolicy.setDateCreated(new Date());
            socPolicy.setLastModified(new Date());
            Optional<User> optionalUser = userDAO.findById(1L);
            if (optionalUser.isPresent()) {
                socPolicy.setUser(optionalUser.get());
            }
            socPolicy.setApplicationInstance(applicationInstance.get());

            socPolicy = socPolicyDAO.save(socPolicy);

            // Set the Input Kafka Steam
            SocPolicyInputKafkaStream socPolicyInputKafkaStream = new SocPolicyInputKafkaStream();
            socPolicyInputKafkaStream.setSocPolicy(socPolicy);
            socPolicyInputKafkaStream.setInputTopic("wazuh-alerts");
            socPolicyInputKafkaStream.setDateCreated(new Date());
            socPolicyInputKafkaStream.setLastModified(new Date());

            socPolicyInputKafkaStream = socPolicyInputKafkaStreamDAO.save(socPolicyInputKafkaStream);

            SocPolicyInputKafkaStreamFieldModel socPolicyInputKafkaStreamFieldModel = new SocPolicyInputKafkaStreamFieldModel();
            socPolicyInputKafkaStreamFieldModel.setDateCreated(new Date());
            socPolicyInputKafkaStreamFieldModel.setLastModified(new Date());
            socPolicyInputKafkaStreamFieldModel.setType(MESSAGE);
            socPolicyInputKafkaStreamFieldModel.setName("String");
            socPolicyInputKafkaStreamFieldModel.setSocPolicyInputKafkaStream(socPolicyInputKafkaStream);
            socPolicyInputKafkaStreamFieldModelDAO.save(socPolicyInputKafkaStreamFieldModel);

            socPolicyInputKafkaStreamFieldModel = new SocPolicyInputKafkaStreamFieldModel();
            socPolicyInputKafkaStreamFieldModel.setDateCreated(new Date());
            socPolicyInputKafkaStreamFieldModel.setLastModified(new Date());
            socPolicyInputKafkaStreamFieldModel.setName("service");
            socPolicyInputKafkaStreamFieldModel.setType("STRING");
            socPolicyInputKafkaStreamFieldModel.setSocPolicyInputKafkaStream(socPolicyInputKafkaStream);
            socPolicyInputKafkaStreamFieldModelDAO.save(socPolicyInputKafkaStreamFieldModel);

            // Set the Kafka expression
            SocPolicyKafkaExpression socPolicyKafkaExpression = new SocPolicyKafkaExpression();
            socPolicyKafkaExpression.setInputTopic("wazuh-alerts");
            socPolicyKafkaExpression.setFieldName(MESSAGE);
            socPolicyKafkaExpression.setOperand("LIKE");
            socPolicyKafkaExpression.setContext("'%sshd%'");
            socPolicyKafkaExpression.setSocPolicy(socPolicy);
            socPolicyKafkaExpression.setDateCreated(new Date());
            socPolicyKafkaExpression.setLastModified(new Date());
            socPolicyKafkaExpressionDAO.save(socPolicyKafkaExpression);

            socPolicyKafkaExpression.setLogical("and");
            socPolicyKafkaExpression.setSocPolicy(socPolicy);
            socPolicyKafkaExpression.setDateCreated(new Date());
            socPolicyKafkaExpression.setLastModified(new Date());
            socPolicyKafkaExpressionDAO.save(socPolicyKafkaExpression);

            socPolicyKafkaExpression.setFieldName(MESSAGE);
            socPolicyKafkaExpression.setOperand("LIKE");
            socPolicyKafkaExpression.setContext("'%sshd%'");
            socPolicyKafkaExpression.setSocPolicy(socPolicy);
            socPolicyKafkaExpression.setDateCreated(new Date());
            socPolicyKafkaExpression.setLastModified(new Date());
            socPolicyKafkaExpressionDAO.save(socPolicyKafkaExpression);

            //Set drools expression
            SocPolicyDroolsExpression socPolicyDroolsExpression = new SocPolicyDroolsExpression();
            socPolicyDroolsExpression.setSocPolicy(socPolicy);
            socPolicyDroolsExpression.setDateCreated(new Date());
            socPolicyDroolsExpression.setLastModified(new Date());
            socPolicyDroolsExpression.setApplicationHexID("");
            socPolicyDroolsExpression.setApplicationInstanceHexID("");
            socPolicyDroolsExpression.setComponentNodeHexID("");
            socPolicyDroolsExpression.setComponentNodeInstanceHexID("");
            socPolicyDroolsExpression.setUrl(uiServerUrl + "/api/v1/auth/login");
            socPolicyDroolsExpression.setType("REST CALL");
            socPolicyDroolsExpression.setContext("test");
            socPolicyDroolsExpression.setRestMethod("POST");
            socPolicyDroolsExpressionDAO.save(socPolicyDroolsExpression);

            //Set output Drools actions
            SocPolicyOutputDroolsAction socPolicyOutputDroolsAction = new SocPolicyOutputDroolsAction();
            socPolicyOutputDroolsAction.setSocPolicy(socPolicy);
            socPolicyOutputDroolsAction.setDateCreated(new Date());
            socPolicyOutputDroolsAction.setLastModified(new Date());
            socPolicyOutputDroolsAction.setApplicationHexID("");
            socPolicyOutputDroolsAction.setApplicationInstanceHexID("");
            socPolicyOutputDroolsAction.setComponentNodeHexID("");
            socPolicyOutputDroolsAction.setComponentNodeInstanceHexID("");
            socPolicyOutputDroolsAction.setUrl(uiServerUrl + "/api/v1/auth/login");
            socPolicyOutputDroolsAction.setType("REST CALL");
            socPolicyOutputDroolsAction.setContext("test");
            socPolicyOutputDroolsAction.setRestMethod("POST");
            socPolicyOutputDroolsActionDAO.save(socPolicyOutputDroolsAction);
        }
    }

    private void initializeCountries() {

        logger.info("Countries initialization has started...");
        Resource resCountries = resourceLoader.getResource("classpath:countries.csv");
        try (InputStreamReader in = new InputStreamReader(resCountries.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(in)) {
            String line = "";
            String cvsSplitBy = ";";

            int counter = 0;

            while ((line = br.readLine()) != null) {
                if (counter > 0) {

                    String[] row = line.split(cvsSplitBy);
                    Country country = new Country();
                    country.setName(row[0]);
                    country.setAlpha2(row[1]);
                    country.setAlpha3(row[2]);
                    country.setIso3166(row[3]);
                    country.setDateCreated(new Date());
                    countryDAO.save(country);

                }
                counter++;
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        logger.info("Countries have been added successfully!");
    }

    private void initializeProviderTypes() {

        logger.info("Provider types initialization has started...");

        // User-defined
        userDefinedProviderType = new ProviderType();
        userDefinedProviderType.setName(ProviderType.ProviderName.USER_DEFINED.name());
        userDefinedProviderType.setFriendlyName(ProviderType.ProviderName.USER_DEFINED.getFriendlyName());
        userDefinedProviderType.setAdapterImplementation(null);
        userDefinedProviderType.setDateCreated(new Date());
        userDefinedProviderType.setLastModified(new Date());
        userDefinedProviderType.setEnabled(true);
        providerTypeDAO.save(userDefinedProviderType);

        // Policy-defined
        policyDefinedProviderType = new ProviderType();
        policyDefinedProviderType.setName(ProviderType.ProviderName.POLICY_DEFINED.name());
        policyDefinedProviderType.setFriendlyName(ProviderType.ProviderName.POLICY_DEFINED.getFriendlyName());
        policyDefinedProviderType.setAdapterImplementation(null);
        policyDefinedProviderType.setDateCreated(new Date());
        policyDefinedProviderType.setLastModified(new Date());
        policyDefinedProviderType.setEnabled(true);
        providerTypeDAO.save(policyDefinedProviderType);

        // GCC
        gccProviderType = new ProviderType();
        gccProviderType.setName(ProviderType.ProviderName.GCC.name());
        gccProviderType.setFriendlyName(ProviderType.ProviderName.GCC.getFriendlyName());
        gccProviderType.setAdapterImplementation("eu.orchestrator.adapter.gcc.GCCAdapter");
        gccProviderType.setDateCreated(new Date());
        gccProviderType.setLastModified(new Date());
        gccProviderType.setEnabled(true);
        providerTypeDAO.save(gccProviderType);

        // AWS
        awsProviderType = new ProviderType();
        awsProviderType.setName(ProviderType.ProviderName.AWS.name());
        awsProviderType.setFriendlyName(ProviderType.ProviderName.AWS.getFriendlyName());
        awsProviderType.setAdapterImplementation("eu.orchestrator.adapter.amazon.AmazonAdapter");
        awsProviderType.setDateCreated(new Date());
        awsProviderType.setLastModified(new Date());
        awsProviderType.setEnabled(true);
        providerTypeDAO.save(awsProviderType);

        // OpenStack
        openstackProviderType = new ProviderType();
        openstackProviderType.setName(ProviderType.ProviderName.OPENSTACK.name());
        openstackProviderType.setFriendlyName(ProviderType.ProviderName.OPENSTACK.getFriendlyName());
        openstackProviderType.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
        openstackProviderType.setDateCreated(new Date());
        openstackProviderType.setLastModified(new Date());
        openstackProviderType.setEnabled(true);
        providerTypeDAO.save(openstackProviderType);

        // 5G
        the5gProviderType = new ProviderType();
        the5gProviderType.setName(ProviderType.ProviderName.FIFTH_GENERATION_TELCO_PROVIDER.name());
        the5gProviderType.setFriendlyName(ProviderType.ProviderName.FIFTH_GENERATION_TELCO_PROVIDER.getFriendlyName());
        the5gProviderType.setDateCreated(new Date());
        the5gProviderType.setLastModified(new Date());
        the5gProviderType.setEnabled(true);
        providerTypeDAO.save(the5gProviderType);

        // IoT Gateway
        iotGatewayProviderType = new ProviderType();
        iotGatewayProviderType.setName(ProviderType.ProviderName.IOT_GATEWAY.name());
        iotGatewayProviderType.setFriendlyName(ProviderType.ProviderName.IOT_GATEWAY.getFriendlyName());
        iotGatewayProviderType.setAdapterImplementation("eu.orchestrator.adapter.iot.IoTAdapter");
        iotGatewayProviderType.setDateCreated(new Date());
        iotGatewayProviderType.setLastModified(new Date());
        iotGatewayProviderType.setEnabled(true);
        providerTypeDAO.save(iotGatewayProviderType);

        // Kubernetes
        kubernetesProviderType = new ProviderType();
        kubernetesProviderType.setName(ProviderType.ProviderName.KUBERNETES.name());
        kubernetesProviderType.setFriendlyName(ProviderType.ProviderName.KUBERNETES.getFriendlyName());
        kubernetesProviderType.setAdapterImplementation(null);
        kubernetesProviderType.setDateCreated(new Date());
        kubernetesProviderType.setLastModified(new Date());
        kubernetesProviderType.setEnabled(true);
        providerTypeDAO.save(kubernetesProviderType);

        //Rainbow Kubernetes
        rainbowKubernetesProviderType = new ProviderType();
        rainbowKubernetesProviderType.setName(ProviderType.ProviderName.RAINBOW_KUBERNETES.name());
        rainbowKubernetesProviderType.setFriendlyName(ProviderType.ProviderName.RAINBOW_KUBERNETES.getFriendlyName());
        rainbowKubernetesProviderType.setDateCreated(new Date());
        rainbowKubernetesProviderType.setLastModified(new Date());
        rainbowKubernetesProviderType.setEnabled(true);
        providerTypeDAO.save(rainbowKubernetesProviderType);

        //5G OSS Kubernetes
        _5gOssKubernetesProviderType = new ProviderType();
        _5gOssKubernetesProviderType.setName(ProviderType.ProviderName.FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER.name());
        _5gOssKubernetesProviderType.setFriendlyName(ProviderType.ProviderName.FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER.getFriendlyName());
        _5gOssKubernetesProviderType.setDateCreated(new Date());
        _5gOssKubernetesProviderType.setLastModified(new Date());
        _5gOssKubernetesProviderType.setEnabled(true);
        providerTypeDAO.save(_5gOssKubernetesProviderType);

        // NextWorks (Kubernetes) provider
        final ProviderType nextWorksOssProviderType = new ProviderType();
        nextWorksOssProviderType.setName(ProviderName.NEXTWORKS_OSS.name());
        nextWorksOssProviderType.setFriendlyName(ProviderName.NEXTWORKS_OSS.getFriendlyName());
        final Date now = new Date();
        nextWorksOssProviderType.setDateCreated(now);
        nextWorksOssProviderType.setLastModified(now);
        nextWorksOssProviderType.setEnabled(true);
        providerTypeDAO.save(nextWorksOssProviderType);


        // DataCloud R-MARKET
        final ProviderType rMarketProviderType = new ProviderType();
        rMarketProviderType.setName(ProviderType.ProviderName.R_MARKET.name());
        rMarketProviderType.setFriendlyName(ProviderName.R_MARKET.getFriendlyName());
        rMarketProviderType.setAdapterImplementation(null);
        rMarketProviderType.setDateCreated(new Date());
        rMarketProviderType.setLastModified(new Date());
        rMarketProviderType.setEnabled(true);
        providerTypeDAO.save(rMarketProviderType);


        logger.info("Provider types have been added successfully!");
    }

    private void initializeQualityIdentifiers() {

        logger.info("Quality identifiers initialization has started...");

        Resource resQIs = resourceLoader.getResource("classpath:qci.csv");

        try (InputStreamReader in = new InputStreamReader(resQIs.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(in)) {

            String line = "";
            String cvsSplitBy = ";";

            int counter = 0;

            while ((line = br.readLine()) != null) {

                if (counter > 0) {

//                    logger.info("Line: " +line);

                    String[] row = line.split(cvsSplitBy);
                    QI qci = new QI();
                    qci.setQiValue(row[0]);
                    qci.setResourceType(row[1]);
                    qci.setDefaultPriorityLevel(Integer.valueOf(row[2]));
                    qci.setPacketDelayBudget(Double.valueOf(row[3]));
                    qci.setPacketErrorRate(Double.valueOf(row[4]));
                    qci.setDefaultMaximumDataBurstVolume(row[5]);
                    qci.setDefaultAveragingWindow(row[6]);
                    qci.setServices(null != row[7] ? row[7] : "N/A");
                    qci.setDateCreated(new Date());
                    qiDAO.save(qci);

                }

                counter++;

            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        logger.info("Quality identifiers have been added successfully!");

    }

    private void initializeRadioServiceTypes() {

        logger.info("Radio service types initialization has started...");

        RadioServiceType radioServiceType = new RadioServiceType();
        radioServiceType.setServiceType("eMBB");
        radioServiceType.setCharacteristics("Slice suitable for the handling of 5G enhanced Mobile Broadband");
        radioServiceType.setSstValue(1);
        radioServiceType.setDateCreated(new Date());
        radioServiceTypeDAO.save(radioServiceType);

        radioServiceType = new RadioServiceType();
        radioServiceType.setServiceType("URLLC");
        radioServiceType.setCharacteristics("Slice suitable for the handling of ultra- reliable low latency communications");
        radioServiceType.setSstValue(2);
        radioServiceType.setDateCreated(new Date());
        radioServiceTypeDAO.save(radioServiceType);

        radioServiceType = new RadioServiceType();
        radioServiceType.setServiceType("MIoT");
        radioServiceType.setCharacteristics("Slice suitable for the handling of massive IoT");
        radioServiceType.setSstValue(3);
        radioServiceType.setDateCreated(new Date());
        radioServiceTypeDAO.save(radioServiceType);

        logger.info("Radio service types have been added successfully!");
    }

    private void initializeOrganization() {

        logger.info("Organization initialization has started...");

        organizationAdmin = new Organization();
        organizationAdmin.setName("Admin_Organization");
        organizationAdmin.setStatus(Organization.Status.ACTIVE.name());
        organizationAdmin.setDateCreated(new Date());
        organizationAdmin.setLastModified(new Date());

        organizationDAO.save(organizationAdmin);

        logger.info("Organization have been added successfully!");
    }

    private void initializeUsers() {

        logger.info("Users initialization has started...");

        admin = new User();
        admin.setUsername(ADMIN_LOWER);
        admin.setPassword(seedUserPasswordHash);
        admin.setFirstName(ADMIN_CAMEL);
        admin.setLastName(ADMIN_CAMEL);
        admin.setEmail("admin@gmail.com");
        admin.setPhone("+306900000000");
        admin.setRole("ADMIN");
        admin.setDateCreated(new Date());
        admin.setNotificationEmailEnabled(true);
        admin.setNotificationWebEnabled(true);
        Optional<Country> optionalCountry = countryDAO.findByName("Greece");
        optionalCountry.ifPresent(country -> admin.setCountry(country));
        admin.setFirstLogin(false);
        admin.setEnabled(true);
        admin.setOrganization(organizationAdmin);

        userDAO.save(admin);

        logger.info("Users have been added successfully!");
    }

    private void initializePlugins() {

        logger.info("Plugins initialization has started...");

        Resource resPlugins = resourceLoader.getResource("classpath:plugins.csv");

        try (
                InputStreamReader in = new InputStreamReader(resPlugins.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(in)
        ) {

            String line = "";
            String cvsSplitBy = ",";

            Map<String, List<String>> mapOfLines = new HashMap<>();

            while ((line = br.readLine()) != null) {

                String[] cells = line.split(cvsSplitBy);

                String pluginName = cells[0];
                String moduleName = "";
                if (null != cells[1] && !cells[1].isEmpty()) {
                    moduleName = cells[1];
                }

//                String metricName = cells[2];

                if (!mapOfLines.containsKey(pluginName + "^" + moduleName)) {

                    mapOfLines.put(pluginName + "^" + moduleName, Arrays.asList(line));

                } else {

                    List<String> existingLines = mapOfLines.get(pluginName + "^" + moduleName);
                    List<String> newLines = new ArrayList<>();

                    if (null != existingLines
                            && !existingLines.isEmpty()
                            && !existingLines.contains(line)) {

                        newLines.add(line);
                        newLines.addAll(existingLines);

                        mapOfLines.remove(pluginName + "^" + moduleName);
                        mapOfLines.put(pluginName + "^" + moduleName, newLines);

                    }

                }

            }

            if (!mapOfLines.isEmpty()) {

                mapOfLines.entrySet().forEach(entry -> {

                    String key = entry.getKey();
                    String[] keyArray = key.split("\\^");
                    List<String> pluginLine = entry.getValue();

                    boolean defaultPlugin = pluginLine.get(0).split(",")[6].equals("1");
                    boolean immutablePlugin = pluginLine.get(0).split(",")[5].equals("1");

                    Plugin plugin = new Plugin();
                    plugin.setName(keyArray[0]);
                    plugin.setModuleName(keyArray.length > 1 ? keyArray[1] : null);
                    plugin.setDefaultPlugin(defaultPlugin);
                    plugin.setPublicPlugin(true);
                    plugin.setImmutablePlugin(immutablePlugin);
                    plugin.setDateCreated(new Date());
                    plugin.setLastModified(new Date());
                    plugin.setDownloadURL(null);
                    plugin.setPluginType(null);
                    plugin.setPort(null);
                    plugin.setEndpoint(null);
                    plugin.setUser(admin);
                    plugin.setOrganization(organizationAdmin);
                    pluginDAO.save(plugin);

                    if (null != pluginLine && !pluginLine.isEmpty()) {

                        pluginLine.stream().forEach(pluginL -> {

                            Metric mtrc = new Metric();
                            mtrc.setPlugin(plugin);
                            mtrc.setName(pluginL.split("\\,")[2]);
                            mtrc.setFriendlyName(pluginL.split("\\,")[3]);
                            mtrc.setUnit(pluginL.split("\\,")[4]);
                            mtrc.setLastModified(new Date());
                            mtrc.setDateCreated(new Date());
                            metricDAO.save(mtrc);

                        });

                    }

                });

            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        logger.info("Plugins have been added successfully!");

    }

    private void initializePythonPlugins() {

        logger.info("Python Plugins initialization has started...");

        Resource resPlugins = resourceLoader.getResource("classpath:mySQLPlugin.csv");

        try (InputStreamReader in = new InputStreamReader(resPlugins.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(in)) {

            String line = "";
            String cvsSplitBy = ",";

            Map<String, List<String>> mapOfLines = new HashMap<>();

            while ((line = br.readLine()) != null) {

                String[] cells = line.split(cvsSplitBy);

                String pluginName = cells[0];
                String moduleName = "";
                if (null != cells[1] && !cells[1].isEmpty()) {
                    moduleName = cells[1];
                }

//                String metricName = cells[2];

                if (!mapOfLines.containsKey(pluginName + "^" + moduleName)) {

                    mapOfLines.put(pluginName + "^" + moduleName, Arrays.asList(line));

                } else {

                    List<String> existingLines = mapOfLines.get(pluginName + "^" + moduleName);
                    List<String> newLines = new ArrayList<>();

                    if (null != existingLines
                            && !existingLines.isEmpty()
                            && !existingLines.contains(line)) {

                        newLines.add(line);
                        newLines.addAll(existingLines);

                        mapOfLines.remove(pluginName + "^" + moduleName);
                        mapOfLines.put(pluginName + "^" + moduleName, newLines);

                    }

                }

            }

            if (!mapOfLines.isEmpty()) {

                mapOfLines.entrySet().stream().forEach(entry -> {

                    List<String> pluginLine = entry.getValue();

                    boolean defaultPlugin = pluginLine.get(0).split("\\,")[6].equals("1");
                    boolean immutablePlugin = pluginLine.get(0).split("\\,")[5].equals("1");

                    Plugin plugin = new Plugin();
                    plugin.setName(PYTHON_D);
                    plugin.setModuleName(MYSQL);
                    plugin.setDefaultPlugin(defaultPlugin);
                    plugin.setPublicPlugin(true);
                    plugin.setImmutablePlugin(immutablePlugin);
                    plugin.setDateCreated(new Date());
                    plugin.setLastModified(new Date());
                    plugin.setDownloadURL(null);
                    plugin.setPluginType(Plugin.PluginType.HTTP.name());
                    plugin.setPort("3306");
                    plugin.setEndpoint(null);
                    plugin.setUser(admin);
                    plugin.setOrganization(organizationAdmin);
                    pluginDAO.save(plugin);

                    if (null != pluginLine && !pluginLine.isEmpty()) {

                        pluginLine.stream().forEach(pluginL -> {

                            Metric mtrc = new Metric();
                            mtrc.setPlugin(plugin);
                            mtrc.setName(pluginL.split("\\,")[2]);
                            mtrc.setFriendlyName(pluginL.split("\\,")[3]);
                            mtrc.setUnit(pluginL.split("\\,")[4]);
                            mtrc.setLastModified(new Date());
                            mtrc.setDateCreated(new Date());
                            metricDAO.save(mtrc);

                        });

                    }

                });

            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        logger.info("Python MySQL Plugin have been added successfully!");

        resPlugins = resourceLoader.getResource("classpath:mongoDBPlugin.csv");

        try (InputStreamReader in = new InputStreamReader(resPlugins.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(in)
        ) {

            String line = "";
            String cvsSplitBy = ",";

            Map<String, List<String>> mapOfLines = new HashMap<>();

            while ((line = br.readLine()) != null) {

                String[] cells = line.split(cvsSplitBy);

                String pluginName = cells[0];
                String moduleName = "";
                if (null != cells[1] && !cells[1].isEmpty()) {
                    moduleName = cells[1];
                }

//                String metricName = cells[2];

                if (!mapOfLines.containsKey(pluginName + "^" + moduleName)) {

                    mapOfLines.put(pluginName + "^" + moduleName, Arrays.asList(line));

                } else {

                    List<String> existingLines = mapOfLines.get(pluginName + "^" + moduleName);
                    List<String> newLines = new ArrayList<>();

                    if (null != existingLines
                            && !existingLines.isEmpty()
                            && !existingLines.contains(line)) {

                        newLines.add(line);
                        newLines.addAll(existingLines);

                        mapOfLines.remove(pluginName + "^" + moduleName);
                        mapOfLines.put(pluginName + "^" + moduleName, newLines);

                    }

                }

            }

            if (!mapOfLines.isEmpty()) {

                mapOfLines.entrySet().stream().forEach(entry -> {

                    List<String> pluginLine = entry.getValue();

                    boolean defaultPlugin = pluginLine.get(0).split("\\,")[6].equals("1");
                    boolean immutablePlugin = pluginLine.get(0).split("\\,")[5].equals("1");

                    Plugin plugin = new Plugin();
                    plugin.setName(PYTHON_D);
                    plugin.setModuleName(MONGODB_CONST);
                    plugin.setDefaultPlugin(defaultPlugin);
                    plugin.setPublicPlugin(true);
                    plugin.setImmutablePlugin(immutablePlugin);
                    plugin.setDateCreated(new Date());
                    plugin.setLastModified(new Date());
                    plugin.setDownloadURL(null);
                    plugin.setPluginType(Plugin.PluginType.HTTP.name());
                    plugin.setPort(PORT);
                    plugin.setEndpoint(null);
                    plugin.setUser(admin);
                    plugin.setOrganization(organizationAdmin);
                    pluginDAO.save(plugin);

                    if (null != pluginLine && !pluginLine.isEmpty()) {

                        pluginLine.stream().forEach(pluginL -> {

                            Metric mtrc = new Metric();
                            mtrc.setPlugin(plugin);
                            mtrc.setName(pluginL.split("\\,")[2]);
                            mtrc.setFriendlyName(pluginL.split("\\,")[3]);
                            mtrc.setUnit(pluginL.split("\\,")[4]);
                            mtrc.setLastModified(new Date());
                            mtrc.setDateCreated(new Date());
                            metricDAO.save(mtrc);

                        });

                    }

                });

            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        logger.info("Python MySQL Plugin have been added successfully!");

    }

    private void initializeCustomPlugins() {

        // Register custom module
        customMetricPlugin = new Plugin();
        customMetricPlugin.setName(PYTHON_D);
        customMetricPlugin.setModuleName("customMetricApp");
        customMetricPlugin.setDefaultPlugin(false);
        customMetricPlugin.setPublicPlugin(true);
        customMetricPlugin.setImmutablePlugin(false);
        customMetricPlugin.setDateCreated(new Date());
        customMetricPlugin.setLastModified(new Date());

        customMetricPlugin.setDownloadURL(
                netdataPluginBaseUrl + "/customMetricApp/customMetricApp.chart.py");

        customMetricPlugin.setPluginType(Plugin.PluginType.SOCKET.name());
        customMetricPlugin.setPort("9281");
        customMetricPlugin.setUser(admin);
        customMetricPlugin.setOrganization(organizationAdmin);
        pluginDAO.save(customMetricPlugin);

        // Register factorial execution collector metrics
        Metric factorialExecutionTime = new Metric();
        factorialExecutionTime.setPlugin(customMetricPlugin);
        factorialExecutionTime.setName("_factorial_execution_time_milliseconds_average");
        factorialExecutionTime.setFriendlyName("Factorial Execution time");
        factorialExecutionTime.setUnit("milliseconds");
        factorialExecutionTime.setLastModified(new Date());
        factorialExecutionTime.setDateCreated(new Date());
        metricDAO.save(factorialExecutionTime);

        // Register Squid module
        agentPlugin = new Plugin();
        agentPlugin.setName(PYTHON_D);
        agentPlugin.setModuleName("agentCollector");
        agentPlugin.setDefaultPlugin(true);
        agentPlugin.setPublicPlugin(true);
        agentPlugin.setImmutablePlugin(true);
        agentPlugin.setDateCreated(new Date());
        agentPlugin.setLastModified(new Date());

        agentPlugin
                .setDownloadURL(netdataPluginBaseUrl + "/agent/agentCollector.chart.py");

        agentPlugin.setPluginType(Plugin.PluginType.DOWNLOAD_CONF.name());
        agentPlugin.setEndpoint(netdataPluginBaseUrl + "/agent/agentCollector.conf");
        agentPlugin.setUser(admin);
        agentPlugin.setOrganization(organizationAdmin);
        pluginDAO.save(agentPlugin);

        // Register agent collector metrics
        Metric agentProfileExecution = new Metric();
        agentProfileExecution.setPlugin(agentPlugin);
        agentProfileExecution.setName("_agent_profile_execution_milliseconds_average");
        agentProfileExecution.setFriendlyName("Agent Execution time");
        agentProfileExecution.setUnit("milliseconds");
        agentProfileExecution.setLastModified(new Date());
        agentProfileExecution.setDateCreated(new Date());
        metricDAO.save(agentProfileExecution);

        // Register Squid module
        squidPlugin = new Plugin();
        squidPlugin.setName(PYTHON_D);
        squidPlugin.setModuleName("squid");
        squidPlugin.setDefaultPlugin(false);
        squidPlugin.setPublicPlugin(true);
        squidPlugin.setImmutablePlugin(false);
        squidPlugin.setDateCreated(new Date());
        squidPlugin.setLastModified(new Date());

        squidPlugin.setDownloadURL(netdataPluginBaseUrl + "/squid/squid.chart.py");

        squidPlugin.setPluginType(Plugin.PluginType.HTTP.name());
        squidPlugin.setPort("3128");
        squidPlugin.setEndpoint("localhost");
        squidPlugin.setUser(admin);
        squidPlugin.setOrganization(organizationAdmin);
        pluginDAO.save(squidPlugin);

        // Register squid metrics
        Metric squidClientsNet = new Metric();
        squidClientsNet.setPlugin(squidPlugin);
        squidClientsNet.setName("clients_net");
        squidClientsNet.setFriendlyName("Squid Client Bandwidth");
        squidClientsNet.setUnit("kilobits/s");
        squidClientsNet.setLastModified(new Date());
        squidClientsNet.setDateCreated(new Date());
        metricDAO.save(squidClientsNet);

        Metric squidClientsRequests = new Metric();
        squidClientsRequests.setPlugin(squidPlugin);
        squidClientsRequests.setName("clients_requests");
        squidClientsRequests.setFriendlyName("Squid Client Requests");
        squidClientsRequests.setUnit("requests/s");
        squidClientsRequests.setLastModified(new Date());
        squidClientsRequests.setDateCreated(new Date());
        metricDAO.save(squidClientsRequests);

        Metric squidServersNet = new Metric();
        squidServersNet.setPlugin(squidPlugin);
        squidServersNet.setName("servers_net");
        squidServersNet.setFriendlyName("Squid Server Bandwidth");
        squidServersNet.setUnit("kilobits/s");
        squidServersNet.setLastModified(new Date());
        squidServersNet.setDateCreated(new Date());
        metricDAO.save(squidServersNet);

        Metric squidServersRequests = new Metric();
        squidServersRequests.setPlugin(squidPlugin);
        squidServersRequests.setName("servers_requests");
        squidServersRequests.setFriendlyName("Squid Server Requests");
        squidServersRequests.setUnit("requests/s");
        squidServersRequests.setLastModified(new Date());
        squidServersRequests.setDateCreated(new Date());
        metricDAO.save(squidServersRequests);

    }

    private void initializeIDRuleSets() {

        // ICMP
        IDRuleSet icmpIDRuleSet = new IDRuleSet();
        icmpIDRuleSet.setName("icmp");
        icmpIDRuleSet.setDateCreated(new Date());
        icmpIDRuleSet.setLastModified(new Date());
        icmpIDRuleSet.setPredefinedIDRuleSet(true);
        icmpIDRuleSet.setPublicIDRuleSet(true);
        icmpIDRuleSet.setUser(admin);
        icmpIDRuleSet.setOrganization(organizationAdmin);
        idRuleSetDAO.save(icmpIDRuleSet);

        IDRule idRule1 = new IDRule();
        idRule1.setIdRuleSet(icmpIDRuleSet);
        idRule1.setName("alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"Pinging...\";sid:1000004;)");
        idRule1.setLastModified(new Date());
        idRule1.setDateCreated(new Date());
        idRuleDAO.save(idRule1);

        IDRule idRule2 = new IDRule();
        idRule2.setIdRuleSet(icmpIDRuleSet);
        idRule2.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP ISS Pinger\"; itype:8; content:\"ISSPNGRQ\"; depth:32; reference:arachnids,158; classtype:attempted-recon; sid:465; rev:3;)");
        idRule2.setLastModified(new Date());
        idRule2.setDateCreated(new Date());
        idRuleDAO.save(idRule2);

        IDRule idRule3 = new IDRule();
        idRule3.setIdRuleSet(icmpIDRuleSet);
        idRule3.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP L3retriever Ping\"; icode:0; itype:8; content:\"ABCDEFGHIJKLMNOPQRSTUVWABCDEFGHI\"; depth:32; reference:arachnids,311; classtype:attempted-recon; sid:466; rev:4;)");
        idRule3.setLastModified(new Date());
        idRule3.setDateCreated(new Date());
        idRuleDAO.save(idRule3);

        IDRule idRule4 = new IDRule();
        idRule4.setIdRuleSet(icmpIDRuleSet);
        idRule4.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP Nemesis v1.1 Echo\"; dsize:20; icmp_id:0; icmp_seq:0; itype:8; content:\"|00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00|\"; reference:arachnids,449; classtype:attempted-recon; sid:467; rev:3;)");
        idRule4.setLastModified(new Date());
        idRule4.setDateCreated(new Date());
        idRuleDAO.save(idRule4);

        IDRule idRule5 = new IDRule();
        idRule5.setIdRuleSet(icmpIDRuleSet);
        idRule5.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP PING NMAP\"; dsize:0; itype:8; reference:arachnids,162; classtype:attempted-recon; sid:469; rev:3;)");
        idRule5.setLastModified(new Date());
        idRule5.setDateCreated(new Date());
        idRuleDAO.save(idRule5);

        IDRule idRule6 = new IDRule();
        idRule6.setIdRuleSet(icmpIDRuleSet);
        idRule6.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP icmpenum v1.1.1\"; dsize:0; icmp_id:666 ; icmp_seq:0; id:666; itype:8; reference:arachnids,450; classtype:attempted-recon; sid:471; rev:3;)");
        idRule6.setLastModified(new Date());
        idRule6.setDateCreated(new Date());
        idRuleDAO.save(idRule6);

        IDRule idRule7 = new IDRule();
        idRule7.setIdRuleSet(icmpIDRuleSet);
        idRule7.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP redirect host\"; icode:1; itype:5; reference:arachnids,135; reference:cve,1999-0265; classtype:bad-unknown; sid:472; rev:4;)");
        idRule7.setLastModified(new Date());
        idRule7.setDateCreated(new Date());
        idRuleDAO.save(idRule7);

        IDRule idRule8 = new IDRule();
        idRule8.setIdRuleSet(icmpIDRuleSet);
        idRule8.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP redirect net\"; icode:0; itype:5; reference:arachnids,199; reference:cve,1999-0265; classtype:bad-unknown; sid:473; rev:4;)");
        idRule8.setLastModified(new Date());
        idRule8.setDateCreated(new Date());
        idRuleDAO.save(idRule8);

        IDRule idRule9 = new IDRule();
        idRule9.setIdRuleSet(icmpIDRuleSet);
        idRule9.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP superscan echo\"; dsize:8; itype:8; content:\"|00 00 00 00 00 00 00 00|\"; classtype:attempted-recon; sid:474; rev:4;)");
        idRule9.setLastModified(new Date());
        idRule9.setDateCreated(new Date());
        idRuleDAO.save(idRule9);

        IDRule idRule10 = new IDRule();
        idRule10.setIdRuleSet(icmpIDRuleSet);
        idRule10.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP traceroute ipopts\"; ipopts:rr; itype:0; reference:arachnids,238; classtype:attempted-recon; sid:475; rev:3;)");
        idRule10.setLastModified(new Date());
        idRule10.setDateCreated(new Date());
        idRuleDAO.save(idRule10);

        IDRule idRule11 = new IDRule();
        idRule11.setIdRuleSet(icmpIDRuleSet);
        idRule11.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP webtrends scanner\"; icode:0; itype:8; content:\"|00 00 00 00|EEEEEEEEEEEE\"; reference:arachnids,307; classtype:attempted-recon; sid:476; rev:4;)");
        idRule11.setLastModified(new Date());
        idRule11.setDateCreated(new Date());
        idRuleDAO.save(idRule11);

        IDRule idRule12 = new IDRule();
        idRule12.setIdRuleSet(icmpIDRuleSet);
        idRule12.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP Source Quench\"; icode:0; itype:4; classtype:bad-unknown; sid:477; rev:2;)");
        idRule12.setLastModified(new Date());
        idRule12.setDateCreated(new Date());
        idRuleDAO.save(idRule12);

        IDRule idRule13 = new IDRule();
        idRule13.setIdRuleSet(icmpIDRuleSet);
        idRule13.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP Broadscan Smurf Scanner\"; dsize:4; icmp_id:0; icmp_seq:0; itype:8; classtype:attempted-recon; sid:478; rev:3;)");
        idRule13.setLastModified(new Date());
        idRule13.setDateCreated(new Date());
        idRuleDAO.save(idRule13);

        IDRule idRule14 = new IDRule();
        idRule14.setIdRuleSet(icmpIDRuleSet);
        idRule14.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP PING speedera\"; itype:8; content:\"89|3A 3B|<=>?\"; depth:100; classtype:misc-activity; sid:480; rev:5;)");
        idRule14.setLastModified(new Date());
        idRule14.setDateCreated(new Date());
        idRuleDAO.save(idRule14);

        IDRule idRule15 = new IDRule();
        idRule15.setIdRuleSet(icmpIDRuleSet);
        idRule15.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP TJPingPro1.1Build 2 Windows\"; itype:8; content:\"TJPingPro by Jim\"; depth:32; reference:arachnids,167; classtype:misc-activity; sid:481; rev:5;)");
        idRule15.setLastModified(new Date());
        idRule15.setDateCreated(new Date());
        idRuleDAO.save(idRule15);

        IDRule idRule16 = new IDRule();
        idRule16.setIdRuleSet(icmpIDRuleSet);
        idRule16.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP PING WhatsupGold Windows\"; itype:8; content:\"WhatsUp - A Netw\"; depth:32; reference:arachnids,168; classtype:misc-activity; sid:482; rev:5;)");
        idRule16.setLastModified(new Date());
        idRule16.setDateCreated(new Date());
        idRuleDAO.save(idRule16);

        IDRule idRule17 = new IDRule();
        idRule17.setIdRuleSet(icmpIDRuleSet);
        idRule17.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP PING CyberKit 2.2 Windows\"; itype:8; content:\"|AA AA AA AA AA AA AA AA AA AA AA AA AA AA AA AA|\"; depth:32; reference:arachnids,154; classtype:misc-activity; sid:483; rev:5;)");
        idRule17.setLastModified(new Date());
        idRule17.setDateCreated(new Date());
        idRuleDAO.save(idRule17);

        IDRule idRule18 = new IDRule();
        idRule18.setIdRuleSet(icmpIDRuleSet);
        idRule18.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP PING Sniffer Pro/NetXRay network scan\"; itype:8; content:\"Cinco Network, Inc.\"; depth:32; classtype:misc-activity; sid:484; rev:4;)");
        idRule18.setLastModified(new Date());
        idRule18.setDateCreated(new Date());
        idRuleDAO.save(idRule18);

        IDRule idRule19 = new IDRule();
        idRule19.setIdRuleSet(icmpIDRuleSet);
        idRule19.setName(
                "alert icmp any any -> any any (msg:\"ICMP Destination Unreachable Communication Administratively Prohibited\"; icode:13; itype:3; classtype:misc-activity; sid:485; rev:4;)");
        idRule19.setLastModified(new Date());
        idRule19.setDateCreated(new Date());
        idRuleDAO.save(idRule19);

        IDRule idRule20 = new IDRule();
        idRule20.setIdRuleSet(icmpIDRuleSet);
        idRule20.setName(
                "alert icmp any any -> any any (msg:\"ICMP Destination Unreachable Communication with Destination Host is Administratively Prohibited\"; icode:10; itype:3; classtype:misc-activity; sid:486; rev:4;)");
        idRule20.setLastModified(new Date());
        idRule20.setDateCreated(new Date());
        idRuleDAO.save(idRule20);

        IDRule idRule21 = new IDRule();
        idRule21.setIdRuleSet(icmpIDRuleSet);
        idRule21.setName(
                "alert icmp any any -> any any (msg:\"ICMP Destination Unreachable Communication with Destination Network is Administratively Prohibited\"; icode:9; itype:3; classtype:misc-activity; sid:487; rev:4;)");
        idRule21.setLastModified(new Date());
        idRule21.setDateCreated(new Date());
        idRuleDAO.save(idRule21);

        IDRule idRule23 = new IDRule();
        idRule23.setIdRuleSet(icmpIDRuleSet);
        idRule23.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP digital island bandwidth query\"; content:\"mailto|3A|ops@digisle.com\"; depth:22; classtype:misc-activity; sid:1813; rev:5;)");
        idRule23.setLastModified(new Date());
        idRule23.setDateCreated(new Date());
        idRuleDAO.save(idRule23);

        IDRule idRule24 = new IDRule();
        idRule24.setIdRuleSet(icmpIDRuleSet);
        idRule24.setName(
                "alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"ICMP Large ICMP Packet\"; dsize:>800; reference:arachnids,246; classtype:bad-unknown; sid:499; rev:4;)");
        idRule24.setLastModified(new Date());
        idRule24.setDateCreated(new Date());
        idRuleDAO.save(idRule24);

        // MySQL
        IDRuleSet mySQLIDRuleSet = new IDRuleSet();
        mySQLIDRuleSet.setName(MYSQL);
        mySQLIDRuleSet.setDateCreated(new Date());
        mySQLIDRuleSet.setLastModified(new Date());
        mySQLIDRuleSet.setPredefinedIDRuleSet(true);
        mySQLIDRuleSet.setPublicIDRuleSet(true);
        mySQLIDRuleSet.setUser(admin);
        mySQLIDRuleSet.setOrganization(organizationAdmin);
        idRuleSetDAO.save(mySQLIDRuleSet);

        IDRule mySQLIDRule1 = new IDRule();
        mySQLIDRule1.setIdRuleSet(mySQLIDRuleSet);
        mySQLIDRule1.setName(
                "alert tcp $EXTERNAL_NET any -> $HOME_NET 3306 (msg:\"MYSQL root login attempt\"; flow:to_server,established; content:\"|0A 00 00 01 85 04 00 00 80|root|00|\"; classtype:protocol-command-decode; sid:1775; rev:2;)");
        mySQLIDRule1.setLastModified(new Date());
        mySQLIDRule1.setDateCreated(new Date());
        idRuleDAO.save(mySQLIDRule1);

        IDRule mySQLIDRule2 = new IDRule();
        mySQLIDRule2.setIdRuleSet(mySQLIDRuleSet);
        mySQLIDRule2.setName(
                "alert tcp $EXTERNAL_NET any -> $HOME_NET 3306 (msg:\"MYSQL show databases attempt\"; flow:to_server,established; content:\"|0F 00 00 00 03|show databases\"; classtype:protocol-command-decode; sid:1776; rev:2;)");
        mySQLIDRule2.setLastModified(new Date());
        mySQLIDRule2.setDateCreated(new Date());
        idRuleDAO.save(mySQLIDRule2);

        IDRule mySQLIDRule3 = new IDRule();
        mySQLIDRule3.setIdRuleSet(mySQLIDRuleSet);
        mySQLIDRule3.setName(
                "alert tcp $EXTERNAL_NET any -> $HOME_NET 3306 (msg:\"MYSQL 4.0 root login attempt\"; flow:to_server,established; content:\"|01|\"; distance:3; within:1; content:\"root|00|\"; nocase; distance:5; within:5; classtype:protocol-command-decode; sid:3456; rev:1;)");
        mySQLIDRule3.setLastModified(new Date());
        mySQLIDRule3.setDateCreated(new Date());
        idRuleDAO.save(mySQLIDRule3);

        List<IDRule> idRules = idRuleDAO.findAll();

        idRules.stream().forEach(idRule -> {

            String tempSID = idRule.getName().substring(idRule.getName().indexOf("sid:"));
            tempSID = tempSID.substring(0, tempSID.indexOf(";"));

            int sid = 1000000 + idRule.getRuleID().intValue();

            idRule.setName(idRule.getName().replace(tempSID, "sid:" + sid));
            idRuleDAO.save(idRule);

        });

    }

    private void initializeProviders() {

        logger.info("Providers initialization has started...");

        // User-defined
        Query q = entityManager.createNativeQuery(
                "INSERT INTO provider (id, name, date_created, last_modified, is_default, is_internal, enabled, provider_type) VALUES (-1, 'User-defined', now(), now(), false, true, true, "
                        + userDefinedProviderType.getId() + ");");
        q.executeUpdate();

        // Policy-defined
        q = entityManager.createNativeQuery(
                "INSERT INTO provider (id, name, date_created, last_modified, is_default, is_internal, enabled, provider_type) VALUES (-2, 'Policy-defined', now(), now(), false, true, true, "
                        + policyDefinedProviderType.getId() + ");");
        q.executeUpdate();

        if (Boolean.TRUE.equals(initializationProviderTelcoProvider)) {
            the5gTelcoProvider = new Provider();
            the5gTelcoProvider.setName("MATILDA-enabled Telco Provider");
            the5gTelcoProvider.setDateCreated(new Date());
            the5gTelcoProvider.setLastModified(new Date());
            the5gTelcoProvider.setDomain(null);
            the5gTelcoProvider.setEndpoint(null);
            the5gTelcoProvider.setProject(null);
            the5gTelcoProvider.setMeshIdentifier(null);
            the5gTelcoProvider.setDefaultProvider(false);
            the5gTelcoProvider.setEnabled(Boolean.FALSE);
            the5gTelcoProvider.setPassword(Util.encrypt("!telcoprovider!", tokenSecret));
            the5gTelcoProvider.setUsername("telcoprovider");
            the5gTelcoProvider.setUser(admin);
            the5gTelcoProvider.setOrganization(organizationAdmin);
            the5gTelcoProvider.setProviderType(the5gProviderType);
            the5gTelcoProvider.setProxy("127.0.0.1:8085");
            the5gTelcoProvider.setInternalProvider(false);
            providerDAO.save(the5gTelcoProvider);

            Region the5gTelcoProviderGR = new Region();
            the5gTelcoProviderGR.setDateCreated(new Date());
            the5gTelcoProviderGR.setLastModified(new Date());
            the5gTelcoProviderGR.setName("it-genoa");
            the5gTelcoProviderGR.setProvider(the5gTelcoProvider);
            regionDAO.save(the5gTelcoProviderGR);
        }

        if (Boolean.TRUE.equals(initializationProviderIotGatewayProvider)) {
            iotGatewayProvider = new Provider();
            iotGatewayProvider.setName("IoT Gateway Provider");
            iotGatewayProvider.setDateCreated(new Date());
            iotGatewayProvider.setLastModified(new Date());
            iotGatewayProvider.setDomain(null);
            iotGatewayProvider.setEndpoint("http://[fc2e:8be9:dd43:054b:33c1:a0a8:2da1:6e5f]:8080/api/v1/");
            iotGatewayProvider.setProject(null);
            iotGatewayProvider.setDefaultProvider(false);
            iotGatewayProvider.setEnabled(Boolean.FALSE);
            iotGatewayProvider.setPassword(Util.encrypt("!admin!", tokenSecret));
            iotGatewayProvider.setUsername(ADMIN_LOWER);
            iotGatewayProvider.setMeshIdentifier(UUID.randomUUID().toString());
            iotGatewayProvider.setUser(admin);
            iotGatewayProvider.setOrganization(organizationAdmin);
            iotGatewayProvider.setProviderType(iotGatewayProviderType);
            iotGatewayProvider.setInternalProvider(false);
            providerDAO.save(iotGatewayProvider);

            Region iotGatewayProviderGR = new Region();
            iotGatewayProviderGR.setDateCreated(new Date());
            iotGatewayProviderGR.setLastModified(new Date());
            iotGatewayProviderGR.setName(GR_ATHENS);
            iotGatewayProviderGR.setProvider(iotGatewayProvider);
            regionDAO.save(iotGatewayProviderGR);
        }

        if (Boolean.TRUE.equals(initializationProviderAwsProvider)) {
            awsProvider = new Provider();
            awsProvider.setName("Amazon AWS Provider");
            awsProvider.setDateCreated(new Date());
            awsProvider.setLastModified(new Date());
            awsProvider.setDomain("sa-east-1");
            awsProvider.setEndpoint(null);
            awsProvider.setProject(null);
            awsProvider.setDefaultProvider(false);
            awsProvider.setEnabled(Boolean.FALSE);
            awsProvider.setPassword(null);
            awsProvider.setUsername(null);
            awsProvider.setPublicKey("defsafcsaece");
            awsProvider.setPrivateKey("");
            awsProvider.setMeshIdentifier(UUID.randomUUID().toString());
            awsProvider.setUser(admin);
            awsProvider.setOrganization(organizationAdmin);
            awsProvider.setImageID("fsecsacsae");
            awsProvider.setProviderType(awsProviderType);
            awsProvider.setInternalProvider(false);
            providerDAO.save(awsProvider);

            regionEuWest = new Region();
            regionEuWest.setDateCreated(new Date());
            regionEuWest.setLastModified(new Date());
            regionEuWest.setName("eu-west");
            regionEuWest.setProvider(awsProvider);
            regionDAO.save(regionEuWest);

            regionEuEast = new Region();
            regionEuEast.setDateCreated(new Date());
            regionEuEast.setLastModified(new Date());
            regionEuEast.setName("eu-east");
            regionEuEast.setProvider(awsProvider);
            regionDAO.save(regionEuEast);

            regionUsWest = new Region();
            regionUsWest.setDateCreated(new Date());
            regionUsWest.setLastModified(new Date());
            regionUsWest.setName("us-west");
            regionUsWest.setProvider(awsProvider);
            regionDAO.save(regionUsWest);

            regionUsEast = new Region();
            regionUsEast.setDateCreated(new Date());
            regionUsEast.setLastModified(new Date());
            regionUsEast.setName("us-east");
            regionUsEast.setProvider(awsProvider);
            regionDAO.save(regionUsEast);
        }

        if (Boolean.TRUE.equals(initializationProviderUbiDell)) {
            ubiDell = new Provider();
            ubiDell.setName("UBIDELL");
            ubiDell.setDateCreated(new Date());
            ubiDell.setLastModified(new Date());
            ubiDell.setDomain(DEFAULT);
            ubiDell.setEndpoint(ENDPOINT);
            ubiDell.setProject(MAESTRO);
            ubiDell.setDefaultProvider(true);
            ubiDell.setMeshIdentifier(null);
            ubiDell.setEnabled(Boolean.TRUE);
            ubiDell.setPassword(Util.encrypt(MAESTRO_PASS, tokenSecret));
            ubiDell.setUsername(MAESTRO);
            ubiDell.setUser(admin);
            ubiDell.setOrganization(organizationAdmin);
            ubiDell.setImageID(IMAGE_ID_1);
            ubiDell.setNetworkID("a4e98321-a3f1-4a12-bda9-26471aa75ef8");
            ubiDell.setProviderType(openstackProviderType);
            ubiDell.setInternalProvider(false);
            ubiDell.setPublicNetwork(PROVIDER);
            providerDAO.save(ubiDell);

            Region ubiDellGR = new Region();
            ubiDellGR.setDateCreated(new Date());
            ubiDellGR.setLastModified(new Date());
            ubiDellGR.setName(GR_ATHENS);
            ubiDellGR.setProvider(ubiDell);
            regionDAO.save(ubiDellGR);
        }

        if (Boolean.TRUE.equals(initializationProviderUbiDellAstrid)) {
            ubiDellAstrid = new Provider();
            ubiDellAstrid.setName("UBIDELL-astrid");
            ubiDellAstrid.setDateCreated(new Date());
            ubiDellAstrid.setLastModified(new Date());
            ubiDellAstrid.setDomain(DEFAULT);
            ubiDellAstrid.setEndpoint(ENDPOINT);
            ubiDellAstrid.setProject("astrid");
            ubiDellAstrid.setDefaultProvider(true);
            ubiDellAstrid.setMeshIdentifier(null);
            ubiDellAstrid.setEnabled(Boolean.TRUE);
            ubiDellAstrid.setPassword(Util.encrypt(MAESTRO_PASS, tokenSecret));
            ubiDellAstrid.setUsername(MAESTRO);
            ubiDellAstrid.setUser(admin);
            ubiDellAstrid.setOrganization(organizationAdmin);
            ubiDellAstrid.setImageID(IMAGE_ID_1);
            ubiDellAstrid.setNetworkID("02557cfe-25c1-40b6-b6c9-af624d7ac140");
            ubiDellAstrid.setProviderType(openstackProviderType);
            ubiDellAstrid.setInternalProvider(false);
            ubiDellAstrid.setPublicNetwork(PROVIDER);
            providerDAO.save(ubiDellAstrid);

            Region ubiDellAstridGR = new Region();
            ubiDellAstridGR.setDateCreated(new Date());
            ubiDellAstridGR.setLastModified(new Date());
            ubiDellAstridGR.setName(GR_ATHENS);
            ubiDellAstridGR.setProvider(ubiDellAstrid);
            regionDAO.save(ubiDellAstridGR);
        }

        if (Boolean.TRUE.equals(initializationProviderUbiDellSpider)) {
            ubiDellSpider = new Provider();
            ubiDellSpider.setName("UBIDELL-spider");
            ubiDellSpider.setDateCreated(new Date());
            ubiDellSpider.setLastModified(new Date());
            ubiDellSpider.setDomain(DEFAULT);
            ubiDellSpider.setEndpoint(ENDPOINT);
            ubiDellSpider.setProject("SPIDER");
            ubiDellSpider.setDefaultProvider(true);
            ubiDellSpider.setMeshIdentifier(null);
            ubiDellSpider.setEnabled(Boolean.TRUE);
            ubiDellSpider.setPassword(Util.encrypt(MAESTRO_PASS, tokenSecret));
            ubiDellSpider.setUsername(MAESTRO);
            ubiDellSpider.setUser(admin);
            ubiDellSpider.setOrganization(organizationAdmin);
            ubiDellSpider.setImageID(IMAGE_ID_1);
            ubiDellSpider.setNetworkID("59602168-1972-406e-99fa-0deb71587639");
            ubiDellSpider.setProviderType(openstackProviderType);
            ubiDellSpider.setInternalProvider(false);
            ubiDellSpider.setPublicNetwork(PROVIDER);
            providerDAO.save(ubiDellSpider);

            Region ubiDellSpiderGR = new Region();
            ubiDellSpiderGR.setDateCreated(new Date());
            ubiDellSpiderGR.setLastModified(new Date());
            ubiDellSpiderGR.setName(GR_ATHENS);
            ubiDellSpiderGR.setProvider(ubiDellSpider);
            regionDAO.save(ubiDellSpiderGR);
        }

        if (Boolean.TRUE.equals(initializationProviderUbiDellUnicorn)) {
            //Added to Ubitech Admin user
            ubiDellUnicorn = new Provider();
            ubiDellUnicorn.setName("UBIDELLUnicorn");
            ubiDellUnicorn.setDateCreated(new Date());
            ubiDellUnicorn.setLastModified(new Date());
            ubiDellUnicorn.setDomain(DEFAULT);
            ubiDellUnicorn.setEndpoint(ENDPOINT);
            ubiDellUnicorn.setProject("unicorn");
            ubiDellUnicorn.setDefaultProvider(!initializationProviderUbiDell);
            ubiDellUnicorn.setMeshIdentifier(null);
            ubiDellUnicorn.setEnabled(Boolean.TRUE);
            ubiDellUnicorn.setPassword(Util.encrypt(MAESTRO_PASS, tokenSecret));
            ubiDellUnicorn.setUsername(MAESTRO);
            ubiDellUnicorn.setUser(admin);
            ubiDellUnicorn.setOrganization(organizationAdmin);
            ubiDellUnicorn.setImageID(IMAGE_ID_2);
            ubiDellUnicorn.setNetworkID("5b9d622a-47ab-44fc-9e73-cebe50f0a75e");
            ubiDellUnicorn.setProviderType(openstackProviderType);
            ubiDellUnicorn.setInternalProvider(false);
            ubiDellUnicorn.setPublicNetwork(PROVIDER);
            providerDAO.save(ubiDellUnicorn);

            Region ubiDellUnicornGR = new Region();
            ubiDellUnicornGR.setDateCreated(new Date());
            ubiDellUnicornGR.setLastModified(new Date());
            ubiDellUnicornGR.setName(GR_ATHENS);
            ubiDellUnicornGR.setProvider(ubiDellUnicorn);
            regionDAO.save(ubiDellUnicornGR);
        }

        if (Boolean.TRUE.equals(initializationProviderUbiDellMatilda)) {
            ubiDellMatilda = new Provider();
            ubiDellMatilda.setName("UBIDELLMatilda");
            ubiDellMatilda.setDateCreated(new Date());
            ubiDellMatilda.setLastModified(new Date());
            ubiDellMatilda.setDomain(DEFAULT);
            ubiDellMatilda.setEndpoint(ENDPOINT);
            ubiDellMatilda.setProject(MATILDA);
            ubiDellMatilda.setDefaultProvider(!initializationProviderUbiDell);
            ubiDellMatilda.setMeshIdentifier(null);
            ubiDellMatilda.setEnabled(Boolean.TRUE);
            ubiDellMatilda.setPassword(Util.encrypt(MAESTRO_PASS, tokenSecret));
            ubiDellMatilda.setUsername(MAESTRO);
            ubiDellMatilda.setUser(admin);
            ubiDellMatilda.setOrganization(organizationAdmin);
            ubiDellMatilda.setImageID(IMAGE_ID_2);
            ubiDellMatilda.setNetworkID("65b525ee-77f1-4201-88b2-14f0ff99e9a6");
            ubiDellMatilda.setProviderType(openstackProviderType);
            ubiDellMatilda.setInternalProvider(false);
            ubiDellMatilda.setPublicNetwork(PROVIDER);
            providerDAO.save(ubiDellMatilda);

            Region ubiDellMatildaGR = new Region();
            ubiDellMatildaGR.setDateCreated(new Date());
            ubiDellMatildaGR.setLastModified(new Date());
            ubiDellMatildaGR.setName(GR_ATHENS);
            ubiDellMatildaGR.setProvider(ubiDellMatilda);
            regionDAO.save(ubiDellMatildaGR);
        }

        if (Boolean.TRUE.equals(initializationProviderUbiTestProvider)) {
            //Added to Ubitech Energy Group
            ubiTestProvider = new Provider();
            ubiTestProvider.setName("UBITestProvider");
            ubiTestProvider.setDateCreated(new Date());
            ubiTestProvider.setLastModified(new Date());
            ubiTestProvider.setDomain(DEFAULT);
            ubiTestProvider.setEndpoint(ENDPOINT);
            ubiTestProvider.setProject(MAESTRO);
            ubiTestProvider.setDefaultProvider(true);
            ubiTestProvider.setMeshIdentifier(null);
            ubiTestProvider.setEnabled(Boolean.TRUE);
            ubiTestProvider.setPassword(Util.encrypt(MAESTRO_PASS, tokenSecret));
            ubiTestProvider.setUsername(MAESTRO);
            ubiTestProvider.setUser(admin);
            ubiTestProvider.setOrganization(organizationAdmin);
            ubiTestProvider.setImageID(IMAGE_ID_2);
            ubiTestProvider.setNetworkID("f1cf2a31-6703-4be2-90fe-e6c312f08f40");
            ubiTestProvider.setProviderType(openstackProviderType);
            ubiTestProvider.setInternalProvider(false);
            ubiTestProvider.setPublicNetwork(PROVIDER);
            providerDAO.save(ubiTestProvider);

            Region ubiTestProviderGR = new Region();
            ubiTestProviderGR.setDateCreated(new Date());
            ubiTestProviderGR.setLastModified(new Date());
            ubiTestProviderGR.setName(GR_ATHENS);
            ubiTestProviderGR.setProvider(ubiTestProvider);
            regionDAO.save(ubiTestProviderGR);
        }

        if (Boolean.TRUE.equals(initializationRainbowKubernetesProvider)) {
            rainbowKubernetesProvider = new Provider();
            rainbowKubernetesProvider.setName("Rainbow Kubernetes");
            rainbowKubernetesProvider.setDateCreated(new Date());
            rainbowKubernetesProvider.setLastModified(new Date());
            rainbowKubernetesProvider.setEndpoint(rainbowEndpoint);
            rainbowKubernetesProvider.setUsername(rainbowCaCertificate);
            rainbowKubernetesProvider.setPublicKey(rainbowClientCertificate);
            rainbowKubernetesProvider.setPrivateKey(rainbowClientKey);
            rainbowKubernetesProvider.setProviderType(rainbowKubernetesProviderType);
            rainbowKubernetesProvider.setInternalProvider(false);
            rainbowKubernetesProvider.setOrganization(organizationAdmin);
            providerDAO.save(rainbowKubernetesProvider);

            Region rainbowKubernetesTestProviderGR = new Region();
            rainbowKubernetesTestProviderGR.setDateCreated(new Date());
            rainbowKubernetesTestProviderGR.setLastModified(new Date());
            rainbowKubernetesTestProviderGR.setName(GR_ATHENS);
            rainbowKubernetesTestProviderGR.setProvider(rainbowKubernetesProvider);
            regionDAO.save(rainbowKubernetesTestProviderGR);
        }

        logger.info("Providers have been added successfully!");
    }

    private void initializeSSHKeys() {

        logger.info("SSH keys initialization has started...");

        SSHKey adminKey = new SSHKey();
        adminKey.setDefaultSSH(true);
        adminKey.setFriendlyName("adminKey");
        adminKey.setLastModified(new Date());
        adminKey.setDateCreated(new Date());
        adminKey.setSshKey(
                "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC1uCM+WTrtr82waAez9ZrPnvIyArAVk5AxOXkS8E4genxu1D7YKK2prLDl8xOzOJmrWkfHtzSSDH+quxAaHCmdz/fMsiDt3CzsRLn1f5GqXy4BM+IvVd8M4585s/hRUmDEGohlt9Ro3gnE65zPAjz35pwkBLIWXZ4tSbhQnUJEjMlKjAtsEXIlJeC1YSTMNhvaBnB1qOMxwZbOUEdhxlbezIVlugLxrdhlEDDJBpR5xwSDgFwEKO/Z9eE+cLAVNEetShenHZevctMHcdEGFBie8MOF2hiKD3kKcPykH6ULPw2hs9seiIz51coGAcX5k3YHxvi7ngVqHjuQsRqUIe97 admin");
        adminKey.setUser(admin);
        adminKey.setOrganization(organizationAdmin);
        sshKeyDAO.save(adminKey);

        logger.info("SSH Keys have been added successfully!");
    }

    private void initializeLabels() {

        logger.info("Labels initialization has started...");

        // Labels
        labelLB = new Label();
        labelLB.setName("Load Balancer");
        labelLB.setDateCreated(new Date());
        labelLB.setLastModified(new Date());
        labelDAO.save(labelLB);

        labelSQLDB = new Label();
        labelSQLDB.setName("SQL Database");
        labelSQLDB.setDateCreated(new Date());
        labelSQLDB.setLastModified(new Date());
        labelDAO.save(labelSQLDB);

        labelNoSQL = new Label();
        labelNoSQL.setName("NoSQL Database");
        labelNoSQL.setDateCreated(new Date());
        labelNoSQL.setLastModified(new Date());
        labelDAO.save(labelNoSQL);

        labelDBManagement = new Label();
        labelDBManagement.setName("DB Management");
        labelDBManagement.setDateCreated(new Date());
        labelDBManagement.setLastModified(new Date());
        labelDAO.save(labelDBManagement);

        labelCM = new Label();
        labelCM.setName("Content Management");
        labelCM.setDateCreated(new Date());
        labelCM.setLastModified(new Date());
        labelDAO.save(labelCM);

        labelLambdaFunction = new Label();
        labelLambdaFunction.setName("Lambda Function");
        labelLambdaFunction.setDateCreated(new Date());
        labelLambdaFunction.setLastModified(new Date());
        labelDAO.save(labelLambdaFunction);

        labelARMLambdaFunction = new Label();
        labelARMLambdaFunction.setName("Lambda ARM Function");
        labelARMLambdaFunction.setDateCreated(new Date());
        labelARMLambdaFunction.setLastModified(new Date());
        labelDAO.save(labelARMLambdaFunction);

        labelCustomComponent = new Label();
        labelCustomComponent.setName("Custom Component");
        labelCustomComponent.setDateCreated(new Date());
        labelCustomComponent.setLastModified(new Date());
        labelDAO.save(labelCustomComponent);

        labelCustomARMComponent = new Label();
        labelCustomARMComponent.setName("Custom ARM Component");
        labelCustomARMComponent.setDateCreated(new Date());
        labelCustomARMComponent.setLastModified(new Date());
        labelDAO.save(labelCustomARMComponent);

        labelEcho = new Label();
        labelEcho.setName("Http Echo");
        labelEcho.setDateCreated(new Date());
        labelEcho.setLastModified(new Date());
        labelDAO.save(labelEcho);

        logger.info("Labels have been added successfully!");

    }

    private void initializeComponents() {

        logger.info("Components initialization has started...");

        // Initialize Load Balancer + Lambda Proxy components
        initializeBalancerAndLambdaProxyComponents();

        // Initialize DB components
        initializeDBComponents();

        // Initialize PHP components
        initializePHPComponents();

        // Initialize Echo component
        initializeHttpEchoComponent();

        logger.info("Components have been added successfully!");

    }

    private void initializeBalancerAndLambdaProxyComponents() {

        logger.info("Load balancer and lambda proxy initialization has started...");

//        List<CapabilityDrop> capabilitiesDrop = new ArrayList<>();
//        capabilitiesDrop.add(CapabilityDrop.DAC_OVERRIDE);
//        capabilitiesDrop.add(CapabilityDrop.FSETID);
//
//        List<CapabilityAdd> capabilitiesAdd = new ArrayList<>();
//        capabilitiesAdd.add(CapabilityAdd.SYS_MODULE);
//        capabilitiesAdd.add(CapabilityAdd.SYS_TTY_CONFIG);

//        // Load Balancer (Traefik)
//        loadBalancer = new eu.orchestrator.repository.domain.Component();
//        loadBalancer = new eu.orchestrator.repository.domain.Component();
//        loadBalancer.setName("Traefik");
//        loadBalancer.setHexID(Util.createRandomHEXString(entityManager));
//        loadBalancer.setDockerImage("traefik:alpine");
//        loadBalancer.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
//        loadBalancer.setDockerUsername(MAESTRO);
//        loadBalancer.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS,secretToken));
//        loadBalancer.setCapabilityDrops(capabilitiesDrop);
//        loadBalancer.setCapabilityAdds(capabilitiesAdd);
//        loadBalancer.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
//        loadBalancer.setPublicComponent(true);
//        loadBalancer.setDateCreated(new Date());
//        loadBalancer.setLastModified(new Date());
//        loadBalancer.setUser(admin);
//        loadBalancer.setOrganization(organizationAdmin);
//        loadBalancer.setElasticityControllerMode(eu.orchestrator.repository.domain.Component.Scaling.NONE.name());
//
//        loadBalancer.setPlugins(new TreeSet<>(listOfDefaultPlugins));
//
//        componentDAO.save(loadBalancer);
//
//        loadBalancer.setLabels(new TreeSet<>(Arrays.asList(labelLB)));
//
//        // Exposed Interface
//        Interface interfaceLBUI = new Interface();
//        interfaceLBUI.setDateCreated(new Date());
//        interfaceLBUI.setLastModified(new Date());
//        interfaceLBUI.setInterfaceType(Interface.InterfaceType.ACCESS.name());
//        interfaceLBUI.setName("traefikUI");
//        interfaceLBUI.setPort("15568");
//        interfaceLBUI.setVna("VNA0");
//        interfaceLBUI.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
//        interfaceLBUI.setComponent(loadBalancer);
//        interfaceDAO.save(interfaceLBUI);
//
//        // Minimum Execution Requirements
//        Requirement requirementLB = new Requirement();
//        requirementLB.setComponent(loadBalancer);
//        requirementLB.setDateCreated(new Date());
//        requirementLB.setLastModified(new Date());
//        requirementLB.setGpuRequired(false);
//        requirementLB.setHypervisorType("ESXI");
//        requirementLB.setRam(2048);
//        requirementLB.setStorage(20);
//        requirementLB.setvCPUs(1);
//        requirementDAO.save(requirementLB);
//
//        // Health Check
//        HealthCheck loadBalancerHealthCheck = new HealthCheck();
//        loadBalancerHealthCheck.setComponent(loadBalancer);
//        loadBalancerHealthCheck.setDateCreated(new Date());
//        loadBalancerHealthCheck.setLastModified(new Date());
//        loadBalancerHealthCheck.setName("LoadBalancerHealthCheck");
//        loadBalancerHealthCheck.setInterval(new Long(10));
//        loadBalancerHealthCheck.setArgs(null);
//        loadBalancerHealthCheck.setHttpURL("http://localhost:15568/metrics");
//        healthCheckDAO.save(loadBalancerHealthCheck);
//
//        componentDAO.save(loadBalancer);

//        for(ElasticityFrameworkBackend elasticityAdapter : (List<ElasticityFrameworkBackend>)elasticityFrameworkAdapters){
//            elasticityAdapter.addComponentToDB();
//        }

        traefikLoadBalancer.addComponentToDB();
        traefikLambdaProxy.addComponentToDB();

//        // Lambda Proxy
//        lambdaProxy = new eu.orchestrator.repository.domain.Component();
//        lambdaProxy.setName("LambdaProxy");
//        lambdaProxy.setHexID(Util.createRandomHEXString(entityManager));
//        lambdaProxy.setDockerImage("traefik:v1.7");
//        lambdaProxy.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
//        lambdaProxy.setDockerUsername(MAESTRO);
//        lambdaProxy.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS,secretToken));
//        lambdaProxy.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
//        lambdaProxy.setPublicComponent(true);
//        lambdaProxy.setDateCreated(new Date());
//        lambdaProxy.setLastModified(new Date());
//        lambdaProxy.setUser(admin);
//        lambdaProxy.setOrganization(organizationAdmin);
//        lambdaProxy.setElasticityControllerMode(eu.orchestrator.repository.domain.Component.Scaling.NONE.name());
//        lambdaProxy.setPlugins(new TreeSet<>(listOfDefaultPlugins));
//
//        componentDAO.save(lambdaProxy);
//
//        lambdaProxy.setLabels(new TreeSet<>(Arrays.asList(labelLB)));
//
//        // Exposed Interface
//        Interface interfaceLambdaProxyUI = new Interface();
//        interfaceLambdaProxyUI.setDateCreated(new Date());
//        interfaceLambdaProxyUI.setLastModified(new Date());
//        interfaceLambdaProxyUI.setInterfaceType(Interface.InterfaceType.ACCESS.name());
//        interfaceLambdaProxyUI.setName("lambdaProxyUI");
//        interfaceLambdaProxyUI.setPort("15568");
//        interfaceLambdaProxyUI.setVna("VNA0");
//        interfaceLambdaProxyUI.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
//        interfaceLambdaProxyUI.setComponent(lambdaProxy);
//        interfaceDAO.save(interfaceLambdaProxyUI);
//
//        // Minimum Execution Requirements
//        Requirement requirementLP = new Requirement();
//        requirementLP.setComponent(lambdaProxy);
//        requirementLP.setDateCreated(new Date());
//        requirementLP.setLastModified(new Date());
//        requirementLP.setGpuRequired(false);
//        requirementLP.setHypervisorType("ESXI");
//        requirementLP.setRam(2048);
//        requirementLP.setStorage(20);
//        requirementLP.setvCPUs(1);
//        requirementDAO.save(requirementLP);
//
//        // Health Check
//        HealthCheck loadProxyHealthCheck = new HealthCheck();
//        loadProxyHealthCheck.setComponent(lambdaProxy);
//        loadProxyHealthCheck.setDateCreated(new Date());
//        loadProxyHealthCheck.setLastModified(new Date());
//        loadProxyHealthCheck.setName("LambdaProxyHealthCheck");
//        loadProxyHealthCheck.setInterval(new Long(10));
//        loadProxyHealthCheck.setArgs(null);
//        loadProxyHealthCheck.setHttpURL("http://localhost:15568/metrics");
//        healthCheckDAO.save(loadProxyHealthCheck);
//
//        componentDAO.save(lambdaProxy);

        logger.info("Load balancer and lambda proxy have been added successfully!");

    }

    private void initializeDBComponents() {

        logger.info("Database components initialization has started...");

        // MariaDB
        Optional<Plugin> mySQLPlugin = pluginDAO.findByNameAndModuleName(PYTHON_D, MYSQL);

        mariaDB = new eu.orchestrator.repository.domain.Component();
        mariaDB.setName(MARIADB_CONST);
        mariaDB.setHexID(Util.createRandomHEXString());
        mariaDB.setDockerImage("mariadb:10.2.14");
        mariaDB.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        mariaDB.setDockerUsername(MAESTRO);
        mariaDB.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        mariaDB.setArchitecture(Architecture.AMD64.getFriendlyName());
        mariaDB.setPublicComponent(true);
        mariaDB.setDateCreated(new Date());
        mariaDB.setLastModified(new Date());
        mariaDB.setUser(admin);
        mariaDB.setOrganization(organizationAdmin);
        mariaDB.setElasticityController("NONE");

        SortedSet<Plugin> mariaDBPlugins = new TreeSet<>();
        mySQLPlugin.ifPresent(mariaDBPlugins::add);
        mariaDBPlugins.addAll(listOfDefaultPlugins);
        mariaDB.setPlugins(mariaDBPlugins);
        componentDAO.save(mariaDB);

        // Labels
        mariaDB.setLabels(new TreeSet<>(Arrays.asList(labelSQLDB)));

        // Environmental Variables
        EnvironmentalVariable mariaDBEnvironmentalVariableA = new EnvironmentalVariable();
        mariaDBEnvironmentalVariableA.setComponent(mariaDB);
        mariaDBEnvironmentalVariableA.setDateCreated(new Date());
        mariaDBEnvironmentalVariableA.setLastModified(new Date());
        mariaDBEnvironmentalVariableA.setKey("MYSQL_DATABASE");

        mariaDBEnvironmentalVariableA.setValue(WORDPRESS_CONST);
        environmentalVariableDAO.save(mariaDBEnvironmentalVariableA);

        EnvironmentalVariable mariaDBEnvironmentalVariableB = new EnvironmentalVariable();
        mariaDBEnvironmentalVariableB.setComponent(mariaDB);
        mariaDBEnvironmentalVariableB.setDateCreated(new Date());
        mariaDBEnvironmentalVariableB.setLastModified(new Date());
        mariaDBEnvironmentalVariableB.setKey("MYSQL_USER");
        mariaDBEnvironmentalVariableB.setValue(WORDPRESS_CONST);
        environmentalVariableDAO.save(mariaDBEnvironmentalVariableB);

        EnvironmentalVariable mariaDBEnvironmentalVariableC = new EnvironmentalVariable();
        mariaDBEnvironmentalVariableC.setComponent(mariaDB);
        mariaDBEnvironmentalVariableC.setDateCreated(new Date());
        mariaDBEnvironmentalVariableC.setLastModified(new Date());
        mariaDBEnvironmentalVariableC.setKey("MYSQL_PASSWORD");
        mariaDBEnvironmentalVariableC.setValue(WORDPRESS_CONST);
        environmentalVariableDAO.save(mariaDBEnvironmentalVariableC);

        EnvironmentalVariable mariaDBEnvironmentalVariableD = new EnvironmentalVariable();
        mariaDBEnvironmentalVariableD.setComponent(mariaDB);
        mariaDBEnvironmentalVariableD.setDateCreated(new Date());
        mariaDBEnvironmentalVariableD.setLastModified(new Date());
        mariaDBEnvironmentalVariableD.setKey("MYSQL_ROOT_PASSWORD");
        mariaDBEnvironmentalVariableD.setValue("xUNvbFUbHvv6hnkrTg86g7fXe87W9fTg");
        environmentalVariableDAO.save(mariaDBEnvironmentalVariableD);

        // Exposed Interface
        interfaceSQL = new Interface();
        interfaceSQL.setDateCreated(new Date());
        interfaceSQL.setLastModified(new Date());
        interfaceSQL.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceSQL.setName("mariaDBSQLInterface");
        interfaceSQL.setPort("3306");
        interfaceSQL.setVna("VNA0");
        interfaceSQL.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceSQL.setComponent(mariaDB);
        interfaceDAO.save(interfaceSQL);

        // Minimum Execution Requirements
        Requirement mariaDBMinimumExecutionRequirements = new Requirement();
        mariaDBMinimumExecutionRequirements.setComponent(mariaDB);
        mariaDBMinimumExecutionRequirements.setDateCreated(new Date());
        mariaDBMinimumExecutionRequirements.setLastModified(new Date());
        mariaDBMinimumExecutionRequirements.setGpuRequired(false);
        mariaDBMinimumExecutionRequirements.setHypervisorType("ESXI");
        mariaDBMinimumExecutionRequirements.setRam(2048);
        mariaDBMinimumExecutionRequirements.setStorage(20);
        mariaDBMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(mariaDBMinimumExecutionRequirements);

        // Health Check
        HealthCheck mariaDBHealthCheck = new HealthCheck();
        mariaDBHealthCheck.setComponent(mariaDB);
        mariaDBHealthCheck.setDateCreated(new Date());
        mariaDBHealthCheck.setLastModified(new Date());
        mariaDBHealthCheck.setName("MariaDBHealthCheck");
        mariaDBHealthCheck.setInterval(10L);
        mariaDBHealthCheck.setArgs("mysqladmin -uroot -pxUNvbFUbHvv6hnkrTg86g7fXe87W9fTg status");
        mariaDBHealthCheck.setHttpURL(null);
        healthCheckDAO.save(mariaDBHealthCheck);

        componentDAO.save(mariaDB);

        // Mongo DB
        Optional<Plugin> mongoDBPlugin = pluginDAO.findByNameAndModuleName(PYTHON_D, MONGODB_CONST);

        mongoDB = new eu.orchestrator.repository.domain.Component();
        mongoDB.setName("MongoDB");
        mongoDB.setHexID(Util.createRandomHEXString());
        mongoDB.setDockerImage("mongo:rc");
        mongoDB.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        mongoDB.setDockerUsername(MAESTRO);
        mongoDB.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        mongoDB.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        mongoDB.setPublicComponent(true);
        mongoDB.setDateCreated(new Date());
        mongoDB.setLastModified(new Date());
        mongoDB.setUser(admin);
        mongoDB.setOrganization(organizationAdmin);
        mongoDB.setElasticityController("NONE");

        SortedSet<Plugin> mongoDBPlugins = new TreeSet<>();
        mongoDBPlugin.ifPresent(mongoDBPlugins::add);
        mongoDBPlugins.addAll(listOfDefaultPlugins);
        mongoDB.setPlugins(mongoDBPlugins);

        componentDAO.save(mongoDB);

        // Labels
        mongoDB.setLabels(new TreeSet<>(Arrays.asList(labelNoSQL)));

        // Environmental Variables
        EnvironmentalVariable mongoDBEnvironmentalVariableA = new EnvironmentalVariable();
        mongoDBEnvironmentalVariableA.setComponent(mongoDB);
        mongoDBEnvironmentalVariableA.setDateCreated(new Date());
        mongoDBEnvironmentalVariableA.setDateCreated(new Date());
        mongoDBEnvironmentalVariableA.setLastModified(new Date());
        mongoDBEnvironmentalVariableA.setKey("MONGO_DATABASE");
        mongoDBEnvironmentalVariableA.setValue("database");
        environmentalVariableDAO.save(mongoDBEnvironmentalVariableA);

        EnvironmentalVariable mongoDBEnvironmentalVariableB = new EnvironmentalVariable();
        mongoDBEnvironmentalVariableB.setComponent(mongoDB);
        mongoDBEnvironmentalVariableB.setDateCreated(new Date());
        mongoDBEnvironmentalVariableB.setLastModified(new Date());
        mongoDBEnvironmentalVariableB.setKey("MONGO_USER");
        mongoDBEnvironmentalVariableB.setValue("root");
        environmentalVariableDAO.save(mongoDBEnvironmentalVariableB);

        EnvironmentalVariable mongoDBEnvironmentalVariableC = new EnvironmentalVariable();
        mongoDBEnvironmentalVariableC.setComponent(mongoDB);
        mongoDBEnvironmentalVariableC.setDateCreated(new Date());
        mongoDBEnvironmentalVariableC.setLastModified(new Date());
        mongoDBEnvironmentalVariableC.setKey("MONGO_PASSWORD");
        mongoDBEnvironmentalVariableC.setValue("sMCyBeVyJry5Z6pAbEdPX3JhEX5T8J7b");
        environmentalVariableDAO.save(mongoDBEnvironmentalVariableC);

        // Exposed Interface
        interfaceNoSQL = new Interface();
        interfaceNoSQL.setDateCreated(new Date());
        interfaceNoSQL.setLastModified(new Date());
        interfaceNoSQL.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceNoSQL.setName("mongoDBNoSQLInterface");
        interfaceNoSQL.setVna("VNA0");
        interfaceNoSQL.setPort(PORT);
        interfaceNoSQL.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceNoSQL.setComponent(mongoDB);
        interfaceDAO.save(interfaceNoSQL);

        // Minimum Execution Requirements
        Requirement mongoDBMinimumExecutionRequirements = new Requirement();
        mongoDBMinimumExecutionRequirements.setComponent(mongoDB);
        mongoDBMinimumExecutionRequirements.setDateCreated(new Date());
        mongoDBMinimumExecutionRequirements.setLastModified(new Date());
        mongoDBMinimumExecutionRequirements.setGpuRequired(false);
        mongoDBMinimumExecutionRequirements.setHypervisorType("ESXI");
        mongoDBMinimumExecutionRequirements.setRam(2048);
        mongoDBMinimumExecutionRequirements.setStorage(20);
        mongoDBMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(mongoDBMinimumExecutionRequirements);

        // Health Check
        HealthCheck mongoDBHealthCheck = new HealthCheck();
        mongoDBHealthCheck.setComponent(mongoDB);
        mongoDBHealthCheck.setDateCreated(new Date());
        mongoDBHealthCheck.setLastModified(new Date());
        mongoDBHealthCheck.setName("MongoDBHealthCheck");
        mongoDBHealthCheck.setInterval(10L);
        mongoDBHealthCheck.setArgs("mongo");
        mongoDBHealthCheck.setHttpURL(null);
        healthCheckDAO.save(mongoDBHealthCheck);

        componentDAO.save(mongoDB);

        logger.info("Database components have been added successfully!");
    }

    private void initializePHPComponents() {

        logger.info("PHP components initialization has started...");

        // PhpMyAdmin
        phpMyAdmin = new eu.orchestrator.repository.domain.Component();
        phpMyAdmin.setName("phpMyAdmin");
        phpMyAdmin.setHexID(Util.createRandomHEXString());
        phpMyAdmin.setDockerImage("phpmyadmin/phpmyadmin:4.7");
        phpMyAdmin.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        phpMyAdmin.setDockerUsername(MAESTRO);
        phpMyAdmin.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        phpMyAdmin.setArchitecture(Architecture.AMD64.getFriendlyName());
        phpMyAdmin.setPublicComponent(true);
        phpMyAdmin.setDateCreated(new Date());
        phpMyAdmin.setLastModified(new Date());
        phpMyAdmin.setUser(admin);
        phpMyAdmin.setOrganization(organizationAdmin);
        phpMyAdmin.setElasticityController("NONE");
        phpMyAdmin.setPlugins(new TreeSet<>(listOfDefaultPlugins));

        componentDAO.save(phpMyAdmin);

        // Labels
        phpMyAdmin.setLabels(new TreeSet<>(Arrays.asList(labelDBManagement)));

        // Environmental Variables
        EnvironmentalVariable phpMyAdminEnvironmentalVariableA = new EnvironmentalVariable();
        phpMyAdminEnvironmentalVariableA.setComponent(phpMyAdmin);
        phpMyAdminEnvironmentalVariableA.setDateCreated(new Date());
        phpMyAdminEnvironmentalVariableA.setLastModified(new Date());
        phpMyAdminEnvironmentalVariableA.setKey("PMA_PORT");
        phpMyAdminEnvironmentalVariableA.setValue("3306");
        environmentalVariableDAO.save(phpMyAdminEnvironmentalVariableA);

        EnvironmentalVariable phpMyAdminEnvironmentalVariableB = new EnvironmentalVariable();
        phpMyAdminEnvironmentalVariableB.setComponent(phpMyAdmin);
        phpMyAdminEnvironmentalVariableB.setDateCreated(new Date());
        phpMyAdminEnvironmentalVariableB.setLastModified(new Date());
        phpMyAdminEnvironmentalVariableB.setKey("PMA_HOST");
        phpMyAdminEnvironmentalVariableB.setValue("@MariaDB");
        environmentalVariableDAO.save(phpMyAdminEnvironmentalVariableB);

        // Exposed Interface
        interfaceHttp = new Interface();
        interfaceHttp.setDateCreated(new Date());
        interfaceHttp.setLastModified(new Date());
        interfaceHttp.setInterfaceType(Interface.InterfaceType.ACCESS.name());
        interfaceHttp.setName("phpMyAdminAccessInterface");
        interfaceHttp.setPort("80");
        interfaceHttp.setVna("VNA0");
        interfaceHttp.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceHttp.setComponent(phpMyAdmin);
        interfaceDAO.save(interfaceHttp);

        // Required Interface
        phpMyAdminGraphLink = new GraphLink();
        phpMyAdminGraphLink.setInterfaceObj(interfaceSQL);
        phpMyAdminGraphLink.setComponent(phpMyAdmin);
        phpMyAdminGraphLink.setDateCreated(new Date());
        phpMyAdminGraphLink.setLastModified(new Date());
        graphLinkDAO.save(phpMyAdminGraphLink);

        // Minimum Execution Requirements
        Requirement phpMyAdminMinimumExecutionRequirements = new Requirement();
        phpMyAdminMinimumExecutionRequirements.setComponent(phpMyAdmin);
        phpMyAdminMinimumExecutionRequirements.setDateCreated(new Date());
        phpMyAdminMinimumExecutionRequirements.setLastModified(new Date());
        phpMyAdminMinimumExecutionRequirements.setGpuRequired(false);
        phpMyAdminMinimumExecutionRequirements.setHypervisorType("ESXI");
        phpMyAdminMinimumExecutionRequirements.setRam(2048);
        phpMyAdminMinimumExecutionRequirements.setStorage(20);
        phpMyAdminMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(phpMyAdminMinimumExecutionRequirements);

        // Health Check
        HealthCheck phpMyAdminHealthCheck = new HealthCheck();
        phpMyAdminHealthCheck.setComponent(phpMyAdmin);
        phpMyAdminHealthCheck.setDateCreated(new Date());
        phpMyAdminHealthCheck.setLastModified(new Date());
        phpMyAdminHealthCheck.setName("PhpMyAdminHealthCheck");
        phpMyAdminHealthCheck.setInterval(10L);
        phpMyAdminHealthCheck.setArgs(null);
        phpMyAdminHealthCheck.setHttpURL(LOCALHOST);
        healthCheckDAO.save(phpMyAdminHealthCheck);

        componentDAO.save(phpMyAdmin);

        // WordPress
        wordPress = new eu.orchestrator.repository.domain.Component();
        wordPress.setName("WordPress");
        wordPress.setHexID(Util.createRandomHEXString());
        wordPress.setDockerImage("wordpress:4");
        wordPress.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        wordPress.setDockerUsername(MAESTRO);
        wordPress.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        wordPress.setArchitecture(Architecture.AMD64.getFriendlyName());
        wordPress.setPublicComponent(true);
        wordPress.setDateCreated(new Date());
        wordPress.setLastModified(new Date());
        wordPress.setUser(admin);
        wordPress.setOrganization(organizationAdmin);
        wordPress.setElasticityController(HORIZONTAL);
        wordPress.setPlugins(new TreeSet<>(listOfDefaultPlugins));

        componentDAO.save(wordPress);

        // Labels
        wordPress.setLabels(new TreeSet<>(Arrays.asList(labelCM)));

        // Environmental Variables
        EnvironmentalVariable wordPressEnvironmentalVariableA = new EnvironmentalVariable();
        wordPressEnvironmentalVariableA.setComponent(wordPress);
        wordPressEnvironmentalVariableA.setDateCreated(new Date());
        wordPressEnvironmentalVariableA.setLastModified(new Date());
        wordPressEnvironmentalVariableA.setKey("WORDPRESS_DB_PASSWORD");
        wordPressEnvironmentalVariableA.setValue(WORDPRESS_CONST);
        environmentalVariableDAO.save(wordPressEnvironmentalVariableA);

        EnvironmentalVariable wordPressEnvironmentalVariableB = new EnvironmentalVariable();
        wordPressEnvironmentalVariableB.setComponent(wordPress);
        wordPressEnvironmentalVariableB.setDateCreated(new Date());
        wordPressEnvironmentalVariableB.setLastModified(new Date());
        wordPressEnvironmentalVariableB.setKey("WORDPRESS_DB_HOST");
        wordPressEnvironmentalVariableB.setValue("@MariaDB");
        environmentalVariableDAO.save(wordPressEnvironmentalVariableB);

        EnvironmentalVariable wordPressEnvironmentalVariableC = new EnvironmentalVariable();
        wordPressEnvironmentalVariableC.setComponent(wordPress);
        wordPressEnvironmentalVariableC.setDateCreated(new Date());
        wordPressEnvironmentalVariableC.setLastModified(new Date());
        wordPressEnvironmentalVariableC.setKey("WORDPRESS_DB_USER");
        wordPressEnvironmentalVariableC.setValue(WORDPRESS_CONST);
        environmentalVariableDAO.save(wordPressEnvironmentalVariableC);

        // Exposed Interface
        Interface interfaceWpHttp = new Interface();
        interfaceWpHttp.setDateCreated(new Date());
        interfaceWpHttp.setLastModified(new Date());
        interfaceWpHttp.setInterfaceType(Interface.InterfaceType.ACCESS.name());
        interfaceWpHttp.setName("WordPressAccessInterface");
        interfaceWpHttp.setPort("80");
        interfaceWpHttp.setVna("VNA0");
        interfaceWpHttp.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceWpHttp.setComponent(wordPress);
        interfaceDAO.save(interfaceWpHttp);

        // Required Interface
        wordPressGraphLink = new GraphLink();
        wordPressGraphLink.setInterfaceObj(interfaceSQL);
        wordPressGraphLink.setComponent(wordPress);
        wordPressGraphLink.setDateCreated(new Date());
        wordPressGraphLink.setLastModified(new Date());
        graphLinkDAO.save(wordPressGraphLink);

        // Minimum Execution Requirements
        Requirement wordPressMinimumExecutionRequirement = new Requirement();
        wordPressMinimumExecutionRequirement.setComponent(wordPress);
        wordPressMinimumExecutionRequirement.setDateCreated(new Date());
        wordPressMinimumExecutionRequirement.setLastModified(new Date());
        wordPressMinimumExecutionRequirement.setGpuRequired(false);
        wordPressMinimumExecutionRequirement.setHypervisorType("ESXI");
        wordPressMinimumExecutionRequirement.setRam(2048);
        wordPressMinimumExecutionRequirement.setStorage(20);
        wordPressMinimumExecutionRequirement.setvCPUs(1);
        requirementDAO.save(wordPressMinimumExecutionRequirement);

        // Health Check
        HealthCheck wordPressHealthCheck = new HealthCheck();
        wordPressHealthCheck.setComponent(wordPress);
        wordPressHealthCheck.setDateCreated(new Date());
        wordPressHealthCheck.setLastModified(new Date());
        wordPressHealthCheck.setName("WordPressHealthCheck");
        wordPressHealthCheck.setInterval(10L);
        wordPressHealthCheck.setArgs(null);
        wordPressHealthCheck.setHttpURL(LOCALHOST);
        healthCheckDAO.save(wordPressHealthCheck);

        componentDAO.save(wordPress);

        logger.info("PHP components have been added successfully!");

    }

    private void initializeHttpEchoComponent() {

        logger.info("Echo component initialization has started...");

        //Http-echo

        httpEcho = new eu.orchestrator.repository.domain.Component();
        httpEcho.setName("HttpEcho");
        httpEcho.setHexID(Util.createRandomHEXString());
        httpEcho.setDockerImage("hashicorp/http-echo:0.2.3");
        httpEcho.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        httpEcho.setDockerUsername(MAESTRO);
        httpEcho.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        httpEcho.setArchitecture(Architecture.AMD64.getFriendlyName());
        httpEcho.setPublicComponent(true);
        httpEcho.setDateCreated(new Date());
        httpEcho.setLastModified(new Date());
        httpEcho.setUser(admin);
        httpEcho.setOrganization(organizationAdmin);
        httpEcho.setElasticityController("NONE");
        httpEcho.setCommand("-text=\"hello world\" ");

        httpEcho.setLabels(new TreeSet<>(Arrays.asList(labelEcho)));

        // Exposed Interface
        interfaceHttpEcho = new Interface();
        interfaceHttpEcho.setDateCreated(new Date());
        interfaceHttpEcho.setLastModified(new Date());
        interfaceHttpEcho.setInterfaceType(InterfaceType.ACCESS.name());
        interfaceHttpEcho.setName("httpEchoInterface");
        interfaceHttpEcho.setPort("5678");
        interfaceHttpEcho.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceHttpEcho.setComponent(httpEcho);
        interfaceDAO.save(interfaceHttpEcho);

        // Minimum Execution Requirements
        Requirement httpEchoMinimumExecutionRequirements = new Requirement();
        httpEchoMinimumExecutionRequirements.setComponent(httpEcho);
        httpEchoMinimumExecutionRequirements.setDateCreated(new Date());
        httpEchoMinimumExecutionRequirements.setLastModified(new Date());
        httpEchoMinimumExecutionRequirements.setGpuRequired(false);
        httpEchoMinimumExecutionRequirements.setHypervisorType("ESXI");
        httpEchoMinimumExecutionRequirements.setRam(2048);
        httpEchoMinimumExecutionRequirements.setStorage(20);
        httpEchoMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(httpEchoMinimumExecutionRequirements);

        // Health Check
        HealthCheck httpEchoHealthCheck = new HealthCheck();
        httpEchoHealthCheck.setComponent(httpEcho);
        httpEchoHealthCheck.setDateCreated(new Date());
        httpEchoHealthCheck.setLastModified(new Date());
        httpEchoHealthCheck.setName("HttpEchoHealthCheck");
        httpEchoHealthCheck.setInterval(20L);
        httpEchoHealthCheck.setArgs(null);
        httpEchoHealthCheck.setHttpURL(LOCALHOST_5678);
        healthCheckDAO.save(httpEchoHealthCheck);

        componentDAO.save(httpEcho);

        logger.info("Echo component has been added successfully!");
    }

    private void initializeApplications() {

        logger.info("Applications initialization has started...");

        // DBMS
        dbms = new eu.orchestrator.repository.domain.Application();
        dbms.setName("DBMS");
        dbms.setHexID(Util.createRandomHEXString());
        dbms.setDateCreated(new Date());
        dbms.setUser(admin);
        dbms.setOrganization(organizationAdmin);
        dbms.setPublicApplication(true);
        dbms.setLastModified(new Date());
        applicationDAO.save(dbms);

        // Component Nodes
        ComponentNode dbmsMariaDB = new ComponentNode();
        dbmsMariaDB.setName(MARIADB_CONST);
        dbmsMariaDB.setHexID(Util.createRandomHEXString());
        dbmsMariaDB.setComponent(mariaDB);
        dbmsMariaDB.setDateCreated(new Date());
        dbmsMariaDB.setLastModified(new Date());
        dbmsMariaDB.setApplication(dbms);
        componentNodeDAO.save(dbmsMariaDB);

        ComponentNode dbmsPhpMyAdmin = new ComponentNode();
        dbmsPhpMyAdmin.setName("PhpMyAdmin");
        dbmsPhpMyAdmin.setHexID(Util.createRandomHEXString());
        dbmsPhpMyAdmin.setComponent(phpMyAdmin);
        dbmsPhpMyAdmin.setDateCreated(new Date());
        dbmsPhpMyAdmin.setLastModified(new Date());
        dbmsPhpMyAdmin.setApplication(dbms);
        componentNodeDAO.save(dbmsPhpMyAdmin);

        // Graph Link Nodes
        GraphLinkNode dbmsGraphLinkNodePhpMyAdminToMariaDB = new GraphLinkNode();
        dbmsGraphLinkNodePhpMyAdminToMariaDB.setComponentNodeFrom(dbmsPhpMyAdmin);
        dbmsGraphLinkNodePhpMyAdminToMariaDB.setComponentNodeTo(dbmsMariaDB);
        dbmsGraphLinkNodePhpMyAdminToMariaDB.setGraphLink(phpMyAdminGraphLink);
        dbmsGraphLinkNodePhpMyAdminToMariaDB.setApplication(dbms);
        dbmsGraphLinkNodePhpMyAdminToMariaDB.setDateCreated(new Date());
        dbmsGraphLinkNodePhpMyAdminToMariaDB.setLastModified(new Date());
        graphLinkNodeDAO.save(dbmsGraphLinkNodePhpMyAdminToMariaDB);

        // CMS
        cms = new eu.orchestrator.repository.domain.Application();
        cms.setName("CMSApp");
        cms.setHexID(Util.createRandomHEXString());
        cms.setDateCreated(new Date());
        cms.setUser(admin);
        cms.setOrganization(organizationAdmin);
        cms.setPublicApplication(true);
        cms.setLastModified(new Date());
        applicationDAO.save(cms);

        // Component Nodes
        ComponentNode cmsMariaDB = new ComponentNode();
        cmsMariaDB.setHexID(Util.createRandomHEXString());
        cmsMariaDB.setName(MARIADB_CONST);
        cmsMariaDB.setApplication(cms);
        cmsMariaDB.setComponent(mariaDB);
        cmsMariaDB.setDateCreated(new Date());
        cmsMariaDB.setLastModified(new Date());
        componentNodeDAO.save(cmsMariaDB);

        ComponentNode cmsPhpMyAdmin = new ComponentNode();
        cmsPhpMyAdmin.setHexID(Util.createRandomHEXString());
        cmsPhpMyAdmin.setName("PhpMyAdmin");
        cmsPhpMyAdmin.setApplication(cms);
        cmsPhpMyAdmin.setComponent(phpMyAdmin);
        cmsPhpMyAdmin.setDateCreated(new Date());
        cmsPhpMyAdmin.setLastModified(new Date());
        componentNodeDAO.save(cmsPhpMyAdmin);

        ComponentNode cmsWordPress = new ComponentNode();
        cmsWordPress.setHexID(Util.createRandomHEXString());
        cmsWordPress.setName("WordPress");
        cmsWordPress.setApplication(cms);
        cmsWordPress.setComponent(wordPress);
        cmsWordPress.setDateCreated(new Date());
        cmsWordPress.setLastModified(new Date());
        componentNodeDAO.save(cmsWordPress);

        // Graph Link Nodes
        GraphLinkNode cmsGraphLinkNodePhpMyAdminToMariaDB = new GraphLinkNode();
        cmsGraphLinkNodePhpMyAdminToMariaDB.setComponentNodeFrom(cmsPhpMyAdmin);
        cmsGraphLinkNodePhpMyAdminToMariaDB.setComponentNodeTo(cmsMariaDB);
        cmsGraphLinkNodePhpMyAdminToMariaDB.setGraphLink(phpMyAdminGraphLink);
        cmsGraphLinkNodePhpMyAdminToMariaDB.setApplication(cms);
        cmsGraphLinkNodePhpMyAdminToMariaDB.setDateCreated(new Date());
        cmsGraphLinkNodePhpMyAdminToMariaDB.setLastModified(new Date());
        graphLinkNodeDAO.save(cmsGraphLinkNodePhpMyAdminToMariaDB);

        GraphLinkNode cmsGraphLinkNodeWordPressToMariaDB = new GraphLinkNode();
        cmsGraphLinkNodeWordPressToMariaDB.setComponentNodeFrom(cmsWordPress);
        cmsGraphLinkNodeWordPressToMariaDB.setComponentNodeTo(cmsMariaDB);
        cmsGraphLinkNodeWordPressToMariaDB.setGraphLink(wordPressGraphLink);
        cmsGraphLinkNodeWordPressToMariaDB.setApplication(cms);
        cmsGraphLinkNodeWordPressToMariaDB.setDateCreated(new Date());
        cmsGraphLinkNodeWordPressToMariaDB.setLastModified(new Date());
        graphLinkNodeDAO.save(cmsGraphLinkNodeWordPressToMariaDB);

        logger.info("Applications have been added successfully!");

    }

    private void initializePilotPPDR() {

        logger.info("PPDR initialization has started...");

        // ppdr Database
        ppdrDatabase = new eu.orchestrator.repository.domain.Component();
        ppdrDatabase.setName("PPDRDatabase");
        ppdrDatabase.setHexID(Util.createRandomHEXString());
        ppdrDatabase.setDockerImage("mysql:5.5");
        ppdrDatabase.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        ppdrDatabase.setDockerUsername(MAESTRO);
        ppdrDatabase.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        ppdrDatabase.setPublicComponent(true);
        ppdrDatabase.setDateCreated(new Date());
        ppdrDatabase.setLastModified(new Date());
        ppdrDatabase.setUser(admin);
        ppdrDatabase.setOrganization(admin.getOrganization());
        ppdrDatabase.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        ppdrDatabase.setElasticityController("NONE");
        ppdrDatabase.setPlugins(new TreeSet<>(listOfDefaultPlugins));
        componentDAO.save(ppdrDatabase);

        // Labels
        ppdrDatabase.setLabels(new TreeSet<>(Arrays.asList(labelSQLDB)));

        // Environmental Variables
        EnvironmentalVariable mySQLEnvironmentalVariableB = new EnvironmentalVariable();
        mySQLEnvironmentalVariableB.setComponent(ppdrDatabase);
        mySQLEnvironmentalVariableB.setDateCreated(new Date());
        mySQLEnvironmentalVariableB.setLastModified(new Date());
        mySQLEnvironmentalVariableB.setKey("MYSQL_USER");
        mySQLEnvironmentalVariableB.setValue("imon");
        environmentalVariableDAO.save(mySQLEnvironmentalVariableB);

        EnvironmentalVariable mySQLEnvironmentalVariableC = new EnvironmentalVariable();
        mySQLEnvironmentalVariableC.setComponent(ppdrDatabase);
        mySQLEnvironmentalVariableC.setDateCreated(new Date());
        mySQLEnvironmentalVariableC.setLastModified(new Date());
        mySQLEnvironmentalVariableC.setKey("MYSQL_PASSWORD");
        mySQLEnvironmentalVariableC.setValue("imon");
        environmentalVariableDAO.save(mySQLEnvironmentalVariableC);

        EnvironmentalVariable mySQLEnvironmentalVariableD = new EnvironmentalVariable();
        mySQLEnvironmentalVariableD.setComponent(ppdrDatabase);
        mySQLEnvironmentalVariableD.setDateCreated(new Date());
        mySQLEnvironmentalVariableD.setLastModified(new Date());
        mySQLEnvironmentalVariableD.setKey("MYSQL_ROOT_PASSWORD");
        mySQLEnvironmentalVariableD.setValue(MATILDA);
        environmentalVariableDAO.save(mySQLEnvironmentalVariableD);

        // Exposed Interface
        interfaceSQLppdr = new Interface();
        interfaceSQLppdr.setDateCreated(new Date());
        interfaceSQLppdr.setLastModified(new Date());
        interfaceSQLppdr.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceSQLppdr.setName("sqlInterface");
        interfaceSQLppdr.setPort("3306");
        interfaceSQLppdr.setVna("VNA0");
        interfaceSQLppdr.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceSQLppdr.setComponent(ppdrDatabase);
        interfaceDAO.save(interfaceSQLppdr);

        // Minimum Execution Requirements
        Requirement mySQLMinimumExecutionRequirements = new Requirement();
        mySQLMinimumExecutionRequirements.setComponent(ppdrDatabase);
        mySQLMinimumExecutionRequirements.setDateCreated(new Date());
        mySQLMinimumExecutionRequirements.setLastModified(new Date());
        mySQLMinimumExecutionRequirements.setGpuRequired(false);
        mySQLMinimumExecutionRequirements.setHypervisorType("ESXI");
        mySQLMinimumExecutionRequirements.setRam(2048);
        mySQLMinimumExecutionRequirements.setStorage(20);
        mySQLMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(mySQLMinimumExecutionRequirements);

        // Health Check
        HealthCheck mySQLHealthCheck = new HealthCheck();
        mySQLHealthCheck.setComponent(ppdrDatabase);
        mySQLHealthCheck.setDateCreated(new Date());
        mySQLHealthCheck.setLastModified(new Date());
        mySQLHealthCheck.setName("MYSQLHealthCheck");
        mySQLHealthCheck.setInterval(10L);
        mySQLHealthCheck.setArgs("mysqladmin -uroot -pmatilda status");
        mySQLHealthCheck.setHttpURL(null);
        healthCheckDAO.save(mySQLHealthCheck);

        componentDAO.save(ppdrDatabase);

        // ppdr samba
        ppdrSamba = new eu.orchestrator.repository.domain.Component();
        ppdrSamba.setName("PPDRSamba");
        ppdrSamba.setHexID(Util.createRandomHEXString());
        ppdrSamba.setDockerImage("dperson/samba");
        ppdrSamba.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        ppdrSamba.setDockerUsername(MAESTRO);
        ppdrSamba.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        ppdrSamba.setPublicComponent(true);
        ppdrSamba.setDateCreated(new Date());
        ppdrSamba.setLastModified(new Date());
        ppdrSamba.setUser(admin);
        ppdrSamba.setOrganization(admin.getOrganization());
        ppdrSamba.setElasticityController("NONE");
        ppdrSamba.setArchitecture(Architecture.X86.name());
        ppdrSamba.setPlugins(new TreeSet<>(listOfDefaultPlugins));
        componentDAO.save(ppdrSamba);

        // Labels
        ppdrSamba.setLabels(new TreeSet<>(Arrays.asList(labelCustomComponent)));

        // Exposed Interface
        interfaceSambaA = new Interface();
        interfaceSambaA.setDateCreated(new Date());
        interfaceSambaA.setLastModified(new Date());
        interfaceSambaA.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceSambaA.setName("SambaInterface1");
        interfaceSambaA.setPort("139");
        interfaceSambaA.setVna("VNA0");
        interfaceSambaA.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceSambaA.setComponent(ppdrSamba);
        interfaceDAO.save(interfaceSambaA);

        interfaceSambaB = new Interface();
        interfaceSambaB.setDateCreated(new Date());
        interfaceSambaB.setLastModified(new Date());
        interfaceSambaB.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceSambaB.setName("SambaInterface2");
        interfaceSambaB.setPort("445");
        interfaceSambaB.setVna("VNA0");
        interfaceSambaB.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceSambaB.setComponent(ppdrSamba);
        interfaceDAO.save(interfaceSambaB);

        // Minimum Execution Requirements
        Requirement sambaMinimumExecutionRequirements = new Requirement();
        sambaMinimumExecutionRequirements.setComponent(ppdrSamba);
        sambaMinimumExecutionRequirements.setDateCreated(new Date());
        sambaMinimumExecutionRequirements.setLastModified(new Date());
        sambaMinimumExecutionRequirements.setGpuRequired(false);
        sambaMinimumExecutionRequirements.setHypervisorType("ESXI");
        sambaMinimumExecutionRequirements.setRam(2048);
        sambaMinimumExecutionRequirements.setStorage(20);
        sambaMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(sambaMinimumExecutionRequirements);

        // Health Check
        HealthCheck sambaHealthCheck = new HealthCheck();
        sambaHealthCheck.setComponent(ppdrSamba);
        sambaHealthCheck.setDateCreated(new Date());
        sambaHealthCheck.setLastModified(new Date());
        sambaHealthCheck.setName("SAMBAHealthCheck");
        sambaHealthCheck.setInterval(10L);
        sambaHealthCheck.setArgs("ps");
        sambaHealthCheck.setHttpURL(null);
        healthCheckDAO.save(sambaHealthCheck);

        componentDAO.save(ppdrSamba);

        // ppdr PhpDashboard
        ppdrPhpDashboard = new eu.orchestrator.repository.domain.Component();
        ppdrPhpDashboard.setName("PPDRPhpDashboard");
        ppdrPhpDashboard.setHexID(Util.createRandomHEXString());
        ppdrPhpDashboard.setDockerImage("phpdashboard:1.2.0");
        ppdrPhpDashboard.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        ppdrPhpDashboard.setDockerUsername(MAESTRO);
        ppdrPhpDashboard.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        ppdrPhpDashboard.setPublicComponent(true);
        ppdrPhpDashboard.setDateCreated(new Date());
        ppdrPhpDashboard.setLastModified(new Date());
        ppdrPhpDashboard.setUser(admin);
        ppdrPhpDashboard.setOrganization(admin.getOrganization());
        ppdrPhpDashboard.setElasticityController(HORIZONTAL);
        ppdrPhpDashboard.setArchitecture(Architecture.X86.name());
        ppdrPhpDashboard.setPlugins(new TreeSet<>(listOfDefaultPlugins));
        componentDAO.save(ppdrPhpDashboard);

        // Labels
        ppdrPhpDashboard.setLabels(new TreeSet<>(Arrays.asList(labelSQLDB)));

        // Environmental Variables
        EnvironmentalVariable phpDashboardEnvironmentalVariableA = new EnvironmentalVariable();
        phpDashboardEnvironmentalVariableA.setComponent(ppdrPhpDashboard);
        phpDashboardEnvironmentalVariableA.setDateCreated(new Date());
        phpDashboardEnvironmentalVariableA.setLastModified(new Date());
        phpDashboardEnvironmentalVariableA.setKey("SHARE_HOST");
        phpDashboardEnvironmentalVariableA.setValue("@PPDRSamba");
        environmentalVariableDAO.save(phpDashboardEnvironmentalVariableA);

        EnvironmentalVariable phpDashboardEnvironmentalVariableB = new EnvironmentalVariable();
        phpDashboardEnvironmentalVariableB.setComponent(ppdrPhpDashboard);
        phpDashboardEnvironmentalVariableB.setDateCreated(new Date());
        phpDashboardEnvironmentalVariableB.setLastModified(new Date());
        phpDashboardEnvironmentalVariableB.setKey("GIT_USER");
        phpDashboardEnvironmentalVariableB.setValue("imon.matilda");
        environmentalVariableDAO.save(phpDashboardEnvironmentalVariableB);

        EnvironmentalVariable phpDashboardEnvironmentalVariableC = new EnvironmentalVariable();
        phpDashboardEnvironmentalVariableC.setComponent(ppdrPhpDashboard);
        phpDashboardEnvironmentalVariableC.setDateCreated(new Date());
        phpDashboardEnvironmentalVariableC.setLastModified(new Date());
        phpDashboardEnvironmentalVariableC.setKey("GIT_PASS");
        phpDashboardEnvironmentalVariableC.setValue("Imon12345.");
        environmentalVariableDAO.save(phpDashboardEnvironmentalVariableC);

        EnvironmentalVariable phpDashboardEnvironmentalVariableD = new EnvironmentalVariable();
        phpDashboardEnvironmentalVariableD.setComponent(ppdrPhpDashboard);
        phpDashboardEnvironmentalVariableD.setDateCreated(new Date());
        phpDashboardEnvironmentalVariableD.setLastModified(new Date());
        phpDashboardEnvironmentalVariableD.setKey("DB_HOST");
        phpDashboardEnvironmentalVariableD.setValue("@PPDRDatabase");
        environmentalVariableDAO.save(phpDashboardEnvironmentalVariableD);

        EnvironmentalVariable phpDashboardEnvironmentalVariableE = new EnvironmentalVariable();
        phpDashboardEnvironmentalVariableE.setComponent(ppdrPhpDashboard);
        phpDashboardEnvironmentalVariableE.setDateCreated(new Date());
        phpDashboardEnvironmentalVariableE.setLastModified(new Date());
        phpDashboardEnvironmentalVariableE.setKey("DB_ROOT_USER");
        phpDashboardEnvironmentalVariableE.setValue("root");
        environmentalVariableDAO.save(phpDashboardEnvironmentalVariableE);

        EnvironmentalVariable phpDashboardEnvironmentalVariableF = new EnvironmentalVariable();
        phpDashboardEnvironmentalVariableF.setComponent(ppdrPhpDashboard);
        phpDashboardEnvironmentalVariableF.setDateCreated(new Date());
        phpDashboardEnvironmentalVariableF.setLastModified(new Date());
        phpDashboardEnvironmentalVariableF.setKey("DB_ROOT_PASS");
        phpDashboardEnvironmentalVariableF.setValue(MATILDA);
        environmentalVariableDAO.save(phpDashboardEnvironmentalVariableF);

        EnvironmentalVariable phpDashboardEnvironmentalVariableG = new EnvironmentalVariable();
        phpDashboardEnvironmentalVariableG.setComponent(ppdrPhpDashboard);
        phpDashboardEnvironmentalVariableG.setDateCreated(new Date());
        phpDashboardEnvironmentalVariableG.setLastModified(new Date());
        phpDashboardEnvironmentalVariableG.setKey("DB_NAME");
        phpDashboardEnvironmentalVariableG.setValue("imon");
        environmentalVariableDAO.save(phpDashboardEnvironmentalVariableG);

        EnvironmentalVariable phpDashboardEnvironmentalVariableH = new EnvironmentalVariable();
        phpDashboardEnvironmentalVariableH.setComponent(ppdrPhpDashboard);
        phpDashboardEnvironmentalVariableH.setDateCreated(new Date());
        phpDashboardEnvironmentalVariableH.setLastModified(new Date());
        phpDashboardEnvironmentalVariableH.setKey("DB_USER");
        phpDashboardEnvironmentalVariableH.setValue("imon");
        environmentalVariableDAO.save(phpDashboardEnvironmentalVariableH);

        EnvironmentalVariable phpDashboardEnvironmentalVariableI = new EnvironmentalVariable();
        phpDashboardEnvironmentalVariableI.setComponent(ppdrPhpDashboard);
        phpDashboardEnvironmentalVariableI.setDateCreated(new Date());
        phpDashboardEnvironmentalVariableI.setLastModified(new Date());
        phpDashboardEnvironmentalVariableI.setKey("DB_PASS");
        phpDashboardEnvironmentalVariableI.setValue("imon");
        environmentalVariableDAO.save(phpDashboardEnvironmentalVariableI);

        // Required Interface
        phpDashboardGraphLinkSQLInterface = new GraphLink();
        phpDashboardGraphLinkSQLInterface.setInterfaceObj(interfaceSQLppdr);
        phpDashboardGraphLinkSQLInterface.setComponent(ppdrPhpDashboard);
        phpDashboardGraphLinkSQLInterface.setDateCreated(new Date());
        phpDashboardGraphLinkSQLInterface.setLastModified(new Date());
        graphLinkDAO.save(phpDashboardGraphLinkSQLInterface);
        phpDashboardGraphLinkSambaInterface = new GraphLink();
        phpDashboardGraphLinkSambaInterface.setInterfaceObj(interfaceSambaA);
        phpDashboardGraphLinkSambaInterface.setComponent(ppdrPhpDashboard);
        phpDashboardGraphLinkSambaInterface.setDateCreated(new Date());
        phpDashboardGraphLinkSambaInterface.setLastModified(new Date());
        graphLinkDAO.save(phpDashboardGraphLinkSambaInterface);

        // Exposed Interface
        interfacePhpDashboard = new Interface();
        interfacePhpDashboard.setDateCreated(new Date());
        interfacePhpDashboard.setLastModified(new Date());
        interfacePhpDashboard.setInterfaceType(Interface.InterfaceType.ACCESS.name());
        interfacePhpDashboard.setName("PhpDashboardInterface");
        interfacePhpDashboard.setPort("80");
        interfacePhpDashboard.setVna("VNA0");
        interfacePhpDashboard.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfacePhpDashboard.setComponent(ppdrPhpDashboard);
        interfaceDAO.save(interfacePhpDashboard);

        // Minimum Execution Requirements
        Requirement phpDashboardMinimumExecutionRequirements = new Requirement();
        phpDashboardMinimumExecutionRequirements.setComponent(ppdrPhpDashboard);
        phpDashboardMinimumExecutionRequirements.setDateCreated(new Date());
        phpDashboardMinimumExecutionRequirements.setLastModified(new Date());
        phpDashboardMinimumExecutionRequirements.setGpuRequired(false);
        phpDashboardMinimumExecutionRequirements.setHypervisorType("ESXI");
        phpDashboardMinimumExecutionRequirements.setRam(2048);
        phpDashboardMinimumExecutionRequirements.setStorage(20);
        phpDashboardMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(phpDashboardMinimumExecutionRequirements);

        // Health Check
        HealthCheck phpDashboardHealthCheck = new HealthCheck();
        phpDashboardHealthCheck.setComponent(ppdrPhpDashboard);
        phpDashboardHealthCheck.setDateCreated(new Date());
        phpDashboardHealthCheck.setLastModified(new Date());
        phpDashboardHealthCheck.setName("phpDashBoardHealthCheck");
        phpDashboardHealthCheck.setInterval(10L);
        phpDashboardHealthCheck.setArgs(null);
        phpDashboardHealthCheck.setHttpURL(LOCALHOST);
        healthCheckDAO.save(phpDashboardHealthCheck);

        componentDAO.save(ppdrPhpDashboard);

        // Application
        ppdrApplication = new eu.orchestrator.repository.domain.Application();
        ppdrApplication.setName("PPDR");
        ppdrApplication.setHexID(Util.createRandomHEXString());
        ppdrApplication.setDateCreated(new Date());
        ppdrApplication.setUser(admin);
        ppdrApplication.setOrganization(admin.getOrganization());
        ppdrApplication.setPublicApplication(true);
        ppdrApplication.setLastModified(new Date());
        applicationDAO.save(ppdrApplication);

        // Component Nodes
        ComponentNode database = new ComponentNode();
        database.setName("database");
        database.setHexID(Util.createRandomHEXString());
        database.setComponent(ppdrDatabase);
        database.setDateCreated(new Date());
        database.setLastModified(new Date());
        database.setApplication(ppdrApplication);
        componentNodeDAO.save(database);

        ComponentNode fileSamba = new ComponentNode();
        fileSamba.setName("fileSamba");
        fileSamba.setHexID(Util.createRandomHEXString());
        fileSamba.setComponent(ppdrSamba);
        fileSamba.setDateCreated(new Date());
        fileSamba.setLastModified(new Date());
        fileSamba.setApplication(ppdrApplication);
        componentNodeDAO.save(fileSamba);

        ComponentNode phpDashboard = new ComponentNode();
        phpDashboard.setName("phpDashboard");
        phpDashboard.setHexID(Util.createRandomHEXString());
        phpDashboard.setComponent(ppdrPhpDashboard);
        phpDashboard.setDateCreated(new Date());
        phpDashboard.setLastModified(new Date());
        phpDashboard.setApplication(ppdrApplication);
        componentNodeDAO.save(phpDashboard);

        // Graph Link Nodes
        GraphLinkNode ppdrGraphLinkNodePhpDashboardToDatabase = new GraphLinkNode();
        ppdrGraphLinkNodePhpDashboardToDatabase.setComponentNodeFrom(phpDashboard);
        ppdrGraphLinkNodePhpDashboardToDatabase.setComponentNodeTo(database);
        ppdrGraphLinkNodePhpDashboardToDatabase.setGraphLink(phpDashboardGraphLinkSQLInterface);
        ppdrGraphLinkNodePhpDashboardToDatabase.setApplication(ppdrApplication);
        ppdrGraphLinkNodePhpDashboardToDatabase.setDateCreated(new Date());
        ppdrGraphLinkNodePhpDashboardToDatabase.setLastModified(new Date());
        graphLinkNodeDAO.save(ppdrGraphLinkNodePhpDashboardToDatabase);

        GraphLinkNode ppdrGraphLinkNodePhpDashboardToSamba = new GraphLinkNode();
        ppdrGraphLinkNodePhpDashboardToSamba.setComponentNodeFrom(phpDashboard);
        ppdrGraphLinkNodePhpDashboardToSamba.setComponentNodeTo(fileSamba);
        ppdrGraphLinkNodePhpDashboardToSamba.setGraphLink(phpDashboardGraphLinkSambaInterface);
        ppdrGraphLinkNodePhpDashboardToSamba.setApplication(ppdrApplication);
        ppdrGraphLinkNodePhpDashboardToSamba.setDateCreated(new Date());
        ppdrGraphLinkNodePhpDashboardToSamba.setLastModified(new Date());
        graphLinkNodeDAO.save(ppdrGraphLinkNodePhpDashboardToSamba);

        logger.info("PPDR have been added successfully!");

    }

    private void initializePilotOlistic() {

        logger.info("Olistic initialization has started...");

        Optional<Plugin> mongoDBPlugin = pluginDAO.findByNameAndModuleName(PYTHON_D, MONGODB_CONST);

        // Mongo olistic
        olisticMongo = new eu.orchestrator.repository.domain.Component();
        olisticMongo.setName("olisticMongo");
        olisticMongo.setHexID(Util.createRandomHEXString());
        olisticMongo.setDockerImage("mongo:3.4");
        olisticMongo.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        olisticMongo.setPublicComponent(true);
        olisticMongo.setDateCreated(new Date());
        olisticMongo.setLastModified(new Date());
        olisticMongo.setUser(admin);
        olisticMongo.setOrganization(organizationAdmin);
        olisticMongo.setElasticityController("NONE");

        SortedSet<Plugin> mongoDBPlugins = new TreeSet<>();
        if (mongoDBPlugin.isPresent()) {
            mongoDBPlugins.add(mongoDBPlugin.get());
        }
        mongoDBPlugins.addAll(listOfDefaultPlugins);
        olisticMongo.setPlugins(mongoDBPlugins);

        componentDAO.save(olisticMongo);

        // Labels
        olisticMongo.setLabels(new TreeSet<>(Arrays.asList(labelNoSQL)));

        // Environmental Variables
        EnvironmentalVariable mongoDBEnvironmentalVariableA = new EnvironmentalVariable();
        mongoDBEnvironmentalVariableA.setComponent(olisticMongo);
        mongoDBEnvironmentalVariableA.setDateCreated(new Date());
        mongoDBEnvironmentalVariableA.setLastModified(new Date());
        mongoDBEnvironmentalVariableA.setKey("MONGO_INITDB_ROOT_USERNAME");
        mongoDBEnvironmentalVariableA.setValue("KualaLumpur");
        environmentalVariableDAO.save(mongoDBEnvironmentalVariableA);

        EnvironmentalVariable mongoDBEnvironmentalVariableB = new EnvironmentalVariable();
        mongoDBEnvironmentalVariableB.setComponent(olisticMongo);
        mongoDBEnvironmentalVariableB.setDateCreated(new Date());
        mongoDBEnvironmentalVariableB.setLastModified(new Date());
        mongoDBEnvironmentalVariableB.setKey("MONGO_INITDB_ROOT_PASSWORD");
        mongoDBEnvironmentalVariableB.setValue("KualaLumpur123!");
        environmentalVariableDAO.save(mongoDBEnvironmentalVariableB);

        // Exposed Interface
        interfaceNoSQLOlistic = new Interface();
        interfaceNoSQLOlistic.setDateCreated(new Date());
        interfaceNoSQLOlistic.setLastModified(new Date());
        interfaceNoSQLOlistic.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceNoSQLOlistic.setName("mongoDBNoSQLInterfaceOlistic");
        interfaceNoSQLOlistic.setVna("VNA0");
        interfaceNoSQLOlistic.setPort(PORT);
        interfaceNoSQLOlistic.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceNoSQLOlistic.setComponent(olisticMongo);
        interfaceDAO.save(interfaceNoSQLOlistic);

        // Minimum Execution Requirements
        Requirement mongoDBMinimumExecutionRequirements = new Requirement();
        mongoDBMinimumExecutionRequirements.setComponent(olisticMongo);
        mongoDBMinimumExecutionRequirements.setDateCreated(new Date());
        mongoDBMinimumExecutionRequirements.setLastModified(new Date());
        mongoDBMinimumExecutionRequirements.setGpuRequired(false);
        mongoDBMinimumExecutionRequirements.setHypervisorType("ESXI");
        mongoDBMinimumExecutionRequirements.setRam(2048);
        mongoDBMinimumExecutionRequirements.setStorage(20);
        mongoDBMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(mongoDBMinimumExecutionRequirements);

        // Health Check
        HealthCheck mongoDBHealthCheck = new HealthCheck();
        mongoDBHealthCheck.setComponent(olisticMongo);
        mongoDBHealthCheck.setDateCreated(new Date());
        mongoDBHealthCheck.setLastModified(new Date());
        mongoDBHealthCheck.setName("MongoOlisticHealthCheck");
        mongoDBHealthCheck.setInterval(10L);
        mongoDBHealthCheck.setArgs("mongo");
        mongoDBHealthCheck.setHttpURL(null);
        healthCheckDAO.save(mongoDBHealthCheck);

        componentDAO.save(olisticMongo);

        // openvas olistic
        olisticOpenvas = new eu.orchestrator.repository.domain.Component();
        olisticOpenvas.setName("olisticOpenvas");
        olisticOpenvas.setHexID(Util.createRandomHEXString());
        olisticOpenvas.setDockerImage("mikesplain/openvas:9");
        olisticOpenvas.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        olisticOpenvas.setPublicComponent(true);
        olisticOpenvas.setDateCreated(new Date());
        olisticOpenvas.setLastModified(new Date());
        olisticOpenvas.setUser(admin);
        olisticOpenvas.setOrganization(organizationAdmin);
        olisticOpenvas.setElasticityController("NONE");
        olisticOpenvas.setPlugins(new TreeSet<>(listOfDefaultPlugins));

        componentDAO.save(olisticOpenvas);

        // Labels
        olisticOpenvas.setLabels(new TreeSet<>(Arrays.asList(labelNoSQL)));

        // Environmental Variables
        EnvironmentalVariable openvasEnvironmentalVariableA = new EnvironmentalVariable();
        openvasEnvironmentalVariableA.setComponent(olisticOpenvas);
        openvasEnvironmentalVariableA.setDateCreated(new Date());
        openvasEnvironmentalVariableA.setLastModified(new Date());
        openvasEnvironmentalVariableA.setKey("PUBLIC_HOSTNAME");
        openvasEnvironmentalVariableA.setValue("@IPV4_PRIVATE");
        environmentalVariableDAO.save(openvasEnvironmentalVariableA);

        // Exposed Interface
        interfaceOpenVasAOlistic = new Interface();
        interfaceOpenVasAOlistic.setDateCreated(new Date());
        interfaceOpenVasAOlistic.setLastModified(new Date());
        interfaceOpenVasAOlistic.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceOpenVasAOlistic.setName("interfaceOpenVasAOlisticA");
        interfaceOpenVasAOlistic.setVna("VNA0");
        interfaceOpenVasAOlistic.setPort("443");
        interfaceOpenVasAOlistic.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceOpenVasAOlistic.setComponent(olisticOpenvas);
        interfaceDAO.save(interfaceOpenVasAOlistic);

        interfaceOpenVasBOlistic = new Interface();
        interfaceOpenVasBOlistic.setDateCreated(new Date());
        interfaceOpenVasBOlistic.setLastModified(new Date());
        interfaceOpenVasBOlistic.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceOpenVasBOlistic.setName("interfaceOpenVasAOlisticB");
        interfaceOpenVasBOlistic.setVna("VNA0");
        interfaceOpenVasBOlistic.setPort("9390");
        interfaceOpenVasBOlistic.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceOpenVasBOlistic.setComponent(olisticOpenvas);
        interfaceDAO.save(interfaceOpenVasBOlistic);

        // Minimum Execution Requirements
        Requirement openvasBMinimumExecutionRequirements = new Requirement();
        openvasBMinimumExecutionRequirements.setComponent(olisticOpenvas);
        openvasBMinimumExecutionRequirements.setDateCreated(new Date());
        openvasBMinimumExecutionRequirements.setLastModified(new Date());
        openvasBMinimumExecutionRequirements.setGpuRequired(false);
        openvasBMinimumExecutionRequirements.setHypervisorType("ESXI");
        openvasBMinimumExecutionRequirements.setRam(2048);
        openvasBMinimumExecutionRequirements.setStorage(20);
        openvasBMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(openvasBMinimumExecutionRequirements);

        // Health Check
        HealthCheck openvasHealthCheck = new HealthCheck();
        openvasHealthCheck.setComponent(olisticOpenvas);
        openvasHealthCheck.setDateCreated(new Date());
        openvasHealthCheck.setLastModified(new Date());
        openvasHealthCheck.setName("openvasOlisticHealthCheck");
        openvasHealthCheck.setInterval(10L);
        openvasHealthCheck.setArgs("ps");
        openvasHealthCheck.setHttpURL(null);
        healthCheckDAO.save(openvasHealthCheck);

        componentDAO.save(olisticOpenvas);

        // mysql olistic
        olisticMysql = new eu.orchestrator.repository.domain.Component();
        olisticMysql.setName("olisticMysql");
        olisticMysql.setHexID(Util.createRandomHEXString());
        olisticMysql.setDockerImage("olistic_database:1.0.0");
        olisticMysql.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        olisticMysql.setDockerUsername(MAESTRO);
        olisticMysql.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        olisticMysql.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        olisticMysql.setPublicComponent(true);
        olisticMysql.setDateCreated(new Date());
        olisticMysql.setLastModified(new Date());
        olisticMysql.setUser(admin);
        olisticMysql.setOrganization(organizationAdmin);
        olisticMysql.setElasticityController("NONE");
        olisticMysql.setPlugins(new TreeSet<>(listOfDefaultPlugins));

        componentDAO.save(olisticMysql);

        // Labels
        olisticMysql.setLabels(new TreeSet<>(Arrays.asList(labelSQLDB)));

        // Exposed Interface
        interfaceSQLOlistic = new Interface();
        interfaceSQLOlistic.setDateCreated(new Date());
        interfaceSQLOlistic.setLastModified(new Date());
        interfaceSQLOlistic.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceSQLOlistic.setName("DBSQLInterfaceOlistic");
        interfaceSQLOlistic.setVna("VNA0");
        interfaceSQLOlistic.setPort("3306");
        interfaceSQLOlistic.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceSQLOlistic.setComponent(olisticMysql);
        interfaceDAO.save(interfaceNoSQLOlistic);

        // Minimum Execution Requirements
        Requirement mysqlDBMinimumExecutionRequirements = new Requirement();
        mysqlDBMinimumExecutionRequirements.setComponent(olisticMysql);
        mysqlDBMinimumExecutionRequirements.setDateCreated(new Date());
        mysqlDBMinimumExecutionRequirements.setLastModified(new Date());
        mysqlDBMinimumExecutionRequirements.setGpuRequired(false);
        mysqlDBMinimumExecutionRequirements.setHypervisorType("ESXI");
        mysqlDBMinimumExecutionRequirements.setRam(2048);
        mysqlDBMinimumExecutionRequirements.setStorage(20);
        mysqlDBMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(mysqlDBMinimumExecutionRequirements);

        // Health Check
        HealthCheck mysqlDBBHealthCheck = new HealthCheck();
        mysqlDBBHealthCheck.setComponent(olisticMysql);
        mysqlDBBHealthCheck.setDateCreated(new Date());
        mysqlDBBHealthCheck.setLastModified(new Date());
        mysqlDBBHealthCheck.setName("mysqlDBOlisticHealthCheck");
        mysqlDBBHealthCheck.setInterval(10L);
        mysqlDBBHealthCheck.setArgs("mysqladmin -uroot -ph7dK_2Qse!olst status");
        mysqlDBBHealthCheck.setHttpURL(null);
        healthCheckDAO.save(mysqlDBBHealthCheck);

        componentDAO.save(olisticMysql);

        // ppdr PhpDashboard
        olisticApp = new eu.orchestrator.repository.domain.Component();
        olisticApp.setName("olisticApp");
        olisticApp.setHexID(Util.createRandomHEXString());
        olisticApp.setDockerImage("olistic_app:1.1.0");
        olisticApp.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        olisticApp.setDockerUsername(MAESTRO);
        olisticApp.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        olisticApp.setPublicComponent(true);
        olisticApp.setDateCreated(new Date());
        olisticApp.setLastModified(new Date());
        olisticApp.setUser(admin);
        olisticApp.setOrganization(admin.getOrganization());
        olisticApp.setElasticityController(HORIZONTAL);
        olisticApp.setArchitecture(Architecture.X86.name());
        olisticApp.setPlugins(new TreeSet<>(listOfDefaultPlugins));
        componentDAO.save(olisticApp);

        // Labels
        olisticApp.setLabels(new TreeSet<>());

        // Environmental Variables
        EnvironmentalVariable olisticAppEnvironmentalVariableA = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableA.setComponent(olisticApp);
        olisticAppEnvironmentalVariableA.setDateCreated(new Date());
        olisticAppEnvironmentalVariableA.setLastModified(new Date());
        olisticAppEnvironmentalVariableA.setKey("olistic.management.riskassessment.auto");
        olisticAppEnvironmentalVariableA.setValue("false");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableA);

        EnvironmentalVariable olisticAppEnvironmentalVariableB = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableB.setComponent(olisticApp);
        olisticAppEnvironmentalVariableB.setDateCreated(new Date());
        olisticAppEnvironmentalVariableB.setLastModified(new Date());
        olisticAppEnvironmentalVariableB.setKey("olistic.management.vulnerability.auto");
        olisticAppEnvironmentalVariableB.setValue("false");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableB);

        EnvironmentalVariable olisticAppEnvironmentalVariableC = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableC.setComponent(olisticApp);
        olisticAppEnvironmentalVariableC.setDateCreated(new Date());
        olisticAppEnvironmentalVariableC.setLastModified(new Date());
        olisticAppEnvironmentalVariableC.setKey("olistic.management.vulnerability.openvas.pass");
        olisticAppEnvironmentalVariableC.setValue(ADMIN_LOWER);
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableC);

        EnvironmentalVariable olisticAppEnvironmentalVariableD = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableD.setComponent(olisticApp);
        olisticAppEnvironmentalVariableD.setDateCreated(new Date());
        olisticAppEnvironmentalVariableD.setLastModified(new Date());
        olisticAppEnvironmentalVariableD.setKey("olistic.management.vulnerability.openvas.user");
        olisticAppEnvironmentalVariableD.setValue(ADMIN_LOWER);
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableD);

        EnvironmentalVariable olisticAppEnvironmentalVariableE = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableE.setComponent(olisticApp);
        olisticAppEnvironmentalVariableE.setDateCreated(new Date());
        olisticAppEnvironmentalVariableE.setLastModified(new Date());
        olisticAppEnvironmentalVariableE.setKey("olistic.management.vulnerability.openvas.port");
        olisticAppEnvironmentalVariableE.setValue("9390");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableE);

        EnvironmentalVariable olisticAppEnvironmentalVariableF = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableF.setComponent(olisticApp);
        olisticAppEnvironmentalVariableF.setDateCreated(new Date());
        olisticAppEnvironmentalVariableF.setLastModified(new Date());
        olisticAppEnvironmentalVariableF.setKey("olistic.management.vulnerability.openvas.host");
        olisticAppEnvironmentalVariableF.setValue("@olisticOpenvas");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableF);

        EnvironmentalVariable olisticAppEnvironmentalVariableJ = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableJ.setComponent(olisticApp);
        olisticAppEnvironmentalVariableJ.setDateCreated(new Date());
        olisticAppEnvironmentalVariableJ.setLastModified(new Date());
        olisticAppEnvironmentalVariableJ.setKey("spring.data.mongodb.password");
        olisticAppEnvironmentalVariableJ.setValue("KualaLumpur123!");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableJ);

        EnvironmentalVariable olisticAppEnvironmentalVariableK = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableK.setComponent(olisticApp);
        olisticAppEnvironmentalVariableK.setDateCreated(new Date());
        olisticAppEnvironmentalVariableK.setLastModified(new Date());
        olisticAppEnvironmentalVariableK.setKey("spring.data.mongodb.username");
        olisticAppEnvironmentalVariableK.setValue("KualaLumpur");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableK);

        EnvironmentalVariable olisticAppEnvironmentalVariableH = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableH.setComponent(olisticApp);
        olisticAppEnvironmentalVariableH.setDateCreated(new Date());
        olisticAppEnvironmentalVariableH.setLastModified(new Date());
        olisticAppEnvironmentalVariableH.setKey("spring.data.mongodb.port");
        olisticAppEnvironmentalVariableH.setValue(PORT);
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableH);

        EnvironmentalVariable olisticAppEnvironmentalVariableM = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableM.setComponent(olisticApp);
        olisticAppEnvironmentalVariableM.setDateCreated(new Date());
        olisticAppEnvironmentalVariableM.setLastModified(new Date());
        olisticAppEnvironmentalVariableM.setKey("spring.data.mongodb.host");
        olisticAppEnvironmentalVariableM.setValue("@olisticMongo");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableM);

        EnvironmentalVariable olisticAppEnvironmentalVariableN = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableN.setComponent(olisticApp);
        olisticAppEnvironmentalVariableN.setDateCreated(new Date());
        olisticAppEnvironmentalVariableN.setLastModified(new Date());
        olisticAppEnvironmentalVariableN.setKey("spring.data.mongodb.authentication-database");
        olisticAppEnvironmentalVariableN.setValue(ADMIN_LOWER);
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableN);

        EnvironmentalVariable olisticAppEnvironmentalVariableO = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableO.setComponent(olisticApp);
        olisticAppEnvironmentalVariableO.setDateCreated(new Date());
        olisticAppEnvironmentalVariableO.setLastModified(new Date());
        olisticAppEnvironmentalVariableO.setKey("spring.data.mongodb.database");
        olisticAppEnvironmentalVariableO.setValue("olistic");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableO);

        EnvironmentalVariable olisticAppEnvironmentalVariableP = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableP.setComponent(olisticApp);
        olisticAppEnvironmentalVariableP.setDateCreated(new Date());
        olisticAppEnvironmentalVariableP.setLastModified(new Date());
        olisticAppEnvironmentalVariableP.setKey("olistic.relational.password");
        olisticAppEnvironmentalVariableP.setValue("Fs_77dHywdLs!");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableP);

        EnvironmentalVariable olisticAppEnvironmentalVariableQ = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableQ.setComponent(olisticApp);
        olisticAppEnvironmentalVariableQ.setDateCreated(new Date());
        olisticAppEnvironmentalVariableQ.setLastModified(new Date());
        olisticAppEnvironmentalVariableQ.setKey("olistic.relational.username");
        olisticAppEnvironmentalVariableQ.setValue("olisDic78ss");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableQ);

        EnvironmentalVariable olisticAppEnvironmentalVariableT = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableT.setComponent(olisticApp);
        olisticAppEnvironmentalVariableT.setDateCreated(new Date());
        olisticAppEnvironmentalVariableT.setLastModified(new Date());
        olisticAppEnvironmentalVariableT.setKey("olistic.relational.port");
        olisticAppEnvironmentalVariableT.setValue("3306");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableT);

        EnvironmentalVariable olisticAppEnvironmentalVariableX = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableX.setComponent(olisticApp);
        olisticAppEnvironmentalVariableX.setDateCreated(new Date());
        olisticAppEnvironmentalVariableX.setLastModified(new Date());
        olisticAppEnvironmentalVariableX.setKey("olistic.relational.host");
        olisticAppEnvironmentalVariableX.setValue("@olisticMysql");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableX);

        EnvironmentalVariable olisticAppEnvironmentalVariableZ = new EnvironmentalVariable();
        olisticAppEnvironmentalVariableZ.setComponent(olisticApp);
        olisticAppEnvironmentalVariableZ.setDateCreated(new Date());
        olisticAppEnvironmentalVariableZ.setLastModified(new Date());
        olisticAppEnvironmentalVariableZ.setKey("olistic.startup.inventory.asset.location");
        olisticAppEnvironmentalVariableZ.setValue("/opt/olistic/initial_hosts");
        environmentalVariableDAO.save(olisticAppEnvironmentalVariableZ);

        // Required Interface
        olisticAppGraphLinkSQLInerface = new GraphLink();
        olisticAppGraphLinkSQLInerface.setInterfaceObj(interfaceSQLOlistic);
        olisticAppGraphLinkSQLInerface.setComponent(olisticApp);
        olisticAppGraphLinkSQLInerface.setDateCreated(new Date());
        olisticAppGraphLinkSQLInerface.setLastModified(new Date());
        graphLinkDAO.save(olisticAppGraphLinkSQLInerface);

        olisticAppGraphLinkNoSQLInerface = new GraphLink();
        olisticAppGraphLinkNoSQLInerface.setInterfaceObj(interfaceNoSQLOlistic);
        olisticAppGraphLinkNoSQLInerface.setComponent(olisticApp);
        olisticAppGraphLinkNoSQLInerface.setDateCreated(new Date());
        olisticAppGraphLinkNoSQLInerface.setLastModified(new Date());
        graphLinkDAO.save(olisticAppGraphLinkNoSQLInerface);

        olisticAppGraphLinkOpenvasInerface = new GraphLink();
        olisticAppGraphLinkOpenvasInerface.setInterfaceObj(interfaceOpenVasAOlistic);
        olisticAppGraphLinkOpenvasInerface.setComponent(olisticApp);
        olisticAppGraphLinkOpenvasInerface.setDateCreated(new Date());
        olisticAppGraphLinkOpenvasInerface.setLastModified(new Date());
        graphLinkDAO.save(olisticAppGraphLinkOpenvasInerface);

        // Exposed Interface
        interfaceAppOlistic = new Interface();
        interfaceAppOlistic.setDateCreated(new Date());
        interfaceAppOlistic.setLastModified(new Date());
        interfaceAppOlistic.setInterfaceType(Interface.InterfaceType.ACCESS.name());
        interfaceAppOlistic.setName("interfaceAppOlistic");
        interfaceAppOlistic.setPort("8080");
        interfaceAppOlistic.setVna("VNA0");
        interfaceAppOlistic.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceAppOlistic.setComponent(olisticApp);
        interfaceDAO.save(interfaceAppOlistic);

        // Minimum Execution Requirements
        Requirement olisticAppMinimumExecutionRequirements = new Requirement();
        olisticAppMinimumExecutionRequirements.setComponent(olisticApp);
        olisticAppMinimumExecutionRequirements.setDateCreated(new Date());
        olisticAppMinimumExecutionRequirements.setLastModified(new Date());
        olisticAppMinimumExecutionRequirements.setGpuRequired(false);
        olisticAppMinimumExecutionRequirements.setHypervisorType("ESXI");
        olisticAppMinimumExecutionRequirements.setRam(2048);
        olisticAppMinimumExecutionRequirements.setStorage(20);
        olisticAppMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(olisticAppMinimumExecutionRequirements);

        // Health Check
        HealthCheck olisticAppHealthCheck = new HealthCheck();
        olisticAppHealthCheck.setComponent(olisticApp);
        olisticAppHealthCheck.setDateCreated(new Date());
        olisticAppHealthCheck.setLastModified(new Date());
        olisticAppHealthCheck.setName("olisticAppHealthCheck");
        olisticAppHealthCheck.setInterval(10L);
        olisticAppHealthCheck.setArgs(null);
        olisticAppHealthCheck.setHttpURL("http://localhost:8080/health");
        healthCheckDAO.save(olisticAppHealthCheck);

        componentDAO.save(olisticApp);

        // Application
        olisticApplication = new eu.orchestrator.repository.domain.Application();
        olisticApplication.setName("olistic");
        olisticApplication.setHexID(Util.createRandomHEXString());
        olisticApplication.setDateCreated(new Date());
        olisticApplication.setUser(admin);
        olisticApplication.setOrganization(admin.getOrganization());
        olisticApplication.setPublicApplication(true);
        olisticApplication.setLastModified(new Date());
        applicationDAO.save(olisticApplication);

        // Component Nodes
        ComponentNode olisticMysqldatabase = new ComponentNode();
        olisticMysqldatabase.setName("olisticMysql");
        olisticMysqldatabase.setHexID(Util.createRandomHEXString());
        olisticMysqldatabase.setComponent(olisticMysql);
        olisticMysqldatabase.setDateCreated(new Date());
        olisticMysqldatabase.setLastModified(new Date());
        olisticMysqldatabase.setApplication(olisticApplication);
        componentNodeDAO.save(olisticMysqldatabase);

        ComponentNode olisticMongodatabase = new ComponentNode();
        olisticMongodatabase.setName("olisticMongo");
        olisticMongodatabase.setHexID(Util.createRandomHEXString());
        olisticMongodatabase.setComponent(olisticMongo);
        olisticMongodatabase.setDateCreated(new Date());
        olisticMongodatabase.setLastModified(new Date());
        olisticMongodatabase.setApplication(olisticApplication);
        componentNodeDAO.save(olisticMongodatabase);

        ComponentNode olisticOpenvasComponenNode = new ComponentNode();
        olisticOpenvasComponenNode.setName("olisticOpenvas");
        olisticOpenvasComponenNode.setHexID(Util.createRandomHEXString());
        olisticOpenvasComponenNode.setComponent(olisticOpenvas);
        olisticOpenvasComponenNode.setDateCreated(new Date());
        olisticOpenvasComponenNode.setLastModified(new Date());
        olisticOpenvasComponenNode.setApplication(olisticApplication);
        componentNodeDAO.save(olisticOpenvasComponenNode);

        ComponentNode olisticAppComponenNode = new ComponentNode();
        olisticAppComponenNode.setName("olisticApp");
        olisticAppComponenNode.setHexID(Util.createRandomHEXString());
        olisticAppComponenNode.setComponent(olisticApp);
        olisticAppComponenNode.setDateCreated(new Date());
        olisticAppComponenNode.setLastModified(new Date());
        olisticAppComponenNode.setApplication(olisticApplication);
        componentNodeDAO.save(olisticAppComponenNode);

        // Graph Link Nodes
        GraphLinkNode olisticGraphLinkNodeAppToMysql = new GraphLinkNode();
        olisticGraphLinkNodeAppToMysql.setComponentNodeFrom(olisticAppComponenNode);
        olisticGraphLinkNodeAppToMysql.setComponentNodeTo(olisticMysqldatabase);
        olisticGraphLinkNodeAppToMysql.setGraphLink(olisticAppGraphLinkSQLInerface);
        olisticGraphLinkNodeAppToMysql.setApplication(olisticApplication);
        olisticGraphLinkNodeAppToMysql.setDateCreated(new Date());
        olisticGraphLinkNodeAppToMysql.setLastModified(new Date());
        graphLinkNodeDAO.save(olisticGraphLinkNodeAppToMysql);

        GraphLinkNode olisticGraphLinkNodeAppToMongo = new GraphLinkNode();
        olisticGraphLinkNodeAppToMongo.setComponentNodeFrom(olisticAppComponenNode);
        olisticGraphLinkNodeAppToMongo.setComponentNodeTo(olisticMongodatabase);
        olisticGraphLinkNodeAppToMongo.setGraphLink(olisticAppGraphLinkNoSQLInerface);
        olisticGraphLinkNodeAppToMongo.setApplication(olisticApplication);
        olisticGraphLinkNodeAppToMongo.setDateCreated(new Date());
        olisticGraphLinkNodeAppToMongo.setLastModified(new Date());
        graphLinkNodeDAO.save(olisticGraphLinkNodeAppToMongo);

        GraphLinkNode olisticGraphLinkNodeAppToOpenvas = new GraphLinkNode();
        olisticGraphLinkNodeAppToOpenvas.setComponentNodeFrom(olisticAppComponenNode);
        olisticGraphLinkNodeAppToOpenvas.setComponentNodeTo(olisticOpenvasComponenNode);
        olisticGraphLinkNodeAppToOpenvas.setGraphLink(olisticAppGraphLinkOpenvasInerface);
        olisticGraphLinkNodeAppToOpenvas.setApplication(olisticApplication);
        olisticGraphLinkNodeAppToOpenvas.setDateCreated(new Date());
        olisticGraphLinkNodeAppToOpenvas.setLastModified(new Date());
        graphLinkNodeDAO.save(olisticGraphLinkNodeAppToOpenvas);

        logger.info("Olistic have been added successfully!");
    }

    private void initializePilotFunction() {

        logger.info("Function Application Pilot initialization has started...");

        // Labels
        labelFunction = new Label();
        labelFunction.setName("Function");
        labelFunction.setDateCreated(new Date());
        labelFunction.setLastModified(new Date());
        labelDAO.save(labelFunction);

        // Sum Function
        sumFunction = new eu.orchestrator.repository.domain.Component();
        sumFunction.setName("SumFunction");
        sumFunction.setHexID(Util.createRandomHEXString());
        sumFunction.setDockerImage("lambda_proxy_app/sum:1.0.0");
        sumFunction.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        sumFunction.setDockerUsername(MAESTRO);
        sumFunction.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        sumFunction.setPublicComponent(true);
        sumFunction.setDateCreated(new Date());
        sumFunction.setLastModified(new Date());
        sumFunction.setUser(admin);
        sumFunction.setOrganization(admin.getOrganization());
        sumFunction.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        sumFunction.setElasticityController(LAMBDA_FUNCTION);
        sumFunction.setElasticityControllerMode("HTTP");
        sumFunction.setPlugins(new TreeSet<>(listOfDefaultPlugins));
        componentDAO.save(sumFunction);

        // Labels
        sumFunction.setLabels(new TreeSet<>(Arrays.asList(labelFunction)));

        // Environmental Variables
        EnvironmentalVariable sumFunctionEnvironmentalVariableA = new EnvironmentalVariable();
        sumFunctionEnvironmentalVariableA.setComponent(sumFunction);
        sumFunctionEnvironmentalVariableA.setDateCreated(new Date());
        sumFunctionEnvironmentalVariableA.setLastModified(new Date());
        sumFunctionEnvironmentalVariableA.setKey(SERVER_PORT);
        sumFunctionEnvironmentalVariableA.setValue("9920");
        environmentalVariableDAO.save(sumFunctionEnvironmentalVariableA);

        // Exposed Interface
        interfaceSumFunction = new Interface();
        interfaceSumFunction.setDateCreated(new Date());
        interfaceSumFunction.setLastModified(new Date());
        interfaceSumFunction.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceSumFunction.setName("interfaceSumFunction");
        interfaceSumFunction.setPort("9920");
        interfaceSumFunction.setVna("VNA0");
        interfaceSumFunction.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceSumFunction.setComponent(sumFunction);
        interfaceDAO.save(interfaceSumFunction);

        // Minimum Execution Requirements
        Requirement sumFunctionMinimumExecutionRequirements = new Requirement();
        sumFunctionMinimumExecutionRequirements.setComponent(sumFunction);
        sumFunctionMinimumExecutionRequirements.setDateCreated(new Date());
        sumFunctionMinimumExecutionRequirements.setLastModified(new Date());
        sumFunctionMinimumExecutionRequirements.setGpuRequired(false);
        sumFunctionMinimumExecutionRequirements.setHypervisorType("ESXI");
        sumFunctionMinimumExecutionRequirements.setRam(2048);
        sumFunctionMinimumExecutionRequirements.setStorage(20);
        sumFunctionMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(sumFunctionMinimumExecutionRequirements);

        // Health Check
        HealthCheck sumFunctionHealthCheck = new HealthCheck();
        sumFunctionHealthCheck.setComponent(sumFunction);
        sumFunctionHealthCheck.setDateCreated(new Date());
        sumFunctionHealthCheck.setLastModified(new Date());
        sumFunctionHealthCheck.setName("SumFunctionHealthCheck");
        sumFunctionHealthCheck.setInterval(10L);
        sumFunctionHealthCheck.setArgs(null);
        sumFunctionHealthCheck.setHttpURL(EXAMPLE_URL);
        healthCheckDAO.save(sumFunctionHealthCheck);

        componentDAO.save(sumFunction);

        // Division Function
        divFunction = new eu.orchestrator.repository.domain.Component();
        divFunction.setName("DivFunction");
        divFunction.setHexID(Util.createRandomHEXString());
        divFunction.setDockerImage("lambda_proxy_app/division:1.0.0");
        divFunction.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        divFunction.setDockerUsername(MAESTRO);
        divFunction.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        divFunction.setPublicComponent(true);
        divFunction.setDateCreated(new Date());
        divFunction.setLastModified(new Date());
        divFunction.setUser(admin);
        divFunction.setOrganization(admin.getOrganization());
        divFunction.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        divFunction.setElasticityController(LAMBDA_FUNCTION);
        divFunction.setElasticityControllerMode("gRPC");
        divFunction.setPlugins(new TreeSet<>(listOfDefaultPlugins));
        componentDAO.save(divFunction);

        // Labels
        divFunction.setLabels(new TreeSet<>(Arrays.asList(labelFunction)));

        // Environmental Variables
        EnvironmentalVariable divFunctionEnvironmentalVariableA = new EnvironmentalVariable();
        divFunctionEnvironmentalVariableA.setComponent(divFunction);
        divFunctionEnvironmentalVariableA.setDateCreated(new Date());
        divFunctionEnvironmentalVariableA.setLastModified(new Date());
        divFunctionEnvironmentalVariableA.setKey("GRPC_PORT");
        divFunctionEnvironmentalVariableA.setValue("9930");
        environmentalVariableDAO.save(divFunctionEnvironmentalVariableA);

        // Exposed Interface
        interfaceDivisionFunction = new Interface();
        interfaceDivisionFunction.setDateCreated(new Date());
        interfaceDivisionFunction.setLastModified(new Date());
        interfaceDivisionFunction.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceDivisionFunction.setName("interfaceDivisionFunction");
        interfaceDivisionFunction.setPort("9930");
        interfaceDivisionFunction.setVna("VNA0");
        interfaceDivisionFunction.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceDivisionFunction.setComponent(divFunction);
        interfaceDAO.save(interfaceDivisionFunction);

        // Minimum Execution Requirements
        Requirement divFunctionMinimumExecutionRequirements = new Requirement();
        divFunctionMinimumExecutionRequirements.setComponent(divFunction);
        divFunctionMinimumExecutionRequirements.setDateCreated(new Date());
        divFunctionMinimumExecutionRequirements.setLastModified(new Date());
        divFunctionMinimumExecutionRequirements.setGpuRequired(false);
        divFunctionMinimumExecutionRequirements.setHypervisorType("ESXI");
        divFunctionMinimumExecutionRequirements.setRam(2048);
        divFunctionMinimumExecutionRequirements.setStorage(20);
        divFunctionMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(divFunctionMinimumExecutionRequirements);

        // Health Check
        HealthCheck divFunctionHealthCheck = new HealthCheck();
        divFunctionHealthCheck.setComponent(divFunction);
        divFunctionHealthCheck.setDateCreated(new Date());
        divFunctionHealthCheck.setLastModified(new Date());
        divFunctionHealthCheck.setName("DivFunctionHealthCheck");
        divFunctionHealthCheck.setInterval(10L);
        divFunctionHealthCheck.setArgs(null);
        divFunctionHealthCheck.setHttpURL(EXAMPLE_URL);
        healthCheckDAO.save(divFunctionHealthCheck);

        componentDAO.save(divFunction);

        // Mul Function
        mulFunction = new eu.orchestrator.repository.domain.Component();
        mulFunction.setName("MulFunction");
        mulFunction.setHexID(Util.createRandomHEXString());
        mulFunction.setDockerImage("lambda_proxy_app/multiplication:1.0.0");
        mulFunction.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        mulFunction.setDockerUsername(MAESTRO);
        mulFunction.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        mulFunction.setPublicComponent(true);
        mulFunction.setDateCreated(new Date());
        mulFunction.setLastModified(new Date());
        mulFunction.setUser(admin);
        mulFunction.setOrganization(admin.getOrganization());
        mulFunction.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        mulFunction.setElasticityController(LAMBDA_FUNCTION);
        mulFunction.setElasticityControllerMode("HTTP");
        mulFunction.setPlugins(new TreeSet<>(listOfDefaultPlugins));
        componentDAO.save(mulFunction);

        // Labels
        sumFunction.setLabels(new TreeSet<>(Arrays.asList(labelFunction)));

        // Environmental Variables
        EnvironmentalVariable mulFunctionEnvironmentalVariableA = new EnvironmentalVariable();
        mulFunctionEnvironmentalVariableA.setComponent(mulFunction);
        mulFunctionEnvironmentalVariableA.setDateCreated(new Date());
        mulFunctionEnvironmentalVariableA.setLastModified(new Date());
        mulFunctionEnvironmentalVariableA.setKey(SERVER_PORT);
        mulFunctionEnvironmentalVariableA.setValue("9910");
        environmentalVariableDAO.save(mulFunctionEnvironmentalVariableA);

        // Exposed Interface
        interfaceMulFunction = new Interface();
        interfaceMulFunction.setDateCreated(new Date());
        interfaceMulFunction.setLastModified(new Date());
        interfaceMulFunction.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfaceMulFunction.setName("interfaceMulFunction");
        interfaceMulFunction.setPort("9910");
        interfaceMulFunction.setVna("VNA0");
        interfaceMulFunction.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceMulFunction.setComponent(mulFunction);
        interfaceDAO.save(interfaceMulFunction);

        // Minimum Execution Requirements
        Requirement mulFunctionMinimumExecutionRequirements = new Requirement();
        mulFunctionMinimumExecutionRequirements.setComponent(mulFunction);
        mulFunctionMinimumExecutionRequirements.setDateCreated(new Date());
        mulFunctionMinimumExecutionRequirements.setLastModified(new Date());
        mulFunctionMinimumExecutionRequirements.setGpuRequired(false);
        mulFunctionMinimumExecutionRequirements.setHypervisorType("ESXI");
        mulFunctionMinimumExecutionRequirements.setRam(2048);
        mulFunctionMinimumExecutionRequirements.setStorage(20);
        mulFunctionMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(mulFunctionMinimumExecutionRequirements);

        // Health Check
        HealthCheck mulFunctionHealthCheck = new HealthCheck();
        mulFunctionHealthCheck.setComponent(mulFunction);
        mulFunctionHealthCheck.setDateCreated(new Date());
        mulFunctionHealthCheck.setLastModified(new Date());
        mulFunctionHealthCheck.setName("SumFunctionHealthCheck");
        mulFunctionHealthCheck.setInterval(10L);
        mulFunctionHealthCheck.setArgs(null);
        mulFunctionHealthCheck.setHttpURL(EXAMPLE_URL);
        healthCheckDAO.save(mulFunctionHealthCheck);

        componentDAO.save(mulFunction);

        // Main App Function
        mainAppFunction = new eu.orchestrator.repository.domain.Component();
        mainAppFunction.setName("MainAppFunction");
        mainAppFunction.setHexID(Util.createRandomHEXString());
        mainAppFunction.setDockerImage("lambda_proxy_app/main_app:1.0.0");
        mainAppFunction.setDockerRegistry(MAESTRO_DOCKER_REGISTRY);
        mainAppFunction.setDockerUsername(MAESTRO);
        mainAppFunction.setDockerPassword(Util.encrypt(MAESTRO_DOCKER_PASS, secretToken));
        mainAppFunction.setPublicComponent(true);
        mainAppFunction.setDateCreated(new Date());
        mainAppFunction.setLastModified(new Date());
        mainAppFunction.setUser(admin);
        mainAppFunction.setOrganization(admin.getOrganization());
        mainAppFunction.setElasticityController("NONE");
        mainAppFunction.setArchitecture(Architecture.X86.name());
        mainAppFunction.setPlugins(new TreeSet<>(listOfDefaultPlugins));
        componentDAO.save(mainAppFunction);

        // Labels
        mainAppFunction.setLabels(new TreeSet<>(Arrays.asList(labelFunction)));

        // Environmental Variables
        EnvironmentalVariable mainAppEnvironmentalVariableA = new EnvironmentalVariable();
        mainAppEnvironmentalVariableA.setComponent(mainAppFunction);
        mainAppEnvironmentalVariableA.setDateCreated(new Date());
        mainAppEnvironmentalVariableA.setLastModified(new Date());
        mainAppEnvironmentalVariableA.setKey(SERVER_PORT);
        mainAppEnvironmentalVariableA.setValue("9980");
        environmentalVariableDAO.save(mainAppEnvironmentalVariableA);

        EnvironmentalVariable mainAppEnvironmentalVariableB = new EnvironmentalVariable();
        mainAppEnvironmentalVariableB.setComponent(mainAppFunction);
        mainAppEnvironmentalVariableB.setDateCreated(new Date());
        mainAppEnvironmentalVariableB.setLastModified(new Date());
        mainAppEnvironmentalVariableB.setKey("SUM_PORT");
        mainAppEnvironmentalVariableB.setValue("9920");
        environmentalVariableDAO.save(mainAppEnvironmentalVariableB);

        EnvironmentalVariable mainAppEnvironmentalVariableC = new EnvironmentalVariable();
        mainAppEnvironmentalVariableC.setComponent(mainAppFunction);
        mainAppEnvironmentalVariableC.setDateCreated(new Date());
        mainAppEnvironmentalVariableC.setLastModified(new Date());
        mainAppEnvironmentalVariableC.setKey("MULTIPLICATION_PORT");
        mainAppEnvironmentalVariableC.setValue("9910");
        environmentalVariableDAO.save(mainAppEnvironmentalVariableC);

        EnvironmentalVariable mainAppEnvironmentalVariableD = new EnvironmentalVariable();
        mainAppEnvironmentalVariableD.setComponent(mainAppFunction);
        mainAppEnvironmentalVariableD.setDateCreated(new Date());
        mainAppEnvironmentalVariableD.setLastModified(new Date());
        mainAppEnvironmentalVariableD.setKey("DIVISION_PORT");
        mainAppEnvironmentalVariableD.setValue("9930");
        environmentalVariableDAO.save(mainAppEnvironmentalVariableD);

        EnvironmentalVariable mainAppEnvironmentalVariableE = new EnvironmentalVariable();
        mainAppEnvironmentalVariableE.setComponent(mainAppFunction);
        mainAppEnvironmentalVariableE.setDateCreated(new Date());
        mainAppEnvironmentalVariableE.setLastModified(new Date());
        mainAppEnvironmentalVariableE.setKey("IPV6ENABLED");
        mainAppEnvironmentalVariableE.setValue("disabled");
        environmentalVariableDAO.save(mainAppEnvironmentalVariableE);

        EnvironmentalVariable mainAppEnvironmentalVariableF = new EnvironmentalVariable();
        mainAppEnvironmentalVariableF.setComponent(mainAppFunction);
        mainAppEnvironmentalVariableF.setDateCreated(new Date());
        mainAppEnvironmentalVariableF.setLastModified(new Date());
        mainAppEnvironmentalVariableF.setKey("SUM_URL");
        mainAppEnvironmentalVariableF.setValue("@SumFunction");
        environmentalVariableDAO.save(mainAppEnvironmentalVariableF);

        EnvironmentalVariable mainAppEnvironmentalVariableG = new EnvironmentalVariable();
        mainAppEnvironmentalVariableG.setComponent(mainAppFunction);
        mainAppEnvironmentalVariableG.setDateCreated(new Date());
        mainAppEnvironmentalVariableG.setLastModified(new Date());
        mainAppEnvironmentalVariableG.setKey("MULTIPLICATION_URL");
        mainAppEnvironmentalVariableG.setValue("@MulFunction");
        environmentalVariableDAO.save(mainAppEnvironmentalVariableG);

        EnvironmentalVariable mainAppEnvironmentalVariableH = new EnvironmentalVariable();
        mainAppEnvironmentalVariableH.setComponent(mainAppFunction);
        mainAppEnvironmentalVariableH.setDateCreated(new Date());
        mainAppEnvironmentalVariableH.setLastModified(new Date());
        mainAppEnvironmentalVariableH.setKey("DIVISION_URL");
        mainAppEnvironmentalVariableH.setValue("@DivFunction");
        environmentalVariableDAO.save(mainAppEnvironmentalVariableH);

        // Required Interface
        mainAppGraphLinkSumFunctionInterface = new GraphLink();
        mainAppGraphLinkSumFunctionInterface.setInterfaceObj(interfaceSumFunction);
        mainAppGraphLinkSumFunctionInterface.setComponent(mainAppFunction);
        mainAppGraphLinkSumFunctionInterface.setDateCreated(new Date());
        mainAppGraphLinkSumFunctionInterface.setLastModified(new Date());
        graphLinkDAO.save(mainAppGraphLinkSumFunctionInterface);

        mainAppGraphLinkMulFunctionInterface = new GraphLink();
        mainAppGraphLinkMulFunctionInterface.setInterfaceObj(interfaceMulFunction);
        mainAppGraphLinkMulFunctionInterface.setComponent(mainAppFunction);
        mainAppGraphLinkMulFunctionInterface.setDateCreated(new Date());
        mainAppGraphLinkMulFunctionInterface.setLastModified(new Date());
        graphLinkDAO.save(mainAppGraphLinkMulFunctionInterface);

        mainAppGraphLinkDivFunctionInterface = new GraphLink();
        mainAppGraphLinkDivFunctionInterface.setInterfaceObj(interfaceDivisionFunction);
        mainAppGraphLinkDivFunctionInterface.setComponent(mainAppFunction);
        mainAppGraphLinkDivFunctionInterface.setDateCreated(new Date());
        mainAppGraphLinkDivFunctionInterface.setLastModified(new Date());
        graphLinkDAO.save(mainAppGraphLinkDivFunctionInterface);

        // Exposed Interface
        interfaceMainAppFunction = new Interface();
        interfaceMainAppFunction.setDateCreated(new Date());
        interfaceMainAppFunction.setLastModified(new Date());
        interfaceMainAppFunction.setInterfaceType(Interface.InterfaceType.ACCESS.name());
        interfaceMainAppFunction.setName("MainAppExposedInterface");
        interfaceMainAppFunction.setPort("9980");
        interfaceMainAppFunction.setVna("VNA0");
        interfaceMainAppFunction.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceMainAppFunction.setComponent(mainAppFunction);
        interfaceDAO.save(interfaceMainAppFunction);

        // Minimum Execution Requirements
        Requirement mainAppMinimumExecutionRequirements = new Requirement();
        mainAppMinimumExecutionRequirements.setComponent(mainAppFunction);
        mainAppMinimumExecutionRequirements.setDateCreated(new Date());
        mainAppMinimumExecutionRequirements.setLastModified(new Date());
        mainAppMinimumExecutionRequirements.setGpuRequired(false);
        mainAppMinimumExecutionRequirements.setHypervisorType("ESXI");
        mainAppMinimumExecutionRequirements.setRam(2048);
        mainAppMinimumExecutionRequirements.setStorage(20);
        mainAppMinimumExecutionRequirements.setvCPUs(1);
        requirementDAO.save(mainAppMinimumExecutionRequirements);

        // Health Check
        HealthCheck mainAppHealthCheck = new HealthCheck();
        mainAppHealthCheck.setComponent(mainAppFunction);
        mainAppHealthCheck.setDateCreated(new Date());
        mainAppHealthCheck.setLastModified(new Date());
        mainAppHealthCheck.setName("mainAppHealthCheck");
        mainAppHealthCheck.setInterval(10L);
        mainAppHealthCheck.setArgs(null);
        mainAppHealthCheck.setHttpURL(EXAMPLE_URL);
        healthCheckDAO.save(mainAppHealthCheck);

        componentDAO.save(mainAppFunction);

        // Application
        functinPilotApplication = new eu.orchestrator.repository.domain.Application();
        functinPilotApplication.setName("FunctionPilot");
        functinPilotApplication.setHexID(Util.createRandomHEXString());
        functinPilotApplication.setDateCreated(new Date());
        functinPilotApplication.setUser(admin);
        functinPilotApplication.setOrganization(admin.getOrganization());
        functinPilotApplication.setPublicApplication(true);
        functinPilotApplication.setLastModified(new Date());
        applicationDAO.save(functinPilotApplication);

        // Component Nodes
        ComponentNode sumFunctionNode = new ComponentNode();
        sumFunctionNode.setName("sumFunction");
        sumFunctionNode.setHexID(Util.createRandomHEXString());
        sumFunctionNode.setComponent(sumFunction);
        sumFunctionNode.setDateCreated(new Date());
        sumFunctionNode.setLastModified(new Date());
        sumFunctionNode.setApplication(functinPilotApplication);
        componentNodeDAO.save(sumFunctionNode);

        // Component Nodes
        ComponentNode mulFunctionNode = new ComponentNode();
        mulFunctionNode.setName("mulFunction");
        mulFunctionNode.setHexID(Util.createRandomHEXString());
        mulFunctionNode.setComponent(mulFunction);
        mulFunctionNode.setDateCreated(new Date());
        mulFunctionNode.setLastModified(new Date());
        mulFunctionNode.setApplication(functinPilotApplication);
        componentNodeDAO.save(mulFunctionNode);

        // Component Nodes
        ComponentNode divFunctionNode = new ComponentNode();
        divFunctionNode.setName("divFunction");
        divFunctionNode.setHexID(Util.createRandomHEXString());
        divFunctionNode.setComponent(divFunction);
        divFunctionNode.setDateCreated(new Date());
        divFunctionNode.setLastModified(new Date());
        divFunctionNode.setApplication(functinPilotApplication);
        componentNodeDAO.save(divFunctionNode);

        // Component Nodes
        ComponentNode mainAppFunctionNode = new ComponentNode();
        mainAppFunctionNode.setName("mainAppFunction");
        mainAppFunctionNode.setHexID(Util.createRandomHEXString());
        mainAppFunctionNode.setComponent(mainAppFunction);
        mainAppFunctionNode.setDateCreated(new Date());
        mainAppFunctionNode.setLastModified(new Date());
        mainAppFunctionNode.setApplication(functinPilotApplication);
        componentNodeDAO.save(mainAppFunctionNode);

        // Graph Link Nodes
        GraphLinkNode functionGraphLinkNodeMainAppToSumFunction = new GraphLinkNode();
        functionGraphLinkNodeMainAppToSumFunction.setComponentNodeFrom(mainAppFunctionNode);
        functionGraphLinkNodeMainAppToSumFunction.setComponentNodeTo(sumFunctionNode);
        functionGraphLinkNodeMainAppToSumFunction.setGraphLink(mainAppGraphLinkSumFunctionInterface);
        functionGraphLinkNodeMainAppToSumFunction.setApplication(functinPilotApplication);
        functionGraphLinkNodeMainAppToSumFunction.setDateCreated(new Date());
        functionGraphLinkNodeMainAppToSumFunction.setLastModified(new Date());
        graphLinkNodeDAO.save(functionGraphLinkNodeMainAppToSumFunction);

        GraphLinkNode functionGraphLinkNodeMainAppToMulFunction = new GraphLinkNode();
        functionGraphLinkNodeMainAppToMulFunction.setComponentNodeFrom(mainAppFunctionNode);
        functionGraphLinkNodeMainAppToMulFunction.setComponentNodeTo(mulFunctionNode);
        functionGraphLinkNodeMainAppToMulFunction.setGraphLink(mainAppGraphLinkMulFunctionInterface);
        functionGraphLinkNodeMainAppToMulFunction.setApplication(functinPilotApplication);
        functionGraphLinkNodeMainAppToMulFunction.setDateCreated(new Date());
        functionGraphLinkNodeMainAppToMulFunction.setLastModified(new Date());
        graphLinkNodeDAO.save(functionGraphLinkNodeMainAppToMulFunction);

        GraphLinkNode functionGraphLinkNodeMainAppToDivFunction = new GraphLinkNode();
        functionGraphLinkNodeMainAppToDivFunction.setComponentNodeFrom(mainAppFunctionNode);
        functionGraphLinkNodeMainAppToDivFunction.setComponentNodeTo(divFunctionNode);
        functionGraphLinkNodeMainAppToDivFunction.setGraphLink(mainAppGraphLinkDivFunctionInterface);
        functionGraphLinkNodeMainAppToDivFunction.setApplication(functinPilotApplication);
        functionGraphLinkNodeMainAppToDivFunction.setDateCreated(new Date());
        functionGraphLinkNodeMainAppToDivFunction.setLastModified(new Date());
        graphLinkNodeDAO.save(functionGraphLinkNodeMainAppToDivFunction);

        logger.info("Function Application Pilot have been added successfully!");

    }

    private void inintializeHashes() {
        ComponentNodeInstanceHash componentNodeInstanceHash = new ComponentNodeInstanceHash();
        componentNodeInstanceHash.setDateCreated(new Date());
        componentNodeInstanceHash.setLastModified(new Date());
        //componentNodeInstanceHash.setComponentNodeInstance();
        // TODO hash for agent service
        String hashAgentService = "";
        componentNodeInstanceHash.setValue(Util.encrypt(hashAgentService, secretToken));
        componentNodeInstanceHash.setType(HashType.AGENT_SERVICE.name());
        componentNodeInstanceHashDAO.save(componentNodeInstanceHash);
    }

}
