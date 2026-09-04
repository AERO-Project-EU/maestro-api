package eu.orchestrator.backend;

import eu.orchestrator.backend.util.Util;
import eu.orchestrator.repository.dao.ApplicationDAO;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ComponentDAO;
import eu.orchestrator.repository.dao.ComponentNodeDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
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
import eu.orchestrator.repository.dao.UserDAO;
import eu.orchestrator.repository.domain.Component.Architecture;
import eu.orchestrator.repository.domain.Component.CapabilityAdd;
import eu.orchestrator.repository.domain.Component.CapabilityDrop;
import eu.orchestrator.repository.domain.ComponentNode;
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
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Profile("dev")
@Component
@Transactional
public class ApplicationStartupDev implements ApplicationListener<ApplicationReadyEvent> {

    static final Logger logger = Logger.getLogger(ApplicationStartupDev.class.getName());

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
    @PersistenceContext
    EntityManager entityManager;
    // Variables
    Organization organizationAdmin, organizationUbitech, organizationUbitechEnergy, organizationSuite5;
    User admin, ubitechAdmin, ubitechEnergyAdmin, ubitechUserA, ubitechUserB, ubitechEnergyUser, ubitechSuite5;
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
    ProviderType _5gProviderType;
    ProviderType openstackProviderType;
    ProviderType iotGatewayProviderType;
    ProviderType kubernetesProviderType;
    ProviderType rainbowKubernetesProviderType;
    ProviderType _5gOssKubernetesProviderType;
    Provider policyDefinedProvider;
    Provider userDefinedProvider;
    Provider _5gTelcoProvider;
    Provider iotGatewayProvider;
    Provider awsProvider;
    Provider ubiDell;
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
    Label labelEcho;
    Interface interfaceSQL;
    Interface interfacePPDRSQL;
    Interface interfaceSambaA;
    Interface interfaceSambaB;
    Interface interfacePhpDashboard;
    Interface interfaceNoSQL;
    Interface interfaceHttp;
    Interface interfaceHttpEcho;
    GraphLink phpDashboardGraphLinkSQLInterface;
    GraphLink phpDashboardGraphLinkSambaInterface;
    GraphLink phpMyAdminGraphLink;
    GraphLink wordPressGraphLink;
    eu.orchestrator.repository.domain.Component loadBalancer;
    eu.orchestrator.repository.domain.Component lambdaProxy;
    eu.orchestrator.repository.domain.Component mariaDB;
    eu.orchestrator.repository.domain.Component mongoDB;
    eu.orchestrator.repository.domain.Component phpMyAdmin;
    eu.orchestrator.repository.domain.Component wordPress;
    eu.orchestrator.repository.domain.Component ppdrDatabase;
    eu.orchestrator.repository.domain.Component ppdrSamba;
    eu.orchestrator.repository.domain.Component ppdrPhpDashboard;
    eu.orchestrator.repository.domain.Component httpEcho;
    eu.orchestrator.repository.domain.Application dbms;
    eu.orchestrator.repository.domain.Application cms;
    eu.orchestrator.repository.domain.Application ppdrApplication;
    @Value("${token.signer.secret}")
    private String tokenSecret;

    // Kubernetes provider credentials (base64 encoded PEM). No defaults: a cluster's admin
    // credentials must never be baked into the image.
    // Seed data for the demo users/plugins. All optional: leave unset to seed nothing sensitive.
    @Value("${initialization.seed.user-password-hash:}")
    private String seedUserPasswordHash;
    @Value("${ui.server.url:}")
    private String uiServerUrl;
    @Value("${initialization.seed.netdata-plugin-base-url:}")
    private String netdataPluginBaseUrl;

    // OpenStack keystone endpoint used by the seeded demo providers.
    @Value("${openstack.endpoint:}")
    private String openstackEndpoint;

    @Value("${kubernetes.rainbow.endpoint:}")
    private String rainbowEndpoint;
    @Value("${kubernetes.rainbow.ca-certificate:}")
    private String rainbowCaCertificate;
    @Value("${kubernetes.rainbow.client-certificate:}")
    private String rainbowClientCertificate;
    @Value("${kubernetes.rainbow.client-key:}")
    private String rainbowClientKey;
    @Value("${token.signer.secret}")
    private String secretToken;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

