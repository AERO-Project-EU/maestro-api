package eu.orchestrator.backend.service.oss.slice;

import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ConstraintService;
import eu.orchestrator.backend.service.oss.intent.converters.FakeAccessGraphLinkIdProvider;
import eu.orchestrator.backend.transfer.ConstraintSatisfactionTO;
import eu.orchestrator.backend.transfer.PlacementTO;
import eu.orchestrator.backend.transfer.SliceTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.document.repository.dao.OrchestratorApplicationInstanceDAO;
import eu.orchestrator.repository.dao.GraphLinkNodeInstanceDAO;
import eu.orchestrator.repository.dao.InterfaceInstanceDAO;
import eu.orchestrator.repository.dao.SliceConstraintSatisfactionDAO;
import eu.orchestrator.repository.dao.SliceDAO;
import eu.orchestrator.repository.dao.SlicePlacementAttachmentPointDAO;
import eu.orchestrator.repository.dao.SlicePlacementDAO;
import eu.orchestrator.repository.dao.SliceProviderDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.Slice;
import eu.orchestrator.repository.domain.SliceConstraintSatisfaction;
import eu.orchestrator.repository.domain.SlicePlacement;
import eu.orchestrator.repository.domain.SlicePlacementAttachmentPoint;
import eu.orchestrator.repository.domain.SliceProvider;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.oss.AttachmentPoint;
import eu.orchestrator.transfer.entities.oss.ComponentPlacement;
import eu.orchestrator.transfer.entities.oss.ConstraintSatisfaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class SliceService {

    private static final Logger LOGGER = Logger.getLogger(SliceService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ApplicationInstanceService applicationInstanceService;
    private final ComponentNodeInstanceService componentNodeInstanceService;
    private final ConstraintService constraintService;
    private final FakeAccessGraphLinkIdProvider fakeAccessGraphLinkIdProvider;
    private final GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO;
    private final InterfaceInstanceDAO interfaceInstanceDAO;
    private final SliceConstraintSatisfactionDAO sliceConstraintSatisfactionDAO;
    private final SliceDAO sliceDAO;
    private final SlicePlacementAttachmentPointDAO slicePlacementAttachmentPointDAO;
    private final SlicePlacementDAO slicePlacementDAO;
    private final SliceProviderDAO sliceProviderDAO;
    private final OrchestratorApplicationInstanceDAO orchestratorApplicationInstanceDAO;

    @Inject
    public SliceService(ApplicationInstanceService applicationInstanceService,
            ComponentNodeInstanceService componentNodeInstanceService, ConstraintService constraintService,
            FakeAccessGraphLinkIdProvider fakeAccessGraphLinkIdProvider,
            GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO, InterfaceInstanceDAO interfaceInstanceDAO,
            SliceConstraintSatisfactionDAO sliceConstraintSatisfactionDAO,
            SliceDAO sliceDAO,
            SlicePlacementAttachmentPointDAO slicePlacementAttachmentPointDAO, SlicePlacementDAO slicePlacementDAO,
            SliceProviderDAO sliceProviderDAO,
            // Optional: the Mongo DAO bean exists only when mongo.enabled=true.
            org.springframework.beans.factory.ObjectProvider<OrchestratorApplicationInstanceDAO> orchestratorApplicationInstanceDAOProvider) {
        this.applicationInstanceService = applicationInstanceService;
        this.componentNodeInstanceService = componentNodeInstanceService;
        this.constraintService = constraintService;
        this.fakeAccessGraphLinkIdProvider = fakeAccessGraphLinkIdProvider;
        this.graphLinkNodeInstanceDAO = graphLinkNodeInstanceDAO;
        this.interfaceInstanceDAO = interfaceInstanceDAO;
        this.sliceConstraintSatisfactionDAO = sliceConstraintSatisfactionDAO;
        this.sliceDAO = sliceDAO;
        this.slicePlacementAttachmentPointDAO = slicePlacementAttachmentPointDAO;
        this.slicePlacementDAO = slicePlacementDAO;
        this.sliceProviderDAO = sliceProviderDAO;
        this.orchestratorApplicationInstanceDAO = orchestratorApplicationInstanceDAOProvider.getIfAvailable();
    }

    public void saveSlice(Slice slice) {
        sliceDAO.save(slice);
    }

    public void deleteSlice(Slice slice) {
        sliceDAO.delete(slice);
    }

    public String fetchSliceById(Long sliceID, User authenticatedUser) throws JsonProcessingException {
        Slice existingSlice = sliceDAO.findById(sliceID).orElse(null);
        if (null != existingSlice) {
            ApplicationInstance applicationInstance =
                    applicationInstanceService.fetchApplicationInstanceById(existingSlice.getApplicationInstance().getApplicationInstanceID());

            if (null != applicationInstance && null != applicationInstance.getStatus()
                    && applicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.WAITING_CONFIRMATION.name())
                    && (authenticatedUser.getRole().equals(User.RoleName.ADMIN.name())
                    || applicationInstance.getUser().getId().equals(authenticatedUser.getId()))) {
                SliceTO sliceTO = new SliceTO();
                sliceTO.setSliceID(existingSlice.getId() + "");
                if (null != existingSlice.getPlacements() && !existingSlice.getPlacements().isEmpty()) {
                    List<PlacementTO> placements = new ArrayList<>();
                    existingSlice.getPlacements().forEach(placement -> {
                        PlacementTO placementTO = new PlacementTO();
                        placementTO.setComponentNodeInstanceHexID(placement.getComponentNodeInstance().getHexID());
                        placementTO.setComponentNodeInstanceName(placement.getComponentNodeInstance().getName());
                        placementTO.setProviderID(placement.getProvider().getProviderID() + "");
                        placementTO.setProviderName(placement.getProvider().getName());
                        placements.add(placementTO);
                    });
                    sliceTO.setPlacements(placements);
                }
                if (null != existingSlice.getConstraintSatisfactions() && !existingSlice.getConstraintSatisfactions().isEmpty()) {
                    List<ConstraintSatisfactionTO> constraintSatisfactions = new ArrayList<>();
                    existingSlice.getConstraintSatisfactions().forEach(constraintSatisfaction -> {
                        ConstraintSatisfactionTO constraintSatisfactionTO = new ConstraintSatisfactionTO();
                        constraintSatisfactionTO.setSatisfied(constraintSatisfaction.getSatisfied().booleanValue() ? "Yes" : "No");
                        constraintSatisfactionTO.setCategory(
                                Constraint.ConstraintCategory.valueOf(constraintSatisfaction.getConstraint().getConstraintCategory()).getFriendlyName());
                        constraintSatisfactionTO.setType(null != constraintSatisfaction.getConstraintType()
                                ? (constraintSatisfaction.getConstraintType().equals("HARD") ? "Hard" : "Soft") : "Hard");
                        List<String> subConstraints = new ArrayList<>();
                        // BL per category
                        String category = constraintSatisfaction.getConstraint().getConstraintCategory();
                        switch (category) {
                            case "COMPONENT_HOSTING":
                                subConstraints.add(
                                        constraintSatisfaction.getConstraint().getComponentNodeInstance().getName()
                                                + " (" + constraintSatisfaction.getConstraint().getComponentNodeInstance().getHexID() + ")");
                                String constraintMetric = Constraint.ConstraintMetric.valueOf(constraintSatisfaction.getConstraint().getConstraintMetric())
                                        .getFriendlyName() + ": " + constraintSatisfaction.getConstraint().getConstraintValue();
                                if (!constraintSatisfaction.getConstraint().getConstraintUnit().equals("AMOUNT")
                                        && !constraintSatisfaction.getConstraint().getConstraintUnit().equals("REGION")) {
                                    constraintMetric += " " + constraintSatisfaction.getConstraint().getConstraintUnit();
                                } else if (constraintSatisfaction.getConstraint().getConstraintUnit().equals("PERCENTAGE")) {
                                    constraintMetric += " %";
                                }
                                subConstraints.add(constraintMetric);
                                break;
                            case "ACCESS":
                                subConstraints.add(constraintSatisfaction.getConstraint().getInterfaceInstance().getComponentNodeInstance().getName()
                                        + " (" + constraintSatisfaction.getConstraint().getInterfaceInstance().getComponentNodeInstance().getHexID() + ")");
                                subConstraints.add("QI: " + constraintSatisfaction.getConstraint().getQi().getQiValue());
                                subConstraints.add("Radio Service Type: " + constraintSatisfaction.getConstraint().getRadioServiceType().getSstValue());
                                subConstraints.add("Resource Type: " + Constraint.ResourceType.valueOf(
                                        constraintSatisfaction.getConstraint().getResourceType()).getFriendlyName());
                                subConstraints.add("Allocation Retention Priority Profile: " + constraintSatisfaction.getConstraint()
                                        .getAllocationRetentionPriorityProfile());
                                subConstraints.add("Minimum Guaranteed Bandwidth: " + constraintSatisfaction.getConstraint()
                                        .getMinimumGuaranteedBandwidth() + " Mbps");
                                subConstraints.add("Maximum Required Bandwidth: " + constraintSatisfaction.getConstraint()
                                        .getMaximumRequiredBandwidth() + " Mbps");
                                break;
                            case "GRAPH_LINK":
                                subConstraints.add(
                                        "From " + constraintSatisfaction.getConstraint().getGraphLinkNodeInstance().getComponentNodeInstanceFrom().getName()
                                                + " (" + constraintSatisfaction.getConstraint().getGraphLinkNodeInstance().getComponentNodeInstanceTo()
                                                .getHexID() + ") to " + constraintSatisfaction.getConstraint().getGraphLinkNodeInstance()
                                                .getComponentNodeInstanceTo().getName() + " (" + constraintSatisfaction.getConstraint()
                                                .getGraphLinkNodeInstance().getComponentNodeInstanceFrom().getHexID() + ")");
                                String graphLinkMetric = Constraint.ConstraintMetric.valueOf(constraintSatisfaction.getConstraint().getConstraintMetric())
                                        .getFriendlyName() + ": " + constraintSatisfaction.getConstraint().getConstraintValue();

                                if (constraintSatisfaction.getConstraint().getConstraintUnit().equals("PERCENTAGE")) {
                                    graphLinkMetric += " %";
                                } else {
                                    graphLinkMetric += " " + constraintSatisfaction.getConstraint().getConstraintUnit();
                                }
                                subConstraints.add(graphLinkMetric);
                                break;
                        }
                        constraintSatisfactionTO.setSubConstraints(subConstraints);
                        constraintSatisfactions.add(constraintSatisfactionTO);
                    });
                    sliceTO.setConstraintSatisfactions(constraintSatisfactions);
                }
                return objectMapper.writeValueAsString(sliceTO);
            }
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public Slice fetchByApplicationInstance(ApplicationInstance applicationInstance) {
        Optional<Slice> sliceOptional = sliceDAO.findByApplicationInstance(applicationInstance);
        return sliceOptional.orElse(null);
    }

    private void logStoredEntity(Object object) {
        LOGGER.log(Level.INFO, "Stored {0}", object);
    }

    public Slice createSlice(ApplicationInstance instance) {
        final Slice slice = new Slice();

        slice.setApplicationInstance(instance);
        final Date now = new Date();
        slice.setDateCreated(now);
        slice.setLastModified(now);
        slice.setHexID(Util.createRandomHEXString());
        slice.setUser(instance.getUser());

        final Optional<OrchestratorApplicationInstance> associatedOrchestratorApplicationInstance = getAssociatedOrchestratorApplicationInstance(
                instance.getApplicationInstanceID());
        if (associatedOrchestratorApplicationInstance.isPresent()) {
            LOGGER.log(Level.INFO, "Retrieved orchestrator application instance associated with application [id={0}]", instance.getApplicationInstanceID());

            String orchestratorApplicationInstanceStr;
            try {
                orchestratorApplicationInstanceStr = objectMapper.writeValueAsString(associatedOrchestratorApplicationInstance.get());
            } catch (JsonProcessingException exception) {
                throw new GenericBusinessException(
                        String.format("Failed to convert orchestrator application instance to JSON: %s", exception.getMessage()),
                        GenericMessage.GENERIC_ERROR);
            }

            slice.setOrchestratorApplicationInstance(orchestratorApplicationInstanceStr);
        }

        final Slice storedSlice = sliceDAO.save(slice);
        logStoredEntity(storedSlice);
        return storedSlice;
    }

    private Optional<OrchestratorApplicationInstance> getAssociatedOrchestratorApplicationInstance(long applicationInstanceId) {
        // Assuming that applicationInstanceID == graphInstanceId
        final String graphInstanceId = String.valueOf(applicationInstanceId);

        if (orchestratorApplicationInstanceDAO == null) {
            // MongoDB is disabled (mongo.enabled=false); no associated OrchestratorApplicationInstance available.
            return Optional.empty();
        }

        final OrchestratorApplicationInstance orchestratorApplicationInstance = orchestratorApplicationInstanceDAO.findByGraphInstanceID(graphInstanceId);
        if (orchestratorApplicationInstance == null) {
            return Optional.empty();
        }

        return Optional.of(orchestratorApplicationInstance);
    }

    public void createSliceConstraintSatisfaction(ConstraintSatisfaction constraintSatisfaction, Long associatedSliceId) {
        final Slice associatedSlice = sliceDAO.getOne(associatedSliceId);
        final Constraint constraint = constraintService.fetchConstraintById(Long.valueOf(constraintSatisfaction.getConstraintID()));

        final SliceConstraintSatisfaction sliceConstraintSatisfaction = new SliceConstraintSatisfaction();
        sliceConstraintSatisfaction.setSlice(associatedSlice);
        sliceConstraintSatisfaction.setConstraintType(constraintSatisfaction.getConstraintType());
        sliceConstraintSatisfaction.setConstraint(constraint);
        sliceConstraintSatisfaction.setSatisfied(constraintSatisfaction.isSatisfied());
        final Date now = new Date();
        sliceConstraintSatisfaction.setDateCreated(now);
        sliceConstraintSatisfaction.setLastModified(now);

        final SliceConstraintSatisfaction storedSliceConstraintSatisfaction = sliceConstraintSatisfactionDAO.save(sliceConstraintSatisfaction);
        logStoredEntity(storedSliceConstraintSatisfaction);
    }

    public void createSliceProvider(Provider associatedProvider, Slice associatedSlice) {
        final SliceProvider sliceProvider = new SliceProvider();
        sliceProvider.setProvider(associatedProvider);
        sliceProvider.setSlice(associatedSlice);
        final Date now = new Date();
        sliceProvider.setDateCreated(now);
        sliceProvider.setLastModified(now);

        final SliceProvider storedSliceProvider = sliceProviderDAO.save(sliceProvider);
        logStoredEntity(storedSliceProvider);
    }

    public SlicePlacement createSlicePlacement(ComponentPlacement componentPlacement, Slice storedSlice, Provider provider) {
        final long componentNodeInstanceId = Long.parseLong(componentPlacement.getComponentNodeInstanceID());
        final ComponentNodeInstance componentNodeInstance = componentNodeInstanceService.fetchComponentNodeInstanceById(componentNodeInstanceId);

        final SlicePlacement slicePlacement = new SlicePlacement();
        slicePlacement.setSlice(storedSlice);
        slicePlacement.setComponentNodeInstance(componentNodeInstance);
        slicePlacement.setProvider(provider);
        slicePlacement.setFlavorID(componentPlacement.getFlavorID());
        final Date now = new Date();
        slicePlacement.setDateCreated(now);
        slicePlacement.setLastModified(now);

        final SlicePlacement storedSlicePlacement = slicePlacementDAO.save(slicePlacement);
        logStoredEntity(storedSlicePlacement);
        return storedSlicePlacement;
    }

    public void createSlicePlacementAttachmentPoint(AttachmentPoint attachmentPoint, SlicePlacement associatedPlacement) {
        final SlicePlacementAttachmentPoint slicePlacementAttachmentPoint = new SlicePlacementAttachmentPoint();

        slicePlacementAttachmentPoint.setSlicePlacement(associatedPlacement);
        slicePlacementAttachmentPoint.setAttachmentPoint(attachmentPoint.getAttachmentPointIdentifier());
        final Date now = new Date();
        slicePlacementAttachmentPoint.setDateCreated(now);
        slicePlacementAttachmentPoint.setLastModified(now);

        final String graphLinkNodeInstanceId = attachmentPoint.getGraphLinkNodeInstanceID();
        final boolean isFakeAccessGraphLink = fakeAccessGraphLinkIdProvider.isFakeAccessGraphLink(graphLinkNodeInstanceId);
        if (isFakeAccessGraphLink) {
            final long interfaceInstanceId = fakeAccessGraphLinkIdProvider.extractInterfaceInstanceIdentifier(graphLinkNodeInstanceId);
            slicePlacementAttachmentPoint.setInterfaceInstance(interfaceInstanceDAO.findById(interfaceInstanceId).orElse(null));
            slicePlacementAttachmentPoint.setGraphLinkNodeInstance(null);
        } else {
            slicePlacementAttachmentPoint.setInterfaceInstance(null);
            final Long graphLinkIdentifier = Long.valueOf(attachmentPoint.getGraphLinkNodeInstanceID());
            slicePlacementAttachmentPoint.setGraphLinkNodeInstance(graphLinkNodeInstanceDAO.findById(graphLinkIdentifier).orElse(null));
        }

        final SlicePlacementAttachmentPoint storedSlicePlacementAttachmentPoint = slicePlacementAttachmentPointDAO.save(slicePlacementAttachmentPoint);
        logStoredEntity(storedSlicePlacementAttachmentPoint);
    }
}
