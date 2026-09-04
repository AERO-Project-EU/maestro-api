package eu.orchestrator.backend.service.application;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.util.ConstantsUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.ApplicationDAO;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ComponentDAO;
import eu.orchestrator.repository.dao.ComponentNodeDAO;
import eu.orchestrator.repository.dao.GraphLinkDAO;
import eu.orchestrator.repository.dao.GraphLinkNodeDAO;
import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.GraphLink;
import eu.orchestrator.repository.domain.GraphLinkNode;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.transfer.ApplicationGraphTO;
import eu.orchestrator.backend.transfer.ComponentNodeTO;
import eu.orchestrator.backend.transfer.ComponentTO;
import eu.orchestrator.backend.transfer.DashboardTO;
import eu.orchestrator.backend.transfer.GraphLinkNodeTO;
import eu.orchestrator.backend.transfer.GraphLinkTO;
import eu.orchestrator.backend.transfer.InterfaceTO;
import eu.orchestrator.backend.transfer.TOConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QApplication.application;

@Service
@Transactional(rollbackOn = Exception.class)
public class ApplicationService {

    private static final Logger logger = Logger.getLogger(ApplicationService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private ComponentDAO componentDAO;

    @Autowired
    private ComponentNodeDAO componentNodeDAO;

    @Autowired
    private GraphLinkDAO graphLinkDAO;

    @Autowired
    private GraphLinkNodeDAO graphLinkNodeDAO;

    @Autowired
    private ApplicationInstanceDAO applicationInstanceDAO;

    @Autowired
    private SimpMessagingTemplate wsTemplate;


    public Application fetchApplicationById(Long id) {
        Optional<Application> applicationOptional = applicationDAO.findById(id);
        return applicationOptional.orElse(null);
    }

    public Page applicationList(String filters, Application fApplication, Pageable pageable, User authenticatedUser) {
        BooleanExpression predicate = application.eq(application);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                Application filterApplication = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Application.class);
                if (null != filterApplication) {
                    proceedWithRequestBody = false;
                    if (null != filterApplication.getName() && !filterApplication.getName().isEmpty()) {
                        predicate = predicate.and(application.name.containsIgnoreCase(filterApplication.getName()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        if (proceedWithRequestBody && null != fApplication && null != fApplication.getName() && !fApplication.getName().isEmpty()) {
            predicate = predicate.and(application.name.containsIgnoreCase(fApplication.getName()));
        }
        if (!authenticatedUser.isAdmin()) {
            predicate = predicate.and(application.organization.eq(authenticatedUser.getOrganization()).or(application.publicApplication.eq(true)));
        }

        Page<Application> page;
        if (pageable.getPageSize() > 100) {
            page = applicationDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = applicationDAO.findAll(predicate, pageable);
        }
        return new TOConverter(Application.class.getName(), page, pageable, authenticatedUser).convertToTOWithPermissions();
    }

    public boolean checkIfApplicationNameExists(String name, User authenticatedUser) {
        // Check if application already exists
        if (authenticatedUser.isAdmin()) {
            //The application name must be unique cross platform for the Admin
            if (applicationDAO.findByName(name).isPresent()) {
                return Boolean.TRUE;
            }

        } else {
            //The application name must be unique in the organization AND in the public applications
            if (applicationDAO.findByNameAndOrganization(name, authenticatedUser.getOrganization()).isPresent()
                    || applicationDAO.findByNameAndPublicApplication(name, true).isPresent()) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;

    }

    public List<Application> fetchApplicationsByInstance(User authenticatedUser) {
        BooleanExpression predicate = application.eq(application);

        // todo - get only the application that used on the created application instances

        if (!authenticatedUser.isAdmin()) {

            predicate = predicate.and(application.organization.eq(authenticatedUser.getOrganization())
                    .or(application.publicApplication.eq(true)));
        }

        List<Application> applications = new ArrayList<>();
        applicationDAO.findAll(predicate).iterator().forEachRemaining(applications::add);

        return applications;
    }

    public List<Application> fetchApplicationsByPredicate(BooleanExpression predicate) {
        return (List<Application>) applicationDAO.findAll(predicate);
    }

    public ApplicationGraphTO fetchById(Long id, User authenticatedUser) {
        Optional<Application> applicationOP = applicationDAO.findById(id);

        if (applicationOP.isPresent() &&
                (authenticatedUser.isAdmin() ||
                        applicationOP.get().getPublicApplication() ||
                        applicationOP.get().getOrganization().equals(authenticatedUser.getOrganization()))) {

            Application exApplication = applicationOP.get();

            ApplicationGraphTO applicationGraphTO = new ApplicationGraphTO();
            applicationGraphTO.setId(exApplication.getId());
            applicationGraphTO.setHexID(exApplication.getHexID());
            applicationGraphTO.setName(exApplication.getName());
            applicationGraphTO.setPublicApplication(exApplication.getPublicApplication());
            applicationGraphTO.setOrganization(exApplication.getOrganization().getName());

            applicationGraphTO.setComponentNodes(componentNodeTOApplicationGraphTO(exApplication));

            if (null != exApplication.getGraphLinkNodes() && !exApplication.getGraphLinkNodes()
                    .isEmpty()) {
                applicationGraphTO.setGraphLinkNodes(graphLinkNodeTOApplicationGraphTO(exApplication));
            } else {
                applicationGraphTO.setGraphLinkNodes(null);
            }

            return applicationGraphTO;

        }

        return null;
    }

    public List<Application> fetchAllByComponentNodesIsIn(List<ComponentNode> componentNodes) {
        return applicationDAO.findAllByComponentNodesIsIn(componentNodes, null).getContent();
    }

    public void create(Application application, User authenticatedUser) {
        //Only the Admin can create a public Application
        if (Boolean.TRUE.equals(application.getPublicApplication()) && !authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.PUBLIC_APPLICATION_NOT_AUTHORIZED.getCode(), GenericMessage.PUBLIC_APPLICATION_NOT_AUTHORIZED);
        }
        if (checkIfApplicationNameExists(application.getName(), authenticatedUser)) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_ALREADY_EXISTS.getCode(), GenericMessage.APPLICATION_ALREADY_EXISTS);
        }

        // Check if component nodes have the same names
        List<String> cnNames = new ArrayList<>();
        for (ComponentNode cn : application.getComponentNodes()) {
            if (null != cn.getName() && !cn.getName().isEmpty()) {
                if (cnNames.contains(cn.getName())) {
                    throw new GenericBusinessException(GenericMessage.COMPONENT_ALREADY_EXISTS.getCode(), GenericMessage.COMPONENT_ALREADY_EXISTS);
                } else {
                    cnNames.add(cn.getName());
                }
            } else {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }
        }

        List<ComponentNode> newComponentNodes = application.getComponentNodes();
        application.setComponentNodes(null);
        List<GraphLinkNode> newGraphLinkNodes = null;
        if (NullCheckUtil.isNotEmpty(application.getGraphLinkNodes())) {
            newGraphLinkNodes = application.getGraphLinkNodes();
            application.setGraphLinkNodes(null);
        }

        application.setDateCreated(new Date());
        application.setHexID(Util.createRandomHEXString());
        application.setLastModified(new Date());
        application.setUser(authenticatedUser);
        application.setOrganization(authenticatedUser.getOrganization());
        applicationDAO.save(application);

        // Store Component Nodes
        storeComponentNodes(newComponentNodes, application);
        application.setComponentNodes(newComponentNodes);

        // Store GraphLinkNodes if exist
        if (NullCheckUtil.isNotEmpty(newGraphLinkNodes)) {
            storeGraphicLinkNodes(newGraphLinkNodes, application);
            application.setGraphLinkNodes(newGraphLinkNodes);
        }

        try {
            DashboardTO dashboardTO = new DashboardTO();
            dashboardTO.setOverview(true);
            String notificationAsString;
            notificationAsString = objectMapper.writeValueAsString(dashboardTO);
            wsTemplate.convertAndSend(ConstantsUtil.DASHBOARD_TOPIC, notificationAsString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void update(Application application, User authenticatedUser) {
        //Only the Admin can create a public Application
        if (Boolean.TRUE.equals(application.getPublicApplication()) && !authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.PUBLIC_APPLICATION_NOT_AUTHORIZED.getCode(), GenericMessage.PUBLIC_APPLICATION_NOT_AUTHORIZED);
        }
        // Check if application already exists or not
        Optional<Application> existingApplicationOP = applicationDAO.findById(application.getId());
        if (!existingApplicationOP.isPresent()) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_NOT_EXIST.getCode(), GenericMessage.APPLICATION_NOT_EXIST);
        }
        //TODO we must check if there is a running instance of this graph
        if (Boolean.TRUE.equals(existingApplicationOP.get().hasEditAllowance(authenticatedUser))) {
            Application existingApplication = existingApplicationOP.get();
            if (!existingApplication.getName().equals(application.getName()) && checkIfApplicationNameExists(application.getName(), authenticatedUser)) {
                throw new GenericBusinessException(GenericMessage.APPLICATION_ALREADY_EXISTS.getCode(), GenericMessage.APPLICATION_ALREADY_EXISTS);
            }
            // TODO - application update missing logic
            // applicationDAO.save(existingApplication);

        } else {
            throw new NotAuthorizedException(GenericMessage.PUBLIC_APPLICATION_NOT_AUTHORIZED.getCode(), GenericMessage.PUBLIC_APPLICATION_NOT_AUTHORIZED);
        }
    }

    public void delete(Long id, User authenticatedUser) {
        // Check if application already exists or not
        Optional<Application> existingApplicationOP = applicationDAO.findById(id);
        if (existingApplicationOP.isPresent() && Boolean.TRUE.equals(existingApplicationOP.get().hasDeleteAllowance(authenticatedUser))) {
            Application existingApplication = existingApplicationOP.get();
            // TODO - Check if Application is used in ApplicationInstance
            List<ApplicationInstance> applicationInstances = applicationInstanceDAO.findAllByApplication(existingApplication);
            if (null != applicationInstances && !applicationInstances.isEmpty()) {
                throw new GenericBusinessException(GenericMessage.APPLICATION_USED_IN_APPLICATION_INSTANCES.getCode(),
                        GenericMessage.APPLICATION_USED_IN_APPLICATION_INSTANCES);
            }
            if (null != existingApplication.getComponentNodes() && !existingApplication.getComponentNodes().isEmpty()) {
                existingApplication.getComponentNodes().forEach(componentNode -> componentNodeDAO.delete(componentNode));
            }
            if (null != existingApplication.getGraphLinkNodes() && !existingApplication.getGraphLinkNodes().isEmpty()) {
                existingApplication.getGraphLinkNodes().forEach(graphLinkNode -> graphLinkNodeDAO.delete(graphLinkNode));
            }
            applicationDAO.delete(existingApplication);
            try {
                DashboardTO dashboardTO = new DashboardTO();
                dashboardTO.setOverview(true);
                String notificationAsString;
                notificationAsString = objectMapper.writeValueAsString(dashboardTO);
                wsTemplate.convertAndSend(ConstantsUtil.DASHBOARD_TOPIC, notificationAsString);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            throw new NotAuthorizedException(GenericMessage.APPLICATION_NOT_AUTHORIZED.getCode(), GenericMessage.APPLICATION_NOT_AUTHORIZED);
        }
    }

    public Long count(User authenticatedUser) {
        return applicationDAO.calculateApplications(authenticatedUser.getOrganization().getId(), true);
    }


    /*
     * Privates
     */


    private List<ComponentNodeTO> componentNodeTOApplicationGraphTO(Application exApplication) {

        List<ComponentNodeTO> componentNodeTOs = new ArrayList<>();
        for (ComponentNode componentNode : exApplication.getComponentNodes()) {

            ComponentNodeTO componentNodeTO = new ComponentNodeTO();
            componentNodeTO.setComponentNodeID(componentNode.getComponentNodeID());
            componentNodeTO.setHexID(componentNode.getHexID());
            componentNodeTO.setName(componentNode.getName());

            ComponentTO componentTO = new ComponentTO();
            componentTO.setId(componentNode.getComponent().getId());
            componentTO.setName(componentNode.getComponent().getName());
            componentTO.setHexID(componentNode.getComponent().getHexID());
            componentTO.setPublicComponent(componentNode.getComponent().getPublicComponent());
            componentTO.setOrganization(componentNode.getComponent().getOrganization().getName());

            List<InterfaceTO> exposedInterfacesTO;
            if (null != componentNode.getComponent().getExposedInterfaces() && !componentNode
                    .getComponent().getExposedInterfaces().isEmpty()) {
                exposedInterfacesTO = new ArrayList<>();

                for (Interface expInterface : componentNode.getComponent().getExposedInterfaces()) {

                    InterfaceTO interfaceTO = new InterfaceTO();
                    interfaceTO.setInterfaceID(expInterface.getInterfaceID());
                    interfaceTO.setName(expInterface.getName());
                    interfaceTO.setPort(expInterface.getPort());
                    interfaceTO.setInterfaceType(expInterface.getInterfaceType());
                    interfaceTO.setTransmissionProtocol(expInterface.getTransmissionProtocol());
                    exposedInterfacesTO.add(interfaceTO);

                }

            } else {
                exposedInterfacesTO = null;
            }

            componentTO.setExposedInterfaces(exposedInterfacesTO);

            componentNodeTO.setComponent(componentTO);
            componentNodeTOs.add(componentNodeTO);

        }

        return componentNodeTOs;

    }


    private List<GraphLinkNodeTO> graphLinkNodeTOApplicationGraphTO(Application exApplication) {
        List<GraphLinkNodeTO> graphLinkNodeTOs = new ArrayList<>();
        for (GraphLinkNode graphLinkNode : exApplication.getGraphLinkNodes()) {

            GraphLinkNodeTO graphLinkNodeTO = new GraphLinkNodeTO();
            graphLinkNodeTO.setGraphLinkNodeID(graphLinkNode.getGraphLinkNodeID());

            GraphLinkTO graphLinkTO = new GraphLinkTO();
            graphLinkTO.setGraphLinkID(graphLinkNode.getGraphLink().getGraphLinkID());
            graphLinkTO.setFriendlyName(graphLinkNode.getGraphLink().getFriendlyName());

            InterfaceTO interfaceTO = new InterfaceTO();
            interfaceTO
                    .setInterfaceID(graphLinkNode.getGraphLink().getInterfaceObj().getInterfaceID());
            interfaceTO.setTransmissionProtocol(
                    graphLinkNode.getGraphLink().getInterfaceObj().getTransmissionProtocol());
            interfaceTO.setPort(graphLinkNode.getGraphLink().getInterfaceObj().getPort());
            interfaceTO.setInterfaceType(
                    graphLinkNode.getGraphLink().getInterfaceObj().getInterfaceType());
            interfaceTO.setName(graphLinkNode.getGraphLink().getInterfaceObj().getName());
            graphLinkTO.setInterfaceObj(interfaceTO);

            graphLinkNodeTO.setGraphLink(graphLinkTO);

            ComponentNodeTO componentNodeFromTO = new ComponentNodeTO();
            componentNodeFromTO
                    .setComponentNodeID(graphLinkNode.getComponentNodeFrom().getComponentNodeID());
            componentNodeFromTO.setHexID(graphLinkNode.getComponentNodeFrom().getHexID());
            componentNodeFromTO.setName(graphLinkNode.getComponentNodeFrom().getName());

            ComponentTO componentFromTO = new ComponentTO();
            componentFromTO.setId(graphLinkNode.getComponentNodeFrom().getComponent().getId());
            componentFromTO
                    .setName(graphLinkNode.getComponentNodeFrom().getComponent().getName());
            componentFromTO
                    .setHexID(graphLinkNode.getComponentNodeFrom().getComponent().getHexID());
            componentFromTO.setPublicComponent(
                    graphLinkNode.getComponentNodeFrom().getComponent().getPublicComponent());
            componentFromTO.setOrganization(graphLinkNode.getComponentNodeFrom().getComponent().getOrganization().getName());
            componentNodeFromTO.setComponent(componentFromTO);

            graphLinkNodeTO.setComponentNodeFrom(componentNodeFromTO);

            ComponentNodeTO componentNodeToTO = new ComponentNodeTO();
            componentNodeToTO
                    .setComponentNodeID(graphLinkNode.getComponentNodeTo().getComponentNodeID());
            componentNodeToTO.setHexID(graphLinkNode.getComponentNodeTo().getHexID());
            componentNodeToTO.setName(graphLinkNode.getComponentNodeTo().getName());

            ComponentTO componentToTO = new ComponentTO();
            componentToTO.setId(graphLinkNode.getComponentNodeTo().getComponent().getId());
            componentToTO.setName(graphLinkNode.getComponentNodeTo().getComponent().getName());
            componentToTO.setHexID(graphLinkNode.getComponentNodeTo().getComponent().getHexID());
            componentToTO.setPublicComponent(
                    graphLinkNode.getComponentNodeTo().getComponent().getPublicComponent());
            componentToTO.setOrganization(graphLinkNode.getComponentNodeFrom().getComponent().getOrganization().getName());
            componentNodeToTO.setComponent(componentToTO);

            graphLinkNodeTO.setComponentNodeTo(componentNodeToTO);

            graphLinkNodeTOs.add(graphLinkNodeTO);
        }
        return graphLinkNodeTOs;
    }

    private void storeComponentNodes(List<ComponentNode> newComponentNodes, Application application) {
        for (ComponentNode componentNode : newComponentNodes) {
            if (null != componentNode.getComponent() && null != componentNode.getComponent().getId()
                    && componentDAO.findById(componentNode.getComponent().getId()).isPresent()
                    && null != componentNode.getName() && !componentNode.getName().isEmpty()) {

                componentNode.setApplication(application);
                Optional<Component> optionalComponent = componentDAO.findById(componentNode.getComponent().getId());
                optionalComponent.ifPresent(componentNode::setComponent);
                componentNode.setDateCreated(new Date());
                componentNode.setLastModified(new Date());
                componentNode.setHexID(Util.createRandomHEXString());
                componentNodeDAO.save(componentNode);

            } else {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }
        }
    }


    private void storeGraphicLinkNodes(List<GraphLinkNode> newGraphLinkNodes, Application application) {

        for (GraphLinkNode graphLinkNode : newGraphLinkNodes) {

            if (null != graphLinkNode.getGraphLink() && null != graphLinkNode.getGraphLink()
                    .getGraphLinkID()
                    && graphLinkDAO.findById(graphLinkNode.getGraphLink().getGraphLinkID()).isPresent()
                    && null != graphLinkNode.getComponentNodeFrom() && null != graphLinkNode
                    .getComponentNodeFrom().getComponent() && null != graphLinkNode
                    .getComponentNodeFrom().getComponent().getId() &&
                    null != graphLinkNode
                            .getComponentNodeFrom().getName()
                    && !graphLinkNode
                    .getComponentNodeFrom().getName().isEmpty() &&
                    componentNodeDAO.findByNameAndApplication(graphLinkNode
                            .getComponentNodeFrom().getName(), application).isPresent() &&
                    null != graphLinkNode.getComponentNodeTo() && null != graphLinkNode
                    .getComponentNodeTo().getComponent() && null != graphLinkNode.getComponentNodeTo()
                    .getComponent().getId()
                    &&
                    null != graphLinkNode
                            .getComponentNodeTo().getName()
                    && !graphLinkNode
                    .getComponentNodeTo().getName().isEmpty() &&
                    componentNodeDAO.findByNameAndApplication(graphLinkNode
                            .getComponentNodeTo().getName(), application).isPresent()) {

                graphLinkNode.setApplication(application);
                graphLinkNode.setDateCreated(new Date());
                graphLinkNode.setLastModified(new Date());
                Optional<GraphLink> graphLinkOptional = graphLinkDAO.findById(graphLinkNode.getGraphLink().getGraphLinkID());
                graphLinkOptional.ifPresent(graphLinkNode::setGraphLink);
                Optional<ComponentNode> optionalComponentNodeFrom = componentNodeDAO.findByNameAndApplication(graphLinkNode
                        .getComponentNodeFrom().getName(), application);
                optionalComponentNodeFrom.ifPresent(graphLinkNode::setComponentNodeFrom);
                Optional<ComponentNode> optionalComponentNodeTo = componentNodeDAO.findByNameAndApplication(graphLinkNode
                        .getComponentNodeTo().getName(), application);
                optionalComponentNodeTo.ifPresent(graphLinkNode::setComponentNodeTo);
                graphLinkNodeDAO.save(graphLinkNode);

            } else {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                        GenericMessage.REQUIRED_FIELDS_MISSING);
            }

        }

    }


}
