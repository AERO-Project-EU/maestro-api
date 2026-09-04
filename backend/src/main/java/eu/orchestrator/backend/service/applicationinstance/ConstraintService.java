package eu.orchestrator.backend.service.applicationinstance;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.util.ConverterUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.ConstraintDAO;
import eu.orchestrator.repository.dao.CountryDAO;
import eu.orchestrator.repository.dao.GraphLinkNodeInstanceDAO;
import eu.orchestrator.repository.dao.InterfaceInstanceDAO;
import eu.orchestrator.repository.dao.QIDAO;
import eu.orchestrator.repository.dao.RadioServiceTypeDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.GraphLinkNodeInstance;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.repository.domain.QI;
import eu.orchestrator.repository.domain.RadioServiceType;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.transfer.ComponentNodeInstanceTO;
import eu.orchestrator.backend.transfer.ConstraintTO;
import eu.orchestrator.backend.transfer.CountryTO;
import eu.orchestrator.backend.transfer.GraphLinkNodeInstanceTO;
import eu.orchestrator.backend.transfer.InterfaceInstanceTO;
import eu.orchestrator.backend.transfer.QITO;
import eu.orchestrator.backend.transfer.RadioServiceTypeTO;
import eu.orchestrator.transfer.entities.oss.ConstraintSatisfaction;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class ConstraintService {

    @Autowired
    ApplicationInstanceService applicationInstanceService;

    @Autowired
    ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    ConstraintDAO constraintDAO;

    @Autowired
    InterfaceInstanceDAO interfaceInstanceDAO;

    @Autowired
    RadioServiceTypeDAO radioServiceTypeDAO;

    @Autowired
    QIDAO qiDAO;

    @Autowired
    CountryDAO countryDAO;

    @Autowired
    GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO;


    public Constraint fetchConstraintById(Long id) {
        Optional<Constraint> constraintOptional = constraintDAO.findById(id);
        return constraintOptional.orElse(null);
    }

    public List<ConstraintTO> fetchConstraintsByApplicationInstanceId(Long applicationInstanceId, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceId);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) &&
                (authenticatedUser.isAdmin()
                        || existingApplicationInstance.getOrganization().getId()
                        .equals(authenticatedUser.getOrganization().getId()))) {

            List<Constraint> applicationInstanceConstraints = constraintDAO
                    .findAllByApplicationInstance(existingApplicationInstance);

            if (NullCheckUtil.isNotEmpty(applicationInstanceConstraints)) {
                return ConverterUtil.convertConstraintsToConstraintTO(applicationInstanceConstraints);
            } else {
                return new ArrayList<>();
            }
        } else {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public List<ConstraintTO> fetchConstraintsByCategoryAndApplicationInstanceId(Long applicationInstanceId, String constraintCategory,
            User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceId);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) &&
                (authenticatedUser.isAdmin() || existingApplicationInstance.getOrganization().getId()
                        .equals(authenticatedUser.getOrganization().getId()))) {

            List<Constraint> applicationInstanceConstraints = constraintDAO
                    .findAllByApplicationInstanceAndConstraintCategory(existingApplicationInstance,
                            constraintCategory);

            List<ConstraintTO> constraintTOs = new ArrayList<>();

            if (null != applicationInstanceConstraints && !applicationInstanceConstraints.isEmpty()) {

                applicationInstanceConstraints.forEach(exConstraint -> {

                    ConstraintTO constraintTO = new ConstraintTO();
                    BeanUtils.copyProperties(exConstraint, constraintTO);

                    // Copy extra TO Fields RadioServiceTypeTO, CountryTO, QITO
                    if (null != exConstraint.getRadioServiceType()) {
                        RadioServiceTypeTO radioServiceTypeTO = new RadioServiceTypeTO();
                        BeanUtils.copyProperties(exConstraint.getRadioServiceType(), radioServiceTypeTO);
                        constraintTO.setRadioServiceType(radioServiceTypeTO);
                    }

                    if (null != exConstraint.getCountry()) {
                        CountryTO countryTO = new CountryTO();
                        BeanUtils.copyProperties(exConstraint.getCountry(), countryTO);
                        constraintTO.setCountry(countryTO);
                    }

                    if (null != exConstraint.getQi()) {
                        QITO qiTO = new QITO();
                        qiTO.setId(exConstraint.getQi().getId());
                        qiTO.setDateCreated(exConstraint.getQi().getDateCreated());
                        qiTO.setQiValue(exConstraint.getQi().getQiValue());
                        qiTO.setDefaultPriorityLevel(exConstraint.getQi().getDefaultPriorityLevel());
                        qiTO.setResourceType(QI.ResourceType.valueOf(exConstraint.getQi().getResourceType())
                                .getFriendlyName());
                        qiTO.setPacketDelayBudget(
                                String.valueOf(exConstraint.getQi().getPacketDelayBudget()));
                        qiTO.setPacketErrorRate(
                                String.valueOf(exConstraint.getQi().getPacketErrorRate()));
                        constraintTO.setQi(qiTO);
                    }

                    if (null != exConstraint.getComponentNodeInstance()) {
                        ComponentNodeInstanceTO componentNodeInstanceTO = new ComponentNodeInstanceTO();
                        componentNodeInstanceTO.setComponentNodeInstanceID(
                                exConstraint.getComponentNodeInstance().getComponentNodeInstanceID());
                        constraintTO.setComponentNodeInstance(componentNodeInstanceTO);
                    }

                    if (null != exConstraint.getGraphLinkNodeInstance()) {
                        GraphLinkNodeInstanceTO graphLinkNodeInstanceTO = new GraphLinkNodeInstanceTO();
                        graphLinkNodeInstanceTO.setGraphLinkNodeInstanceID(
                                exConstraint.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID());
                        constraintTO.setGraphLinkNodeInstance(graphLinkNodeInstanceTO);
                    }

                    if (null != exConstraint.getInterfaceInstance()) {
                        InterfaceInstanceTO interfaceInstanceTO = new InterfaceInstanceTO();
                        interfaceInstanceTO.setInterfaceInstanceID(exConstraint.getInterfaceInstance().getInterfaceInstanceID());
                        constraintTO.setInterfaceInstance(interfaceInstanceTO);
                    }

                    constraintTOs.add(constraintTO);

                });

                return constraintTOs;
            } else {
                return constraintTOs;
            }
        } else {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }

    }

    public void updateConstraintsByCategoryAndApplicationInstanceId(Long applicationInstanceId, String constraintCategory,
            List<ConstraintTO> constraints, User authenticatedUser) {
        List<Constraint> newConstraints = new ArrayList<>();
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceId);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) && Boolean.TRUE.equals(existingApplicationInstance.hasEditAllowance(authenticatedUser))) {

            List<Constraint> applicationInstanceConstraints = constraintDAO
                    .findAllByApplicationInstanceAndConstraintCategory(existingApplicationInstance,
                            constraintCategory);

            if (null != constraints && !constraints.isEmpty()) {

                for (ConstraintTO exConstraint : constraints) {

                    boolean requiredFields =
                            null != exConstraint.getConstraintCategory() && !exConstraint.getConstraintCategory()
                                    .isEmpty()
                                    && null != Constraint.ConstraintCategory.valueOf(exConstraint.getConstraintCategory()).name();
                                    /*&& null != exConstraint.getConstraintType()*/
                                    //&& !exConstraint.getConstraintType().isEmpty();
                                    /*&& null != Constraint.ConstraintType.valueOf(exConstraint.getConstraintType())
                                    .name()*/

                    if (requiredFields) {

                        switch (constraintCategory) {

                            case "ACCESS":

                                Optional<InterfaceInstance> exInterfaceInstanceOP = interfaceInstanceDAO
                                        .findById(exConstraint.getInterfaceInstance().getInterfaceInstanceID());

                                if (exInterfaceInstanceOP.isPresent() && !constraintDAO.
                                        findAllByApplicationInstanceAndInterfaceInstance(existingApplicationInstance,
                                                exInterfaceInstanceOP.get()).isPresent()) {

                                    Constraint newConstraint = new Constraint();
                                    newConstraint.setResourceType(exConstraint.getResourceType());
                                    newConstraint.setAllocationRetentionPriorityProfile(
                                            exConstraint.getAllocationRetentionPriorityProfile());
                                    newConstraint
                                            .setMinimumGuaranteedBandwidth(exConstraint.getMinimumGuaranteedBandwidth());
                                    newConstraint
                                            .setMaximumRequiredBandwidth(exConstraint.getMaximumRequiredBandwidth());
                                    newConstraint.setDateCreated(new Date());
                                    newConstraint.setLastModified(new Date());
                                    newConstraint.setDeletableConstraint(true);

                                    newConstraint.setInterfaceInstance(interfaceInstanceDAO
                                            .findById(exConstraint.getInterfaceInstance().getInterfaceInstanceID())
                                            .get());
                                    newConstraint.setGraphLinkNodeInstance(null);
                                    newConstraint.setComponentNodeInstance(null);

                                    newConstraint.setApplicationInstance(existingApplicationInstance);
                                    newConstraint.setConstraintMetric(null);
                                    newConstraint.setConstraintUnit(null);
                                    newConstraint.setConstraintType(exConstraint.getConstraintType());
                                    newConstraint.setConstraintCategory(Constraint.ConstraintCategory.ACCESS.name());

                                    if (null != exConstraint.getRadioServiceType()) {
                                        newConstraint.setRadioServiceType(
                                                radioServiceTypeDAO.findById(exConstraint.getRadioServiceType().getId())
                                                        .get());
                                    }

                                    if (null != exConstraint.getQi()) {
                                        newConstraint.setQi(qiDAO.findById(exConstraint.getQi().getId()).get());
                                    }

                                    if (null != exConstraint.getCountry()) {
                                        newConstraint
                                                .setCountry(countryDAO.findById(exConstraint.getCountry().getId()).get());
                                    }

                                    constraintDAO.save(newConstraint);

                                    newConstraints.add(newConstraint);

                                } else {

                                    Optional<Constraint> existingConstraintOP = constraintDAO
                                            .findAllByApplicationInstanceAndInterfaceInstance(existingApplicationInstance,
                                                    exInterfaceInstanceOP.get());

                                    if (existingConstraintOP.isPresent()) {

                                        Constraint existingConstraint = existingConstraintOP.get();

                                        if (null != exConstraint.getRadioServiceType()) {
                                            existingConstraint.setRadioServiceType(
                                                    radioServiceTypeDAO.findById(exConstraint.getRadioServiceType().getId())
                                                            .get());
                                        }

                                        if (null != exConstraint.getQi()) {
                                            existingConstraint.setQi(qiDAO.findById(exConstraint.getQi().getId()).get());
                                        }

                                        if (null != exConstraint.getCountry()) {
                                            existingConstraint
                                                    .setCountry(countryDAO.findById(exConstraint.getCountry().getId()).get());
                                        }

                                        existingConstraint
                                                .setMaximumRequiredBandwidth(exConstraint.getMaximumRequiredBandwidth());
                                        existingConstraint
                                                .setMinimumGuaranteedBandwidth(
                                                        exConstraint.getMinimumGuaranteedBandwidth());
                                        existingConstraint.setAllocationRetentionPriorityProfile(
                                                exConstraint.getAllocationRetentionPriorityProfile());
                                        existingConstraint.setResourceType(exConstraint.getResourceType());
                                        existingConstraint.setConstraintType(exConstraint.getConstraintType());

                                        constraintDAO.save(existingConstraint);

                                        newConstraints.add(existingConstraint);

                                    } else {
                                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                                                GenericMessage.REQUIRED_FIELDS_MISSING);
                                    }

                                }

                                break;

                            case "GRAPH_LINK":

                                Optional<GraphLinkNodeInstance> exGraphLinkNodeInstanceOP = graphLinkNodeInstanceDAO
                                        .findById(exConstraint.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID());

                                if (exGraphLinkNodeInstanceOP.isPresent() && !constraintDAO.
                                        findAllByApplicationInstanceAndConstraintMetricAndGraphLinkNodeInstance(
                                                existingApplicationInstance,
                                                exConstraint.getConstraintMetric(), exGraphLinkNodeInstanceOP.get())
                                        .isPresent()) {

                                    Constraint newConstraint = new Constraint();

                                    newConstraint.setDateCreated(new Date());
                                    newConstraint.setLastModified(new Date());
                                    newConstraint.setDeletableConstraint(true);
                                    newConstraint.setConstraintCategory(Constraint.ConstraintCategory.GRAPH_LINK.name());
                                    newConstraint.setInterfaceInstance(null);
                                    newConstraint.setGraphLinkNodeInstance(graphLinkNodeInstanceDAO
                                            .findById(
                                                    exConstraint.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID())
                                            .get());
                                    newConstraint.setComponentNodeInstance(null);

                                    newConstraint.setApplicationInstance(existingApplicationInstance);

                                    newConstraint.setConstraintType(exConstraint.getConstraintType());
                                    newConstraint.setConstraintValue(exConstraint.getConstraintValue());

                                    if (null != exConstraint.getConstraintMetric()
                                            && null != Constraint.ConstraintMetric
                                            .valueOf(exConstraint.getConstraintMetric()).name()) {

                                        if (exConstraint.getConstraintMetric()
                                                .equals(Constraint.ConstraintMetric.JITTER.name())) {
                                            newConstraint.setConstraintUnit("ms");
                                        } else if (exConstraint.getConstraintMetric()
                                                .equals(Constraint.ConstraintMetric.DELAY.name())) {
                                            newConstraint.setConstraintUnit("ms");
                                        } else if (exConstraint.getConstraintMetric()
                                                .equals(Constraint.ConstraintMetric.PACKET_LOSS.name())) {
                                            newConstraint.setConstraintUnit("percentage");
                                        } else if (exConstraint.getConstraintMetric()
                                                .equals(Constraint.ConstraintMetric.THROUGHPUT.name())) {
                                            newConstraint.setConstraintUnit("kbps");
                                        }

                                        newConstraint.setConstraintMetric(exConstraint.getConstraintMetric());
                                    }

                                    constraintDAO.save(newConstraint);

                                    newConstraints.add(newConstraint);

                                } else {

                                    Constraint existingConstraint = constraintDAO
                                            .findAllByApplicationInstanceAndConstraintMetricAndGraphLinkNodeInstance(
                                                    existingApplicationInstance, exConstraint.getConstraintMetric(),
                                                    exGraphLinkNodeInstanceOP.get()).get();

                                    existingConstraint.setConstraintValue(exConstraint.getConstraintValue());
                                    existingConstraint.setConstraintType(exConstraint.getConstraintType());

                                    constraintDAO.save(existingConstraint);

                                    newConstraints.add(existingConstraint);

                                }

                                break;

                            case "COMPONENT_HOSTING":

                                ComponentNodeInstance componentNodeInstance = componentNodeInstanceService
                                        .fetchComponentNodeInstanceById(exConstraint.getComponentNodeInstance().getComponentNodeInstanceID());

                                if (!constraintDAO.findAllByApplicationInstanceAndConstraintMetricAndComponentNodeInstance(
                                        existingApplicationInstance, exConstraint.getConstraintMetric(), componentNodeInstance).isPresent()) {

                                    Constraint newConstraint = new Constraint();
                                    newConstraint.setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                                    newConstraint.setDateCreated(new Date());
                                    newConstraint.setLastModified(new Date());
                                    newConstraint.setDeletableConstraint(true);
                                    newConstraint.setConstraintValue(exConstraint.getConstraintValue());
                                    newConstraint.setConstraintType(exConstraint.getConstraintType());
                                    newConstraint.setInterfaceInstance(null);
                                    newConstraint.setGraphLinkNodeInstance(null);
                                    newConstraint.setComponentNodeInstance(componentNodeInstance);

                                    newConstraint.setApplicationInstance(existingApplicationInstance);

                                    if (null != exConstraint.getConstraintMetric()
                                            && null != Constraint.ConstraintMetric
                                            .valueOf(exConstraint.getConstraintMetric()).name()) {
                                        newConstraint.setConstraintMetric(exConstraint.getConstraintMetric());
                                    }

                                    constraintDAO.save(newConstraint);

                                    newConstraints.add(newConstraint);

                                } else {

                                    newConstraints.add(constraintDAO
                                            .findAllByApplicationInstanceAndConstraintMetricAndComponentNodeInstance(
                                                    existingApplicationInstance, exConstraint.getConstraintMetric(),
                                                    componentNodeInstance).get());

                                    Constraint existingConstraint = constraintDAO
                                            .findAllByApplicationInstanceAndConstraintMetricAndComponentNodeInstance(
                                                    existingApplicationInstance, exConstraint.getConstraintMetric(),
                                                    componentNodeInstance).get();

                                    existingConstraint.setConstraintValue(exConstraint.getConstraintValue());
                                    existingConstraint.setConstraintType(exConstraint.getConstraintType());

                                    constraintDAO.save(existingConstraint);

                                    newConstraints.add(existingConstraint);

                                }

                                break;

                        }

                    } else {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                                GenericMessage.REQUIRED_FIELDS_MISSING);
                    }

                }

                switch (constraintCategory) {

                    case "ACCESS":

                        applicationInstanceConstraints.stream().filter(
                                        existingConstraint -> null != existingConstraint.getInterfaceInstance() && existingConstraint.getInterfaceInstance()
                                                .getInterfaceInstanceID()
                                                .equals(newConstraints.get(0).getInterfaceInstance().getInterfaceInstanceID()))
                                .forEach(existingConstraint -> {

                                    if (newConstraints.stream().filter(constraint -> constraint.getConstraintID()
                                                    .equals(existingConstraint.getConstraintID())).collect(Collectors.toList())
                                            .isEmpty() && existingConstraint.getDeletableConstraint()) {
                                        constraintDAO.delete(existingConstraint);
                                    }

                                });

                        break;

                    case "COMPONENT_HOSTING":

                        applicationInstanceConstraints.stream().filter(
                                existingConstraint -> null != existingConstraint.getComponentNodeInstance() && existingConstraint.getComponentNodeInstance()
                                        .getComponentNodeInstanceID().equals(
                                                newConstraints.get(0).getComponentNodeInstance()
                                                        .getComponentNodeInstanceID())).forEach(existingConstraint -> {

                            if (newConstraints.stream().filter(constraint -> constraint.getConstraintID()
                                            .equals(existingConstraint.getConstraintID())).collect(Collectors.toList())
                                    .isEmpty() && existingConstraint.getDeletableConstraint()) {
                                constraintDAO.delete(existingConstraint);
                            }

                        });

                        break;
                    case "GRAPH_LINK":

                        applicationInstanceConstraints.stream().filter(
                                existingConstraint -> null != existingConstraint.getGraphLinkNodeInstance() && existingConstraint.getGraphLinkNodeInstance()
                                        .getGraphLinkNodeInstanceID().equals(
                                                newConstraints.get(0).getGraphLinkNodeInstance()
                                                        .getGraphLinkNodeInstanceID())).forEach(existingConstraint -> {

                            if (newConstraints.stream().filter(constraint -> constraint.getConstraintID()
                                            .equals(existingConstraint.getConstraintID())).collect(Collectors.toList())
                                    .isEmpty() && existingConstraint.getDeletableConstraint()) {
                                constraintDAO.delete(existingConstraint);
                            }

                        });

                        break;
                }

                return;

            } else {
                // TODO Send different message ??
            }
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public List<Constraint> fetchConstrainsByQiAndConstraintCategory(QI qi, String category) {
        return constraintDAO.findAllByQiAndConstraintCategory(qi, category);
    }

    public List<Constraint> fetchConstrainsByRadioServiceTypeAndConstraintCategory(RadioServiceType radioServiceType, String category) {
        return constraintDAO.findAllByRadioServiceTypeAndConstraintCategory(radioServiceType, category);
    }

    public void deleteAll(List<Constraint> constraints) {
        constraintDAO.deleteAll(constraints);
    }
}