        // Check if initialization is needed
        if (!userDAO.findByUsername("admin").isPresent()) {

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

            logger.info("Initialization process has been completed successfully!");

        } else {

            logger.info("Initialization process has been already completed!");

        }

    }

    private void initializeCountries() {

        logger.info("Countries initialization has started...");

        BufferedReader br = null;
        try {

            Resource resCountries = resourceLoader.getResource("classpath:countries.csv");

            String line = "";
            String cvsSplitBy = ";";

            int counter = 0;

            br = new BufferedReader(new InputStreamReader(resCountries.getInputStream(), StandardCharsets.UTF_8));
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
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
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
        _5gProviderType = new ProviderType();
        _5gProviderType.setName(ProviderType.ProviderName.FIFTH_GENERATION_TELCO_PROVIDER.name());
        _5gProviderType.setFriendlyName(ProviderType.ProviderName.FIFTH_GENERATION_TELCO_PROVIDER.getFriendlyName());
        _5gProviderType.setDateCreated(new Date());
        _5gProviderType.setLastModified(new Date());
        _5gProviderType.setEnabled(true);
        providerTypeDAO.save(_5gProviderType);

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

        logger.info("Provider types have been added successfully!");
    }

    private void initializeQualityIdentifiers() {

        logger.info("Quality identifiers initialization has started...");

        BufferedReader br = null;

        try {

            Resource resQIs = resourceLoader.getResource("classpath:qci.csv");

            String line = "";
            String cvsSplitBy = ";";

            int counter = 0;

            br = new BufferedReader(new InputStreamReader(resQIs.getInputStream(), StandardCharsets.UTF_8));
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
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
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

        organizationUbitech = new Organization();
        organizationUbitech.setName("Ubitech");
        organizationUbitech.setStatus(Organization.Status.ACTIVE.name());
        organizationUbitech.setDateCreated(new Date());
        organizationUbitech.setLastModified(new Date());

        organizationDAO.save(organizationUbitech);

        organizationUbitechEnergy = new Organization();
        organizationUbitechEnergy.setName("Ubitech_Energy");
        organizationUbitechEnergy.setStatus(Organization.Status.ACTIVE.name());
        organizationUbitechEnergy.setDateCreated(new Date());
        organizationUbitechEnergy.setLastModified(new Date());

        organizationDAO.save(organizationUbitechEnergy);

        organizationSuite5 = new Organization();
        organizationSuite5.setName("Suite5");
        organizationSuite5.setStatus(Organization.Status.ACTIVE.name());
        organizationSuite5.setDateCreated(new Date());
        organizationSuite5.setLastModified(new Date());

        organizationDAO.save(organizationSuite5);

        logger.info("Organization have been added successfully!");
    }

    private void initializeUsers() {

        logger.info("Users initialization has started...");

        admin = new User();
        admin.setUsername("admin");
        admin.setPassword(seedUserPasswordHash);
        admin.setFirstName("Admin");
        admin.setLastName("Admin");
        admin.setEmail("admin@gmail.com");
        admin.setPhone("+306900000000");
        admin.setRole("ADMIN");
        admin.setDateCreated(new Date());
        admin.setNotificationEmailEnabled(true);
        admin.setNotificationWebEnabled(true);
        admin.setCountry(countryDAO.findByName("Greece").get());
        admin.setFirstLogin(false);
        admin.setEnabled(true);
        admin.setOrganization(organizationAdmin);

        userDAO.save(admin);

        ubitechAdmin = new User();
        ubitechAdmin.setUsername("ubitechAdmin");
        ubitechAdmin.setPassword(seedUserPasswordHash);
        ubitechAdmin.setFirstName("ubitechAdmin");
        ubitechAdmin.setLastName("ubitechAdmin");
        ubitechAdmin.setEmail("ubitechAdmin@gmail.com");
        ubitechAdmin.setPhone("+306900000000");
        ubitechAdmin.setRole("ORGANIZATIONADMIN");
        ubitechAdmin.setDateCreated(new Date());
        ubitechAdmin.setNotificationEmailEnabled(true);
        ubitechAdmin.setNotificationWebEnabled(true);
        ubitechAdmin.setCountry(countryDAO.findByName("Greece").get());
        ubitechAdmin.setFirstLogin(false);
        ubitechAdmin.setEnabled(true);
        ubitechAdmin.setOrganization(organizationUbitech);

        userDAO.save(ubitechAdmin);

        ubitechEnergyAdmin = new User();
        ubitechEnergyAdmin.setUsername("ubitechEnergyAdmin");
        ubitechEnergyAdmin.setPassword(seedUserPasswordHash);
        ubitechEnergyAdmin.setFirstName("ubitechEnergyAdmin");
        ubitechEnergyAdmin.setLastName("ubitechEnergyAdmin");
        ubitechEnergyAdmin.setEmail("ubitechEnergyAdmin@gmail.com");
        ubitechEnergyAdmin.setPhone("+306900000000");
        ubitechEnergyAdmin.setRole("ORGANIZATIONADMIN");
        ubitechEnergyAdmin.setDateCreated(new Date());
        ubitechEnergyAdmin.setNotificationEmailEnabled(true);
        ubitechEnergyAdmin.setNotificationWebEnabled(true);
        ubitechEnergyAdmin.setCountry(countryDAO.findByName("Greece").get());
        ubitechEnergyAdmin.setFirstLogin(false);
        ubitechEnergyAdmin.setEnabled(true);
        ubitechEnergyAdmin.setOrganization(organizationUbitechEnergy);

        userDAO.save(ubitechEnergyAdmin);

        ubitechUserA = new User();
        ubitechUserA.setUsername("ubitechUserA");
        ubitechUserA.setPassword(seedUserPasswordHash);
        ubitechUserA.setFirstName("ubitechUserA");
        ubitechUserA.setLastName("ubitechUserA");
        ubitechUserA.setEmail("ubitechUserA@gmail.com");
        ubitechUserA.setPhone("+306900000000");
        ubitechUserA.setRole("USER");
        ubitechUserA.setDateCreated(new Date());
        ubitechUserA.setNotificationEmailEnabled(true);
        ubitechUserA.setNotificationWebEnabled(true);
        ubitechUserA.setCountry(countryDAO.findByName("Greece").get());
        ubitechUserA.setFirstLogin(false);
        ubitechUserA.setEnabled(true);
        ubitechUserA.setOrganization(organizationUbitech);

        userDAO.save(ubitechUserA);

        ubitechUserB = new User();
        ubitechUserB.setUsername("ubitechUserB");
        ubitechUserB.setPassword(seedUserPasswordHash);
        ubitechUserB.setFirstName("ubitechUserB");
        ubitechUserB.setLastName("ubitechUserB");
        ubitechUserB.setEmail("ubitechUserB@gmail.com");
        ubitechUserB.setPhone("+306900000000");
        ubitechUserB.setRole("USER");
        ubitechUserB.setDateCreated(new Date());
        ubitechUserB.setNotificationEmailEnabled(true);
        ubitechUserB.setNotificationWebEnabled(true);
        ubitechUserB.setCountry(countryDAO.findByName("Greece").get());
        ubitechUserB.setFirstLogin(false);
        ubitechUserB.setEnabled(true);
        ubitechUserB.setOrganization(organizationUbitech);

        userDAO.save(ubitechUserB);

        ubitechEnergyUser = new User();
        ubitechEnergyUser.setUsername("ubitechEnergyUser");
        ubitechEnergyUser.setPassword(seedUserPasswordHash);
        ubitechEnergyUser.setFirstName("ubitechEnergyUser");
        ubitechEnergyUser.setLastName("ubitechEnergyUser");
        ubitechEnergyUser.setEmail("ubitechEnergyUser@gmail.com");
        ubitechEnergyUser.setPhone("+306900000000");
        ubitechEnergyUser.setRole("USER");
        ubitechEnergyUser.setDateCreated(new Date());
        ubitechEnergyUser.setNotificationEmailEnabled(true);
        ubitechEnergyUser.setNotificationWebEnabled(true);
        ubitechEnergyUser.setCountry(countryDAO.findByName("Greece").get());
        ubitechEnergyUser.setFirstLogin(false);
        ubitechEnergyUser.setEnabled(true);
        ubitechEnergyUser.setOrganization(organizationUbitechEnergy);

        userDAO.save(ubitechEnergyUser);

        ubitechSuite5 = new User();
        ubitechSuite5.setUsername("suite5");
        ubitechSuite5.setPassword(seedUserPasswordHash);
        ubitechSuite5.setFirstName("suite5");
        ubitechSuite5.setLastName("suite5");
        ubitechSuite5.setEmail("suite5@gmail.com");
        ubitechSuite5.setPhone("+306900000000");
        ubitechSuite5.setRole("ORGANIZATIONADMIN");
        ubitechSuite5.setDateCreated(new Date());
        ubitechSuite5.setNotificationEmailEnabled(true);
        ubitechSuite5.setNotificationWebEnabled(true);
        ubitechSuite5.setCountry(countryDAO.findByName("Greece").get());
        ubitechSuite5.setFirstLogin(false);
        ubitechSuite5.setEnabled(true);
        ubitechSuite5.setOrganization(organizationSuite5);

        userDAO.save(ubitechSuite5);
        logger.info("Users have been added successfully!");
    }

    private void initializePlugins() {

        logger.info("Plugins initialization has started...");

        BufferedReader br = null;

        try {

            Resource resPlugins = resourceLoader.getResource("classpath:plugins.csv");

            String line = "";
            String cvsSplitBy = ",";

            Map<String, List<String>> mapOfLines = new HashMap<>();

            br = new BufferedReader(new InputStreamReader(resPlugins.getInputStream(), StandardCharsets.UTF_8));
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

                    if (null != existingLines && !existingLines.isEmpty()) {

                        if (!existingLines.contains(line)) {

                            newLines.add(line);
                            newLines.addAll(existingLines);

                            mapOfLines.remove(pluginName + "^" + moduleName);
                            mapOfLines.put(pluginName + "^" + moduleName, newLines);
                        }

                    }

                }

            }

            if (!mapOfLines.isEmpty()) {

                mapOfLines.entrySet().stream().forEach(entry -> {

                    String key = entry.getKey();
                    String[] keyArray = key.split("\\^");
                    List<String> pluginLine = entry.getValue();

                    boolean defaultPlugin = pluginLine.get(0).split("\\,")[6].equals("1");
                    boolean immutablePlugin = pluginLine.get(0).split("\\,")[5].equals("1");

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
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }

        logger.info("Plugins have been added successfully!");

    }

    private void initializeCustomPlugins() {

        // Register custom module
        customMetricPlugin = new Plugin();
        customMetricPlugin.setName("python.d");
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
        Metric factorial_execution_time = new Metric();
        factorial_execution_time.setPlugin(customMetricPlugin);
        factorial_execution_time.setName("_factorial_execution_time_milliseconds_average");
        factorial_execution_time.setFriendlyName("Factorial Execution time");
        factorial_execution_time.setUnit("milliseconds");
        factorial_execution_time.setLastModified(new Date());
        factorial_execution_time.setDateCreated(new Date());
        metricDAO.save(factorial_execution_time);

        // Register Squid module
        agentPlugin = new Plugin();
        agentPlugin.setName("python.d");
        agentPlugin.setModuleName("agentCollector");
        agentPlugin.setDefaultPlugin(true);
        agentPlugin.setPublicPlugin(true);
        agentPlugin.setImmutablePlugin(true);
        agentPlugin.setDateCreated(new Date());
        agentPlugin.setLastModified(new Date());

        agentPlugin.setDownloadURL(
                netdataPluginBaseUrl + "/agent/agentCollector.chart.py");

        agentPlugin.setPluginType(Plugin.PluginType.DOWNLOAD_CONF.name());
        agentPlugin.setEndpoint(netdataPluginBaseUrl + "/agent/agentCollector.conf");
        agentPlugin.setUser(admin);
        agentPlugin.setOrganization(organizationAdmin);
        pluginDAO.save(agentPlugin);

        // Register agent collector metrics
        Metric agent_profile_execution = new Metric();
        agent_profile_execution.setPlugin(agentPlugin);
        agent_profile_execution.setName("_agent_profile_execution_milliseconds_average");
        agent_profile_execution.setFriendlyName("Agent Execution time");
        agent_profile_execution.setUnit("milliseconds");
        agent_profile_execution.setLastModified(new Date());
        agent_profile_execution.setDateCreated(new Date());
        metricDAO.save(agent_profile_execution);

        // Register Squid module
        squidPlugin = new Plugin();
        squidPlugin.setName("python.d");
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
        Metric squid_clients_net = new Metric();
        squid_clients_net.setPlugin(squidPlugin);
        squid_clients_net.setName("clients_net");
        squid_clients_net.setFriendlyName("Squid Client Bandwidth");
        squid_clients_net.setUnit("kilobits/s");
        squid_clients_net.setLastModified(new Date());
        squid_clients_net.setDateCreated(new Date());
        metricDAO.save(squid_clients_net);

        Metric squid_clients_requests = new Metric();
        squid_clients_requests.setPlugin(squidPlugin);
        squid_clients_requests.setName("clients_requests");
        squid_clients_requests.setFriendlyName("Squid Client Requests");
        squid_clients_requests.setUnit("requests/s");
        squid_clients_requests.setLastModified(new Date());
        squid_clients_requests.setDateCreated(new Date());
        metricDAO.save(squid_clients_requests);

        Metric squid_servers_net = new Metric();
        squid_servers_net.setPlugin(squidPlugin);
        squid_servers_net.setName("servers_net");
        squid_servers_net.setFriendlyName("Squid Server Bandwidth");
        squid_servers_net.setUnit("kilobits/s");
        squid_servers_net.setLastModified(new Date());
        squid_servers_net.setDateCreated(new Date());
        metricDAO.save(squid_servers_net);

        Metric squid_servers_requests = new Metric();
        squid_servers_requests.setPlugin(squidPlugin);
        squid_servers_requests.setName("servers_requests");
        squid_servers_requests.setFriendlyName("Squid Server Requests");
        squid_servers_requests.setUnit("requests/s");
        squid_servers_requests.setLastModified(new Date());
        squid_servers_requests.setDateCreated(new Date());
        metricDAO.save(squid_servers_requests);

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
        mySQLIDRuleSet.setName("mysql");
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
                "INSERT INTO provider (id, name, date_created, last_modified, is_default, is_internal, status, provider_type) VALUES (-1, 'User-defined', now(), now(), false, true, 'ACTIVE', "
                        + userDefinedProviderType.getId() + ");");
        q.executeUpdate();

        // Policy-defined
        q = entityManager.createNativeQuery(
                "INSERT INTO provider (id, name, date_created, last_modified, is_default, is_internal, status, provider_type) VALUES (-2, 'Policy-defined', now(), now(), false, true, 'ACTIVE', "
                        + policyDefinedProviderType.getId() + ");");
        q.executeUpdate();

        _5gTelcoProvider = new Provider();
        _5gTelcoProvider.setName("MATILDA-enabled Telco Provider");
        _5gTelcoProvider.setDateCreated(new Date());
        _5gTelcoProvider.setLastModified(new Date());
        _5gTelcoProvider.setDomain(null);
        _5gTelcoProvider.setEndpoint(null);
        _5gTelcoProvider.setProject(null);
        _5gTelcoProvider.setMeshIdentifier(null);
        _5gTelcoProvider.setDefaultProvider(false);
        _5gTelcoProvider.setEnabled(Boolean.FALSE);
        _5gTelcoProvider.setPassword(Util.encrypt("!telcoprovider!", tokenSecret));
        _5gTelcoProvider.setUsername("telcoprovider");
        _5gTelcoProvider.setUser(admin);
        _5gTelcoProvider.setOrganization(organizationAdmin);
        _5gTelcoProvider.setProviderType(_5gProviderType);
        _5gTelcoProvider.setProxy("127.0.0.1:8085");
        _5gTelcoProvider.setInternalProvider(false);
        providerDAO.save(_5gTelcoProvider);

        Region _5gTelcoProviderGR = new Region();
        _5gTelcoProviderGR.setDateCreated(new Date());
        _5gTelcoProviderGR.setLastModified(new Date());
        _5gTelcoProviderGR.setName("it-genoa");
        _5gTelcoProviderGR.setProvider(_5gTelcoProvider);
        regionDAO.save(_5gTelcoProviderGR);

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
        iotGatewayProvider.setUsername("admin");
        iotGatewayProvider.setMeshIdentifier(UUID.randomUUID().toString());
        iotGatewayProvider.setUser(admin);
        iotGatewayProvider.setOrganization(organizationAdmin);
        iotGatewayProvider.setProviderType(iotGatewayProviderType);
        iotGatewayProvider.setInternalProvider(false);
        providerDAO.save(iotGatewayProvider);

        Region iotGatewayProviderGR = new Region();
        iotGatewayProviderGR.setDateCreated(new Date());
        iotGatewayProviderGR.setLastModified(new Date());
        iotGatewayProviderGR.setName("gr-athens");
        iotGatewayProviderGR.setProvider(iotGatewayProvider);
        regionDAO.save(iotGatewayProviderGR);

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

        ubiDell = new Provider();
        ubiDell.setName("UBIDELL");
        ubiDell.setDateCreated(new Date());
        ubiDell.setLastModified(new Date());
        ubiDell.setDomain("default");
        ubiDell.setEndpoint(openstackEndpoint);
        ubiDell.setProject("maestro");
        ubiDell.setDefaultProvider(true);
        ubiDell.setMeshIdentifier(null);
        ubiDell.setEnabled(Boolean.TRUE);
        ubiDell.setPassword(Util.encrypt("!maestro!", tokenSecret));
        ubiDell.setUsername("maestro");
        ubiDell.setUser(admin);
        ubiDell.setOrganization(organizationAdmin);
        ubiDell.setImageID("8528d050-3e6e-4c79-ac00-ae8c9da2dfdd");
        ubiDell.setNetworkID("40d260ad-7917-440e-810c-7b82bdc7a6e1");
        ubiDell.setProviderType(openstackProviderType);
        ubiDell.setInternalProvider(false);
        ubiDell.setPublicNetwork("provider");
        providerDAO.save(ubiDell);

        Region ubiDellGR = new Region();
        ubiDellGR.setDateCreated(new Date());
        ubiDellGR.setLastModified(new Date());
        ubiDellGR.setName("gr-athens");
        ubiDellGR.setProvider(ubiDell);
        regionDAO.save(ubiDellGR);

        //Added to Ubitech Admin user
        ubiDellUnicorn = new Provider();
        ubiDellUnicorn.setName("UBIDELLUnicorn");
        ubiDellUnicorn.setDateCreated(new Date());
        ubiDellUnicorn.setLastModified(new Date());
        ubiDellUnicorn.setDomain("default");
        ubiDellUnicorn.setEndpoint(openstackEndpoint);
        ubiDellUnicorn.setProject("unicorn");
        ubiDellUnicorn.setDefaultProvider(true);
        ubiDellUnicorn.setMeshIdentifier(null);
        ubiDellUnicorn.setEnabled(Boolean.TRUE);
        ubiDellUnicorn.setPassword(Util.encrypt("!unicorn!", tokenSecret));
        ubiDellUnicorn.setUsername("unicorn");
        ubiDellUnicorn.setUser(ubitechAdmin);
        ubiDellUnicorn.setOrganization(organizationUbitech);
        ubiDellUnicorn.setImageID("b4085493-f260-47af-b69f-89afd59abb48");
        ubiDellUnicorn.setNetworkID("412193dc-92bb-4b5a-b28f-6cc18aa9e3c9");
        ubiDellUnicorn.setProviderType(openstackProviderType);
        ubiDellUnicorn.setInternalProvider(false);
        ubiDellUnicorn.setPublicNetwork("provider");
        providerDAO.save(ubiDellUnicorn);

        Region ubiDellUnicornGR = new Region();
        ubiDellUnicornGR.setDateCreated(new Date());
        ubiDellUnicornGR.setLastModified(new Date());
        ubiDellUnicornGR.setName("gr-athens");
        ubiDellUnicornGR.setProvider(ubiDellUnicorn);
        regionDAO.save(ubiDellUnicornGR);

        ubiDellMatilda = new Provider();
        ubiDellMatilda.setName("UBIDELLMatilda");
        ubiDellMatilda.setDateCreated(new Date());
        ubiDellMatilda.setLastModified(new Date());
        ubiDellMatilda.setDomain("default");
        ubiDellMatilda.setEndpoint(openstackEndpoint);
        ubiDellMatilda.setProject("matilda");
        ubiDellMatilda.setDefaultProvider(false);
        ubiDellMatilda.setMeshIdentifier(null);
        ubiDellMatilda.setEnabled(Boolean.TRUE);
        ubiDellMatilda.setPassword(Util.encrypt("!matilda!", tokenSecret));
        ubiDellMatilda.setUsername("matilda");
        ubiDellMatilda.setUser(ubitechAdmin);
        ubiDellMatilda.setOrganization(organizationUbitech);
        ubiDellMatilda.setImageID("8528d050-3e6e-4c79-ac00-ae8c9da2dfdd");
        ubiDellMatilda.setNetworkID("f1cf2a31-6703-4be2-90fe-e6c312f08f40");
        ubiDellMatilda.setProviderType(openstackProviderType);
        ubiDellMatilda.setInternalProvider(false);
        ubiDellMatilda.setPublicNetwork("provider");
        providerDAO.save(ubiDellMatilda);

        Region ubiDellMatildaGR = new Region();
        ubiDellMatildaGR.setDateCreated(new Date());
        ubiDellMatildaGR.setLastModified(new Date());
        ubiDellMatildaGR.setName("gr-athens");
        ubiDellMatildaGR.setProvider(ubiDellMatilda);
        regionDAO.save(ubiDellMatildaGR);

        //Added to Ubitech Energy Group
        ubiTestProvider = new Provider();
        ubiTestProvider.setName("UBITestProvider");
        ubiTestProvider.setDateCreated(new Date());
        ubiTestProvider.setLastModified(new Date());
        ubiTestProvider.setDomain("default");
        ubiTestProvider.setEndpoint(openstackEndpoint);
        ubiTestProvider.setProject("maestro");
        ubiTestProvider.setDefaultProvider(true);
        ubiTestProvider.setMeshIdentifier(null);
        ubiTestProvider.setEnabled(Boolean.TRUE);
        ubiTestProvider.setPassword(Util.encrypt("!maestro!", tokenSecret));
        ubiTestProvider.setUsername("maestro");
        ubiTestProvider.setUser(ubitechEnergyAdmin);
        ubiTestProvider.setOrganization(organizationUbitechEnergy);
        ubiTestProvider.setImageID("8528d050-3e6e-4c79-ac00-ae8c9da2dfdd");
        ubiTestProvider.setNetworkID("f1cf2a31-6703-4be2-90fe-e6c312f08f40");
        ubiTestProvider.setProviderType(openstackProviderType);
        ubiTestProvider.setInternalProvider(false);
        ubiTestProvider.setPublicNetwork("provider");
        providerDAO.save(ubiTestProvider);

        Region ubiTestProviderGR = new Region();
        ubiTestProviderGR.setDateCreated(new Date());
        ubiTestProviderGR.setLastModified(new Date());
        ubiTestProviderGR.setName("gr-athens");
        ubiTestProviderGR.setProvider(ubiTestProvider);
        regionDAO.save(ubiTestProviderGR);

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
        rainbowKubernetesTestProviderGR.setName("gr-athens");
        rainbowKubernetesTestProviderGR.setProvider(rainbowKubernetesProvider);
        regionDAO.save(rainbowKubernetesTestProviderGR);

        Provider rMarketProvider = new Provider();
        rMarketProvider.setName("R-MARKET");
        rMarketProvider.setDateCreated(new Date());
        rMarketProvider.setLastModified(new Date());
        rMarketProvider.setDomain("default");
        rMarketProvider.setEndpoint(null);
        rMarketProvider.setProject("datacloud");
        rMarketProvider.setDefaultProvider(false);
        rMarketProvider.setMeshIdentifier(null);
        rMarketProvider.setEnabled(Boolean.TRUE);
        rMarketProvider.setPassword(Util.encrypt("dataCldl!3", tokenSecret));
        rMarketProvider.setUsername("datacloud");
        rMarketProvider.setUser(admin);
        rMarketProvider.setOrganization(organizationAdmin);
        rMarketProvider.setImageID("rmarketImageID");
        rMarketProvider.setNetworkID("rmarketNetworkID");
        rMarketProvider.setProviderType(openstackProviderType);
        rMarketProvider.setInternalProvider(false);
        rMarketProvider.setPublicNetwork("provider");
        providerDAO.save(rMarketProvider);

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

        SSHKey ubitechAdminKey = new SSHKey();
        ubitechAdminKey.setDefaultSSH(true);
        ubitechAdminKey.setFriendlyName("ubitechAdminKey");
        ubitechAdminKey.setLastModified(new Date());
        ubitechAdminKey.setDateCreated(new Date());
        ubitechAdminKey.setSshKey(
                "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC1uCM+WTrtr82waAez9ZrPnvIyArAVk5AxOXkS8E4genxu1D7YKK2prLDl8xOzOJmrWkfHtzSSDH+quxAaHCmdz/fMsiDt3CzsRLn1f5GqXy4BM+IvVd8M4585s/hRUmDEGohlt9Ro3gnE65zPAjz35pwkBLIWXZ4tSbhQnUJEjMlKjAtsEXIlJeC1YSTMNhvaBnB1qOMxwZbOUEdhxlbezIVlugLxrdhlEDDJBpR5xwSDgFwEKO/Z9eE+cLAVNEetShenHZevctMHcdEGFBie8MOF2hiKD3kKcPykH6ULPw2hs9seiIz51coGAcX5k3YHxvi7ngVqHjuQsRqUIe97 admin");
        ubitechAdminKey.setUser(ubitechAdmin);
        ubitechAdminKey.setOrganization(organizationUbitech);
        sshKeyDAO.save(ubitechAdminKey);

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

        //Initialize pilot ppdr
        initializePilotPPDR();

        // Initialize Echo component
        initializeHttpEchoComponent();

        logger.info("Components have been added successfully!");

    }

    private void initializeBalancerAndLambdaProxyComponents() {

        logger.info("Load balancer and lambda proxy initialization has started...");

        List<CapabilityDrop> capabilitiesDrop = new ArrayList<>();
        capabilitiesDrop.add(CapabilityDrop.DAC_OVERRIDE);
        capabilitiesDrop.add(CapabilityDrop.FSETID);

        List<CapabilityAdd> capabilitiesAdd = new ArrayList<>();
        capabilitiesAdd.add(CapabilityAdd.SYS_MODULE);
        capabilitiesAdd.add(CapabilityAdd.SYS_TTY_CONFIG);

        // Load Balancer (Traefik)
        loadBalancer = new eu.orchestrator.repository.domain.Component();
        loadBalancer.setName("Traefik");
        loadBalancer.setHexID(Util.createRandomHEXString());
        loadBalancer.setDockerImage("traefik:alpine");
        loadBalancer.setDockerRegistry("");
        loadBalancer.setDockerUsername("maestro");
        loadBalancer.setDockerPassword(Util.encrypt("!maestro!$", secretToken));
        loadBalancer.setCapabilityDrops(capabilitiesDrop);
        loadBalancer.setCapabilityAdds(capabilitiesAdd);
        loadBalancer.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        loadBalancer.setPublicComponent(true);
        loadBalancer.setDateCreated(new Date());
        loadBalancer.setLastModified(new Date());
        loadBalancer.setUser(admin);
        loadBalancer.setOrganization(organizationAdmin);
        loadBalancer.setElasticityController("NONE");

        loadBalancer.setPlugins(new TreeSet<>(listOfDefaultPlugins));

        componentDAO.save(loadBalancer);

        loadBalancer.setLabels(new TreeSet<>(Arrays.asList(labelLB)));

        // Exposed Interface
        Interface interfaceLBUI = new Interface();
        interfaceLBUI.setDateCreated(new Date());
        interfaceLBUI.setLastModified(new Date());
        interfaceLBUI.setInterfaceType(Interface.InterfaceType.ACCESS.name());
        interfaceLBUI.setName("traefikUI");
        interfaceLBUI.setPort("15568");
        interfaceLBUI.setVna("VNA0");
        interfaceLBUI.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceLBUI.setComponent(loadBalancer);
        interfaceDAO.save(interfaceLBUI);

        // Minimum Execution Requirements
        Requirement requirementLB = new Requirement();
        requirementLB.setComponent(loadBalancer);
        requirementLB.setDateCreated(new Date());
        requirementLB.setLastModified(new Date());
        requirementLB.setGpuRequired(false);
        requirementLB.setHypervisorType("ESXI");
        requirementLB.setRam(2048);
        requirementLB.setStorage(20);
        requirementLB.setvCPUs(1);
        requirementDAO.save(requirementLB);

        // Health Check
        HealthCheck loadBalancerHealthCheck = new HealthCheck();
        loadBalancerHealthCheck.setComponent(loadBalancer);
        loadBalancerHealthCheck.setDateCreated(new Date());
        loadBalancerHealthCheck.setLastModified(new Date());
        loadBalancerHealthCheck.setName("LoadBalancerHealthCheck");
        loadBalancerHealthCheck.setInterval(new Long(10));
        loadBalancerHealthCheck.setArgs(null);
        loadBalancerHealthCheck.setHttpURL("http://localhost:15568/metrics");
        healthCheckDAO.save(loadBalancerHealthCheck);

        componentDAO.save(loadBalancer);

        // Lambda Proxy
        lambdaProxy = new eu.orchestrator.repository.domain.Component();
        lambdaProxy.setName("LambdaProxy");
        lambdaProxy.setHexID(Util.createRandomHEXString());
        lambdaProxy.setDockerImage("traefik:v1.7");
        lambdaProxy.setDockerRegistry("");
        lambdaProxy.setDockerUsername("maestro");
        lambdaProxy.setDockerPassword(Util.encrypt("!maestro!$", secretToken));
        lambdaProxy.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        lambdaProxy.setPublicComponent(true);
        lambdaProxy.setDateCreated(new Date());
        lambdaProxy.setLastModified(new Date());
        lambdaProxy.setUser(admin);
        lambdaProxy.setOrganization(organizationAdmin);
        lambdaProxy.setElasticityController("NONE");
        lambdaProxy.setPlugins(new TreeSet<>(listOfDefaultPlugins));

        componentDAO.save(lambdaProxy);

        lambdaProxy.setLabels(new TreeSet<>(Arrays.asList(labelLB)));

        // Exposed Interface
        Interface interfaceLambdaProxyUI = new Interface();
        interfaceLambdaProxyUI.setDateCreated(new Date());
        interfaceLambdaProxyUI.setLastModified(new Date());
        interfaceLambdaProxyUI.setInterfaceType(Interface.InterfaceType.ACCESS.name());
        interfaceLambdaProxyUI.setName("lambdaProxyUI");
        interfaceLambdaProxyUI.setPort("15568");
        interfaceLambdaProxyUI.setVna("VNA0");
        interfaceLambdaProxyUI.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfaceLambdaProxyUI.setComponent(lambdaProxy);
        interfaceDAO.save(interfaceLambdaProxyUI);

        // Minimum Execution Requirements
        Requirement requirementLP = new Requirement();
        requirementLP.setComponent(lambdaProxy);
        requirementLP.setDateCreated(new Date());
        requirementLP.setLastModified(new Date());
        requirementLP.setGpuRequired(false);
        requirementLP.setHypervisorType("ESXI");
        requirementLP.setRam(2048);
        requirementLP.setStorage(20);
        requirementLP.setvCPUs(1);
        requirementDAO.save(requirementLP);

        // Health Check
        HealthCheck loadProxyHealthCheck = new HealthCheck();
        loadProxyHealthCheck.setComponent(lambdaProxy);
        loadProxyHealthCheck.setDateCreated(new Date());
        loadProxyHealthCheck.setLastModified(new Date());
        loadProxyHealthCheck.setName("LambdaProxyHealthCheck");
        loadProxyHealthCheck.setInterval(new Long(10));
        loadProxyHealthCheck.setArgs(null);
        loadProxyHealthCheck.setHttpURL("http://localhost:15568/metrics");
        healthCheckDAO.save(loadProxyHealthCheck);

        componentDAO.save(lambdaProxy);

        logger.info("Load balancer and lambda proxy have been added successfully!");

    }

    private void initializeDBComponents() {

        logger.info("Database components initialization has started...");

        // MariaDB
        mariaDB = new eu.orchestrator.repository.domain.Component();
        mariaDB.setName("MariaDB");
        mariaDB.setHexID(Util.createRandomHEXString());
        mariaDB.setDockerImage("mariadb:10.2.14");
        mariaDB.setDockerRegistry("");
        mariaDB.setDockerUsername("maestro");
        mariaDB.setDockerPassword(Util.encrypt("!maestro!$", secretToken));
        mariaDB.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.AMD64.getFriendlyName());
        mariaDB.setPublicComponent(true);
        mariaDB.setDateCreated(new Date());
        mariaDB.setLastModified(new Date());
        mariaDB.setUser(admin);
        mariaDB.setOrganization(organizationAdmin);
        mariaDB.setElasticityController("NONE");
        mariaDB.setPlugins(new TreeSet<>(listOfDefaultPlugins));

        componentDAO.save(mariaDB);

        // Labels
        mariaDB.setLabels(new TreeSet<>(Arrays.asList(labelSQLDB)));

        // Environmental Variables
        EnvironmentalVariable mariaDBEnvironmentalVariableA = new EnvironmentalVariable();
        mariaDBEnvironmentalVariableA.setComponent(mariaDB);
        mariaDBEnvironmentalVariableA.setDateCreated(new Date());
        mariaDBEnvironmentalVariableA.setLastModified(new Date());
        mariaDBEnvironmentalVariableA.setKey("MYSQL_DATABASE");

        mariaDBEnvironmentalVariableA.setValue("wordpress");
        environmentalVariableDAO.save(mariaDBEnvironmentalVariableA);

        EnvironmentalVariable mariaDBEnvironmentalVariableB = new EnvironmentalVariable();
        mariaDBEnvironmentalVariableB.setComponent(mariaDB);
        mariaDBEnvironmentalVariableB.setDateCreated(new Date());
        mariaDBEnvironmentalVariableB.setLastModified(new Date());
        mariaDBEnvironmentalVariableB.setKey("MYSQL_USER");
        mariaDBEnvironmentalVariableB.setValue("wordpress");
        environmentalVariableDAO.save(mariaDBEnvironmentalVariableB);

        EnvironmentalVariable mariaDBEnvironmentalVariableC = new EnvironmentalVariable();
        mariaDBEnvironmentalVariableC.setComponent(mariaDB);
        mariaDBEnvironmentalVariableC.setDateCreated(new Date());
        mariaDBEnvironmentalVariableC.setLastModified(new Date());
        mariaDBEnvironmentalVariableC.setKey("MYSQL_PASSWORD");
        mariaDBEnvironmentalVariableC.setValue("wordpress");
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
        mariaDBHealthCheck.setInterval(new Long(10));
        mariaDBHealthCheck.setArgs("mysqladmin -uroot -pxUNvbFUbHvv6hnkrTg86g7fXe87W9fTg status");
        mariaDBHealthCheck.setHttpURL(null);
        healthCheckDAO.save(mariaDBHealthCheck);

        componentDAO.save(mariaDB);

        // Mongo DB
        mongoDB = new eu.orchestrator.repository.domain.Component();
        mongoDB.setName("MongoDB");
        mongoDB.setHexID(Util.createRandomHEXString());
        mongoDB.setDockerImage("mongo:rc");
        mongoDB.setDockerRegistry("");
        mongoDB.setDockerUsername("maestro");
        mongoDB.setDockerPassword(Util.encrypt("!maestro!$", secretToken));
        mongoDB.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        mongoDB.setPublicComponent(true);
        mongoDB.setDateCreated(new Date());
        mongoDB.setLastModified(new Date());
        mongoDB.setUser(admin);
        mongoDB.setOrganization(organizationAdmin);
        mongoDB.setElasticityController("NONE");
        mongoDB.setPlugins(new TreeSet<>(listOfDefaultPlugins));

        componentDAO.save(mongoDB);

        // Labels
        mongoDB.setLabels(new TreeSet<>(Arrays.asList(labelNoSQL)));

        // Environmental Variables
        EnvironmentalVariable mongoDBEnvironmentalVariableA = new EnvironmentalVariable();
        mongoDBEnvironmentalVariableA.setComponent(mongoDB);
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
        interfaceNoSQL.setPort("27017");
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
        mongoDBHealthCheck.setInterval(new Long(10));
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
        phpMyAdmin.setDockerRegistry("");
        phpMyAdmin.setDockerUsername("maestro");
        phpMyAdmin.setDockerPassword(Util.encrypt("!maestro!$", secretToken));
        phpMyAdmin.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.AMD64.getFriendlyName());
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
        phpMyAdminHealthCheck.setInterval(new Long(10));
        phpMyAdminHealthCheck.setArgs(null);
        phpMyAdminHealthCheck.setHttpURL("http://localhost:80");
        healthCheckDAO.save(phpMyAdminHealthCheck);

        componentDAO.save(phpMyAdmin);

        // WordPress
        wordPress = new eu.orchestrator.repository.domain.Component();
        wordPress.setName("WordPress");
        wordPress.setHexID(Util.createRandomHEXString());
        wordPress.setDockerImage("wordpress:4");
        wordPress.setDockerRegistry("");
        wordPress.setDockerUsername("maestro");
        wordPress.setDockerPassword(Util.encrypt("!maestro!$", secretToken));
        wordPress.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.AMD64.name());
        wordPress.setPublicComponent(true);
        wordPress.setDateCreated(new Date());
        wordPress.setLastModified(new Date());
        wordPress.setUser(admin);
        wordPress.setOrganization(organizationAdmin);
        wordPress.setElasticityController("HORIZONTAL");
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
        wordPressEnvironmentalVariableA.setValue("wordpress");
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
        wordPressEnvironmentalVariableC.setValue("wordpress");
        environmentalVariableDAO.save(wordPressEnvironmentalVariableC);

        // Exposed Interface
        Interface interfaceWpHttp = new Interface();
        interfaceWpHttp.setDateCreated(new Date());
        interfaceWpHttp.setLastModified(new Date());
        interfaceWpHttp.setInterfaceType(Interface.InterfaceType.ACCESS.name());
        interfaceWpHttp.setName("phpMyAdminAccessInterface");
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
        wordPressHealthCheck.setInterval(new Long(10));
        wordPressHealthCheck.setArgs(null);
        wordPressHealthCheck.setHttpURL("http://localhost:80");
        healthCheckDAO.save(wordPressHealthCheck);

        componentDAO.save(wordPress);

        logger.info("PHP components have been added successfully!");

    }

    private void initializePilotPPDR() {

        // ppdr Database
        ppdrDatabase = new eu.orchestrator.repository.domain.Component();
        ppdrDatabase.setName("PPDRDatabase");
        ppdrDatabase.setHexID(Util.createRandomHEXString());
        ppdrDatabase.setDockerImage("mysql:5.5");
        ppdrDatabase.setDockerRegistry("");
        ppdrDatabase.setDockerUsername("maestro");
        ppdrDatabase.setDockerPassword(Util.encrypt("!maestro!$", secretToken));
        ppdrDatabase.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        ppdrDatabase.setPublicComponent(false);
        ppdrDatabase.setDateCreated(new Date());
        ppdrDatabase.setLastModified(new Date());
        ppdrDatabase.setUser(ubitechUserA);
        ppdrDatabase.setOrganization(organizationUbitech);
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
        mySQLEnvironmentalVariableD.setValue("matilda");
        environmentalVariableDAO.save(mySQLEnvironmentalVariableD);

        // Exposed Interface
        interfacePPDRSQL = new Interface();
        interfacePPDRSQL.setDateCreated(new Date());
        interfacePPDRSQL.setLastModified(new Date());
        interfacePPDRSQL.setInterfaceType(Interface.InterfaceType.CORE.name());
        interfacePPDRSQL.setName("ppdrSqlInterface");
        interfacePPDRSQL.setPort("3306");
        interfacePPDRSQL.setVna("VNA0");
        interfacePPDRSQL.setTransmissionProtocol(Interface.TransmissionProtocol.TCP.name());
        interfacePPDRSQL.setComponent(ppdrDatabase);
        interfaceDAO.save(interfacePPDRSQL);

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
        mySQLHealthCheck.setInterval(new Long(10));
        mySQLHealthCheck.setArgs("mysqladmin -uroot -pmatilda status");
        mySQLHealthCheck.setHttpURL(null);
        healthCheckDAO.save(mySQLHealthCheck);

        componentDAO.save(ppdrDatabase);

        // ppdr samba
        ppdrSamba = new eu.orchestrator.repository.domain.Component();
        ppdrSamba.setName("PPDRSamba");
        ppdrSamba.setHexID(Util.createRandomHEXString());
        ppdrSamba.setDockerImage("dperson/samba");
        ppdrSamba.setDockerRegistry("");
        ppdrSamba.setDockerUsername("maestro");
        ppdrSamba.setDockerPassword(Util.encrypt("!maestro!$", secretToken));
        ppdrSamba.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        ppdrSamba.setPublicComponent(false);
        ppdrSamba.setDateCreated(new Date());
        ppdrSamba.setLastModified(new Date());
        ppdrSamba.setUser(ubitechUserA);
        ppdrSamba.setOrganization(organizationUbitech);
        ppdrSamba.setElasticityController("NONE");
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
        sambaHealthCheck.setInterval(new Long(10));
        sambaHealthCheck.setArgs("ps");
        sambaHealthCheck.setHttpURL(null);
        healthCheckDAO.save(sambaHealthCheck);

        componentDAO.save(ppdrSamba);

        // ppdr PhpDashboard
        ppdrPhpDashboard = new eu.orchestrator.repository.domain.Component();
        ppdrPhpDashboard.setName("PPDRPhpDashboard");
        ppdrPhpDashboard.setHexID(Util.createRandomHEXString());
        ppdrPhpDashboard.setDockerImage("phpdashboard:1.2.0");
        ppdrPhpDashboard.setDockerRegistry("");
        ppdrPhpDashboard.setDockerUsername("maestro");
        ppdrPhpDashboard.setDockerPassword(Util.encrypt("!maestro!$", secretToken));
        ppdrPhpDashboard.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        ppdrPhpDashboard.setPublicComponent(false);
        ppdrPhpDashboard.setDateCreated(new Date());
        ppdrPhpDashboard.setLastModified(new Date());
        ppdrPhpDashboard.setUser(ubitechUserA);
        ppdrPhpDashboard.setOrganization(organizationUbitech);
        ppdrPhpDashboard.setElasticityController("HORIZONTAL");
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
        phpDashboardEnvironmentalVariableF.setValue("matilda");
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
        phpDashboardGraphLinkSQLInterface.setInterfaceObj(interfacePPDRSQL);
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
        phpDashboardHealthCheck.setInterval(new Long(10));
        phpDashboardHealthCheck.setArgs(null);
        phpDashboardHealthCheck.setHttpURL("http://localhost:80");
        healthCheckDAO.save(phpDashboardHealthCheck);

        componentDAO.save(ppdrPhpDashboard);

        // Application

        ppdrApplication = new eu.orchestrator.repository.domain.Application();
        ppdrApplication.setName("PPDR");
        ppdrApplication.setHexID(Util.createRandomHEXString());
        ppdrApplication.setDateCreated(new Date());
        ppdrApplication.setUser(ubitechUserA);
        ppdrApplication.setOrganization(organizationUbitech);
        ppdrApplication.setPublicApplication(false);
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

    }

    private void initializeHttpEchoComponent() {

        logger.info("Echo component initialization has started...");

        //Http-echo

        httpEcho = new eu.orchestrator.repository.domain.Component();
        httpEcho.setName("HttpEcho");
        httpEcho.setHexID(Util.createRandomHEXString());
        httpEcho.setDockerImage("hashicorp/http-echo:0.2.3");
        httpEcho.setDockerRegistry("");
        httpEcho.setDockerUsername("maestro");
        httpEcho.setDockerPassword(Util.encrypt("!maestro!", secretToken));
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
        httpEchoHealthCheck.setHttpURL("http://localhost:5678");
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
        dbmsMariaDB.setName("MariaDB");
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
        cmsMariaDB.setName("MariaDB");
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
}
