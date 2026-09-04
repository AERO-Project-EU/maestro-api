package eu.orchestrator.backend.service.oss.slice;

import eu.orchestrator.backend.service.oss.ProviderNameReducer;
import eu.orchestrator.backend.service.resourceprovider.ProviderService;
import eu.orchestrator.backend.service.resourceprovider.ProviderTypeService;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.exception.BadRequestBusinessException;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.ProviderType.ProviderName;
import eu.orchestrator.repository.domain.SlicePlacement;
import eu.orchestrator.transfer.entities.oss.AttachmentPoint;
import eu.orchestrator.transfer.entities.oss.ComponentPlacement;
import eu.orchestrator.transfer.entities.oss.ConstraintSatisfaction;
import eu.orchestrator.transfer.entities.oss.Slice;
import eu.orchestrator.transfer.entities.oss.VIM;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eu.orchestrator.common.enums.GenericMessage.GENERIC_ERROR;
import static eu.orchestrator.common.enums.GenericMessage.REQUIRED_FIELDS_MISSING;

@Service
@Transactional(rollbackOn = Exception.class)
public class SliceIngestionService {

    private static final Logger LOGGER = Logger.getLogger(SliceIngestionService.class.getName());

    private final ProviderService providerService;
    private final ProviderTypeService providerTypeService;
    private final SliceService sliceService;
    private final SliceValidator sliceValidator;
    private final ProviderNameReducer reducer;

    // Required to encrypt passwords
    @Value("${token.signer.secret}")
    private String tokenSecret;

    @Inject
    public SliceIngestionService(ProviderService providerService, ProviderTypeService providerTypeService,
            SliceService sliceService) {
        this.providerService = providerService;
        this.providerTypeService = providerTypeService;
        this.sliceService = sliceService;
        this.sliceValidator = new SliceValidator();
        reducer = new ProviderNameReducer();
    }

    public eu.orchestrator.repository.domain.Slice ingest(ApplicationInstance applicationInstance, Slice slice) {
        try {
            sliceValidator.validate(slice);
        } catch (ValidationException exception) {
            throw new BadRequestBusinessException(
                    String.format("Slice validation failed: %s", exception.getMessage()),
                    REQUIRED_FIELDS_MISSING);
        }

        final eu.orchestrator.repository.domain.Slice storedSlice = sliceService.createSlice(applicationInstance);

        for (ConstraintSatisfaction constraintSatisfaction : slice.getConstraintSatisfactions()) {
            sliceService.createSliceConstraintSatisfaction(constraintSatisfaction, storedSlice.getId());
        }

        final Map<String, Provider> providers = createSliceProviders(applicationInstance, slice, storedSlice);

        createSlicePlacements(slice, storedSlice, providers);

        return storedSlice;
    }

    private Map<String, Provider> createSliceProviders(ApplicationInstance applicationInstance, Slice slice,
            eu.orchestrator.repository.domain.Slice storedSlice) {
        final Map<String, Provider> providers = new HashMap<>();
        for (VIM vim : slice.getVimDescriptors()) {
            Provider associatedProvider = providerService.fetchByNameAndProjectAndUsername(vim.getVimID(), vim.getProject(), vim.getUsername());
            if (associatedProvider == null) {
                final ProviderName reducedProviderName = getReducedProviderName(applicationInstance.getProvider());
                associatedProvider = createInternalProvider(applicationInstance, vim, reducedProviderName);
            }

            providers.putIfAbsent(vim.getVimID(), associatedProvider);
            sliceService.createSliceProvider(associatedProvider, storedSlice);
        }
        return providers;
    }

    private ProviderName getReducedProviderName(Provider applicationInstanceProvider) {
        final ProviderType providerType = applicationInstanceProvider.getProviderType();
        if (providerType == null) {
            throw new IllegalArgumentException("Cannot infer provider name with null provider type");
        }

        return reducer.reduce(providerType.getProviderName());
    }

    private Provider createInternalProvider(ApplicationInstance applicationInstance, VIM vim, ProviderName providerName) {
        LOGGER.log(Level.INFO, "Creating internal provider with name {0} and provider type {1} for application instance id {2}",
                new Object[]{vim.getVimID(), providerName, applicationInstance.getApplicationInstanceID()});

        final Provider provider = new Provider();
        provider.setName(vim.getVimID());
        provider.setProviderType(providerTypeService.fetchByFriendlyName(providerName.name()));
        provider.setUser(applicationInstance.getUser());
        provider.setEnabled(true);
        provider.setInternalProvider(true);
        provider.setDefaultProvider(false);
        final Date now = new Date();
        provider.setDateCreated(now);
        provider.setLastModified(now);

        provider.setEndpoint(vim.getEndpoint());
        provider.setProject(vim.getProject());

        if (reducer.isOpenStackBasedProvider(providerName)) {
            provider.setDomain(vim.getDomain());
            provider.setUsername(vim.getUsername());

            final String encryptedPassword = Util.encrypt(vim.getPassword(), tokenSecret);
            provider.setPassword(encryptedPassword);

            provider.setProxy(applicationInstance.getProvider().getProxy());
            provider.setNetworkID(vim.getNetworkID());
            provider.setExternalNetworkID(vim.getExternalNetworkID());
            provider.setImageID(vim.getImageID());

        } else if (reducer.isKubernetesBasedProvider(providerName)) {
            provider.setUsername(vim.getCaCert());
            provider.setPublicKey(vim.getToken());

        } else {
            throw new GenericBusinessException(
                    String.format(
                            "Reject creation of internal provider for application instance [id='%s'] from VIM '%s' as provider name (type) '%s' is invalid",
                            applicationInstance.getApplicationInstanceID(), vim.getVimID(), providerName.name()), GENERIC_ERROR);
        }

        providerService.saveProvider(provider);
        return provider;
    }

    private void createSlicePlacements(Slice slice, eu.orchestrator.repository.domain.Slice storedSlice, Map<String, Provider> providers) {
        for (ComponentPlacement componentPlacement : slice.getComponentPlacements()) {
            SlicePlacement slicePlacement;
            if (providers.containsKey(componentPlacement.getVimID())) {
                slicePlacement = sliceService.createSlicePlacement(componentPlacement, storedSlice, providers.get(componentPlacement.getVimID()));
            } else {
                throw new GenericBusinessException(
                        String.format("VIM id '%s' is not in supplied vim descriptors '%s'",
                                componentPlacement.getVimID(), Strings.join(providers.keySet(), ',')), GENERIC_ERROR);
            }

            for (AttachmentPoint attachmentPoint : componentPlacement.getAttachmentPoints()) {
                sliceService.createSlicePlacementAttachmentPoint(attachmentPoint, slicePlacement);
            }
        }
    }
}
