package eu.orchestrator.backend.service.applicationinstance;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.StatusTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.ComponentNodeInstanceStatusDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class StatusService {

    @Autowired
    ApplicationInstanceService applicationInstanceService;

    @Autowired
    ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO;


    public Page fetchStatusesByApplicationInstanceId(Long applicationInstanceId, Pageable pageable, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceId);

        List<StatusTO> statusesTOs = new ArrayList<>();

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) &&
                (authenticatedUser.isAdmin() || existingApplicationInstance.getOrganization().getId()
                        .equals(authenticatedUser.getOrganization().getId()))) {

            List<ComponentNodeInstance> componentNodeInstances
                    = componentNodeInstanceService.retrieveAllComponentNodeInstances(applicationInstanceId);

            if (NullCheckUtil.isNotEmpty(componentNodeInstances)) {

                Page<ComponentNodeInstanceStatus> page;

                if (pageable.getPageSize() > 100) {
                    page = componentNodeInstanceStatusDAO.findAllByComponentNodeInstanceInOrderByDateCreatedDesc(componentNodeInstances,
                            PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
                } else {
                    page = componentNodeInstanceStatusDAO.findAllByComponentNodeInstanceInOrderByDateCreatedDesc(componentNodeInstances, pageable);
                }
                if (null != page && !page.getContent().isEmpty()) {

                    page.getContent().forEach(status -> {
                        StatusTO statusTO = new StatusTO();
                        statusTO.setComponentNodeInstanceStatusID(status.getComponentNodeInstanceStatusID());
                        statusTO.setApplicationInstanceID(status.getComponentNodeInstance().getApplicationInstance()
                                .getApplicationInstanceID());
                        statusTO.setComponentNodeInstanceID(
                                status.getComponentNodeInstance().getComponentNodeInstanceID());
                        statusTO.setComponentNodeInstanceName(status.getComponentNodeInstance().getName());
                        statusTO.setDateCreated(status.getDateCreated());
                        statusTO.setLastModified(status.getLastModified());
                        statusTO.setMessage(status.getMessage());
                        statusTO.setReportedChange(status.getReportedChange());
                        statusTO.setStatus(status.getStatus());
                        statusesTOs.add(statusTO);
                    });
                    return new PageImpl<>(statusesTOs, pageable, page.getTotalElements());
                }
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public List<StatusTO> fetchStatusesLimitedById(Long applicationInstanceId, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceId);

        List<StatusTO> statusesTOs = new ArrayList<>();

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) &&
                (authenticatedUser.isAdmin()
                        || existingApplicationInstance.getOrganization().getId()
                        .equals(authenticatedUser.getOrganization().getId()))) {

            List<ComponentNodeInstance> componentNodeInstances
                    = componentNodeInstanceService.retrieveAllComponentNodeInstances(applicationInstanceId);

            if (NullCheckUtil.isNotEmpty(componentNodeInstances)) {

                List<ComponentNodeInstanceStatus> applicationInstanceStatuses = componentNodeInstanceStatusDAO
                        .findTop30ByComponentNodeInstanceInOrderByDateCreatedDesc(componentNodeInstances);

                if (null != applicationInstanceStatuses && !applicationInstanceStatuses.isEmpty()) {

                    applicationInstanceStatuses.forEach(status -> {

                        StatusTO statusTO = new StatusTO();
                        statusTO.setComponentNodeInstanceStatusID(status.getComponentNodeInstanceStatusID());
                        statusTO.setApplicationInstanceID(
                                status.getComponentNodeInstance().getApplicationInstance()
                                        .getApplicationInstanceID());
                        statusTO.setComponentNodeInstanceID(
                                status.getComponentNodeInstance().getComponentNodeInstanceID());
                        statusTO.setComponentNodeInstanceName(status.getComponentNodeInstance().getName());
                        statusTO.setDateCreated(status.getDateCreated());
                        statusTO.setLastModified(status.getLastModified());
                        statusTO.setMessage(status.getMessage());
                        statusTO.setReportedChange(status.getReportedChange());
                        statusTO.setStatus(status.getStatus());
                        statusesTOs.add(statusTO);

                    });
                    return statusesTOs;
                } else {
                    return statusesTOs;
                }
            }
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public List<ComponentNodeInstanceStatus> fetchAllComponentNodeInstanceStatusesByOrganizationOrderByDateCreatedAsc(User authenticatedUser) {
        return componentNodeInstanceStatusDAO.findAllByApplicationInstance_OrganizationOrderByDateCreatedAsc(authenticatedUser.getOrganization());
    }
}
