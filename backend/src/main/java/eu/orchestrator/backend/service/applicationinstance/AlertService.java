package eu.orchestrator.backend.service.applicationinstance;

import eu.orchestrator.backend.transfer.AlertTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.ComponentNodeInstanceAlertDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAlert;
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
public class AlertService {

    @Autowired
    private ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    private ComponentNodeInstanceAlertDAO componentNodeInstanceAlertDAO;


    public Page fetchAlertsById(ApplicationInstance existingApplicationInstance, Pageable pageable, User authenticatedUser) {
        Page<ComponentNodeInstanceAlert> page = null;
        if ((authenticatedUser.isAdmin() || existingApplicationInstance.getOrganization().getId().equals(authenticatedUser.getOrganization().getId()))) {
            List<ComponentNodeInstance> componentNodeInstances = componentNodeInstanceDAO.findAllByApplicationInstance(existingApplicationInstance);
            if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {
                if (pageable.getPageSize() > 100) {
                    page = componentNodeInstanceAlertDAO.findAllByComponentNodeInstanceInOrderByDateCreatedDesc(componentNodeInstances,
                            PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
                } else {
                    page = componentNodeInstanceAlertDAO.findAllByComponentNodeInstanceInOrderByDateCreatedDesc(componentNodeInstances, pageable);
                }
            }
        }
        if (NullCheckUtil.isNotEmpty(page) && NullCheckUtil.isNotEmpty(page.getContent())) {
            return new PageImpl<>(fetchAlerts(page.getContent()), pageable, page.getTotalElements());
        } else {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public List<AlertTO> fetchAlertsLimitedById(ApplicationInstance existingApplicationInstance, User authenticatedUser) {
        List<AlertTO> alertTOs = new ArrayList<>();

        if (authenticatedUser.isAdmin() || existingApplicationInstance.getOrganization().getId()
                .equals(authenticatedUser.getOrganization().getId())) {

            List<ComponentNodeInstance> componentNodeInstances = componentNodeInstanceDAO
                    .findAllByApplicationInstance(existingApplicationInstance);

            if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {

                List<ComponentNodeInstanceAlert> applicationInstanceAlerts = componentNodeInstanceAlertDAO
                        .findTop30ByComponentNodeInstanceInOrderByDateCreatedDesc(componentNodeInstances);

                if (null != applicationInstanceAlerts && !applicationInstanceAlerts.isEmpty()) {
                    alertTOs = fetchAlerts(applicationInstanceAlerts);
                }
            }
        }

        return alertTOs;

    }

    public List<ComponentNodeInstanceAlert> fetchAllByApplicationInstanceOrganizationOrderByDateCreatedAsc(User authenticatedUser) {
        return componentNodeInstanceAlertDAO.findAllByApplicationInstance_OrganizationOrderByDateCreatedAsc(authenticatedUser.getOrganization());
    }

    public Long countComponentNodeInstanceAlerts(Long applicationInstanceId, Long componentNodeInstanceId) {
        return componentNodeInstanceAlertDAO.calculateComponentNodeInstanceAlerts(applicationInstanceId, componentNodeInstanceId);
    }

    private List<AlertTO> fetchAlerts(List<ComponentNodeInstanceAlert> componentNodeInstanceAlertList) {

        List<AlertTO> alertTOs = new ArrayList<>();

        componentNodeInstanceAlertList.forEach(alert -> {

            AlertTO alertTO = new AlertTO();
            alertTO.setComponentNodeInstanceAlertID(alert.getComponentNodeInstanceAlertID());
            alertTO.setApplicationInstanceID(
                    alert.getComponentNodeInstance().getApplicationInstance()
                            .getApplicationInstanceID());
            alertTO.setComponentNodeInstanceID(
                    alert.getComponentNodeInstance().getComponentNodeInstanceID());
            alertTO.setComponentNodeInstanceName(alert.getComponentNodeInstance().getName());
            alertTO.setGraphInstanceID(
                    alert.getComponentNodeInstance().getApplicationInstance().getApplicationInstanceID()
                            + "");
            alertTO.setMessage(alert.getMessage());
            alertTO.setStatus(alert.getStatus());
            alertTO.setDateCreated(alert.getDateCreated());
            alertTO.setLastModified(alert.getLastModified());
            alertTOs.add(alertTO);

        });

        return alertTOs;
    }

}
