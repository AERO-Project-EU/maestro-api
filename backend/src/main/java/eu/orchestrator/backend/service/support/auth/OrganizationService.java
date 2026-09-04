package eu.orchestrator.backend.service.support.auth;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.OrganizationTO;
import eu.orchestrator.repository.api.IFilesystemService;
import eu.orchestrator.repository.dao.OrganizationDAO;
import eu.orchestrator.repository.dao.UserDAO;
import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.Organization.Status;
import eu.orchestrator.repository.domain.User;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QOrganization.organization;

@Service
@Transactional(rollbackOn = Exception.class)
public class OrganizationService {

    private static final Logger logger = Logger.getLogger(OrganizationService.class.getName());

    @Autowired
    private OrganizationDAO organizationDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private IFilesystemService filesystemService;

    @Value("${nfs.path}")
    private String rootPath;


    public Page fetchOrganization(Pageable pageable, String filters, Organization fOrganization, User authenticatedUser) {
        BooleanExpression predicate = organization.eq(organization);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                Organization filterOrganization = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Organization.class);
                if (null != filterOrganization) {
                    proceedWithRequestBody = false;
                    if (null != filterOrganization.getName() && !filterOrganization.getName().isEmpty()) {
                        predicate = predicate.and(organization.name.containsIgnoreCase(filterOrganization.getName()));
                    }
                    if (null != filterOrganization.getStatus()) {
                        predicate = predicate.and(organization.status.eq(filterOrganization.getStatus()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fOrganization) {
                if (null != fOrganization.getName() && !fOrganization.getName().isEmpty()) {
                    predicate = predicate.and(organization.name.containsIgnoreCase(fOrganization.getName()));
                }
                if (null != fOrganization.getStatus()) {
                    predicate = predicate.and(organization.status.eq(fOrganization.getStatus()));
                }
            }
        }
        // Check if authenticated User has permission
        if (!authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.ORGANIZATION_NOT_AUTHORIZED.getCode(), GenericMessage.ORGANIZATION_NOT_AUTHORIZED);
        }
        Page<Organization> page;
        if (pageable.getPageSize() > 100) {
            page = organizationDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = organizationDAO.findAll(predicate, pageable);
        }
        List<OrganizationTO> organizationTOS = new ArrayList<>();
        if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
            User finalAuthenticatedUser = authenticatedUser;
            page.getContent().forEach(organization -> {
                OrganizationTO organizationTO = new OrganizationTO();
                organizationTO.setName(organization.getName());
                organizationTO.setAllowEdit(organization.hasEditAllowance(finalAuthenticatedUser));
                organizationTO.setId(organization.getId());
                organizationTO.setAllowDelete(organization.hasDeleteAllowance(finalAuthenticatedUser));
                organizationTO.setDateCreated(organization.getDateCreated());
                organizationTO.setLastModified(organization.getLastModified());
                organizationTO.setUsersCounter(organization.getUsers().size());
                organizationTO.setStatus(organization.getStatus());
                organizationTOS.add(organizationTO);
            });
        }
        return new PageImpl<>(organizationTOS, pageable, page.getTotalElements());
    }

    public Page fetchOrganizationNoAdmin(Pageable pageable, String filters, Organization fOrganization, User authenticatedUser) {
        BooleanExpression predicate = organization.eq(organization);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                Organization filterOrganization = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Organization.class);
                if (null != filterOrganization) {
                    proceedWithRequestBody = false;
                    if (null != filterOrganization.getName() && !filterOrganization.getName().isEmpty()) {
                        predicate = predicate.and(organization.name.containsIgnoreCase(filterOrganization.getName()));
                    }
                    if (null != filterOrganization.getStatus()) {
                        predicate = predicate.and(organization.status.eq(filterOrganization.getStatus()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fOrganization) {
                if (null != fOrganization.getName() && !fOrganization.getName().isEmpty()) {
                    predicate = predicate.and(organization.name.containsIgnoreCase(fOrganization.getName()));
                }
                if (null != fOrganization.getStatus()) {
                    predicate = predicate.and(organization.status.eq(fOrganization.getStatus()));
                }
            }
        }
        // Check if authenticated User has permission
        if (!authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.ORGANIZATION_NOT_AUTHORIZED.getCode(), GenericMessage.ORGANIZATION_NOT_AUTHORIZED);
        }
        Page<Organization> page;
        if (pageable.getPageSize() > 100) {
            page = organizationDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = organizationDAO.findAll(predicate, pageable);
        }
        List<OrganizationTO> organizationTOS = new ArrayList<>();
        if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
            User finalAuthenticatedUser = authenticatedUser;
            page.getContent().stream().filter(organization -> !organization.getName().equals("Admin_Organization")).forEach(organization -> {
                OrganizationTO organizationTO = new OrganizationTO();
                organizationTO.setName(organization.getName());
                organizationTO.setAllowEdit(organization.hasEditAllowance(finalAuthenticatedUser));
                organizationTO.setId(organization.getId());
                organizationTO.setAllowDelete(organization.hasDeleteAllowance(finalAuthenticatedUser));
                organizationTO.setDateCreated(organization.getDateCreated());
                organizationTO.setLastModified(organization.getLastModified());
                organizationTO.setUsersCounter(organization.getUsers().size());
                organizationTO.setStatus(organization.getStatus());
                organizationTOS.add(organizationTO);
            });
        }
        return new PageImpl<>(organizationTOS, pageable, page.getTotalElements());
    }

    public Organization fetchById(Long id, User authenticatedUser) {
        if (!authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.ORGANIZATION_NOT_AUTHORIZED.getCode(), GenericMessage.ORGANIZATION_NOT_AUTHORIZED);
        }
        Optional<Organization> organizationOP = organizationDAO.findById(id);
        return organizationOP.orElse(null);
    }

    public void create(Organization organization) {
        // Check if organization exists
        if (organizationDAO.findByName(organization.getName()).isPresent()) {
            throw new GenericBusinessException(GenericMessage.ORGANIZATION_NOT_AUTHORIZED.getCode(), GenericMessage.ORGANIZATION_NOT_AUTHORIZED);
        }
          /*  boolean updateUser = false;
            if (!authenticatedUser.isAdmin()) {
              if (null == authenticatedUser.getOrganization()) {
                updateUser = true;
              } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponseSPA(GenericMessage.USER_ASSIGNED_TO_ORGANIZATION.getCode(),
                        GenericMessage.USER_ASSIGNED_TO_ORGANIZATION.getMessage(request)));
              }
            }*/
        organization.setDateCreated(new Date());
        organization.setLastModified(new Date());
        organization.setStatus(Status.ACTIVE.name());
        organizationDAO.save(organization);
/*        if (updateUser) {
          authenticatedUser.setOrganization(organization);
          userDAO.save(authenticatedUser);
        }*/
        String pathAsString = rootPath + "/" + organization.getName();
        if (!filesystemService.createFolderIntoPath(pathAsString)) {
            organizationDAO.delete(organization);
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public void update(Organization organization, User authenticatedUser) {
        if (!authenticatedUser.isAdmin()) {
            throw new GenericBusinessException(GenericMessage.ORGANIZATION_NOT_AUTHORIZED.getCode(), GenericMessage.ORGANIZATION_NOT_AUTHORIZED);
        }
        Optional<Organization> existingOrganizationOP = organizationDAO.findById(organization.getId());
        if (existingOrganizationOP.isPresent()) {
            Organization existingOrganization = existingOrganizationOP.get();
            // Check if organization's name Admin_Organization
            if (existingOrganization.getName().compareTo("Admin_Organization") == 0) {
                throw new GenericBusinessException(GenericMessage.ORGANIZATION_CANNOT_EDIT.getCode(), GenericMessage.ORGANIZATION_CANNOT_EDIT);
            }
            // Check if organization's name exists
            if (organizationDAO.findByName(organization.getName()).isPresent()
                    && organizationDAO.findByName(organization.getName()).get() != existingOrganization) {
                throw new GenericBusinessException(GenericMessage.ORGANIZATION_ALREADY_EXISTS.getCode(), GenericMessage.ORGANIZATION_ALREADY_EXISTS);
            }
            existingOrganization.setName(organization.getName());
            existingOrganization.setStatus(organization.getStatus());
            existingOrganization.setLastModified(new Date());
            if (existingOrganization.getStatus().compareTo(Status.ACTIVE.name()) == 0) {
                // enable the organization Admin
                List<User> organizationUser = userDAO.findAllByOrganization(existingOrganization);
                organizationUser.stream().filter(user -> user.isOrganizationAdmin()).forEach(user -> {
                    user.setEnabled(true);
                    userDAO.save(user);
                });
            } else if (existingOrganization.getStatus().compareTo(Status.INACTIVE.name()) == 0) {
                // disable the organization users (admin + users)
                List<User> organizationUser = userDAO.findAllByOrganization(existingOrganization);
                organizationUser.stream().forEach(user -> {
                    user.setEnabled(false);
                    userDAO.save(user);
                });
            }
            String pathAsString = rootPath + "/" + existingOrganization.getName();

            if (filesystemService.createFolderIntoPath(pathAsString)) {
                organizationDAO.save(existingOrganization);
            } else {
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
        } else {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public void changeStatus(Long id, User authenticatedUser) {
        // Check if authenticated User has permission
        if (!authenticatedUser.isAdmin()) {
            throw new GenericBusinessException(GenericMessage.ORGANIZATION_NOT_AUTHORIZED.getCode(), GenericMessage.ORGANIZATION_NOT_AUTHORIZED);
        }
        Optional<Organization> existingOrganizationOP = organizationDAO.findById(id);
        if (existingOrganizationOP.isPresent()) {
            Organization existingOrganization = existingOrganizationOP.get();
            //check if is admin organization
            if (existingOrganization.getName().equals("Admin_Organization")) {
                throw new GenericBusinessException(GenericMessage.ORGANIZATION_CANNOT_EDIT.getCode(), GenericMessage.ORGANIZATION_CANNOT_EDIT);
            }
            if (existingOrganization.getStatus().equals(Status.ACTIVE.name())) {
                existingOrganization.setStatus(Status.INACTIVE.name());
            } else {
                existingOrganization.setStatus(Status.ACTIVE.name());
            }
            existingOrganization.setLastModified(new Date());
            organizationDAO.save(existingOrganization);
        } else {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public void delete(Long id, User authenticatedUser) {
        // Check if authenticated User has permission
        if (!authenticatedUser.isAdmin()) {
            throw new GenericBusinessException(GenericMessage.ORGANIZATION_NOT_AUTHORIZED.getCode(), GenericMessage.ORGANIZATION_NOT_AUTHORIZED);
        }
        // Check if organization already exists or not
        Optional<Organization> existingOrganizationOP = organizationDAO.findById(id);
        if (existingOrganizationOP.isPresent()) {
            Organization existingOrganization = existingOrganizationOP.get();
            if (existingOrganization.getName().compareTo("Admin_Organization") == 0) {
                throw new GenericBusinessException(GenericMessage.ORGANIZATION_CANNOT_DELETED.getCode(), GenericMessage.ORGANIZATION_CANNOT_DELETED);
            }
            List<User> organizationUser = userDAO.findAllByOrganization(existingOrganization);
            if (organizationUser.size() == 0) {
                organizationDAO.delete(existingOrganization);
            }
            if (organizationUser.size() > 0) {
                organizationUser.forEach(user -> {
                    user.setEnabled(false);
                    userDAO.save(user);
                });
                existingOrganization.setStatus(Status.INACTIVE.name());
                organizationDAO.save(existingOrganization);
            }
          /*// Check if organization is used
          List<Provider> providers = providerDAO
              .findAllByOrganization(existingOrganization);

          if (null != providers && !providers.isEmpty()) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestResponseSPA(GenericMessage.ORGANIZATION_HAS_PROVIDERS.getCode(),
                    GenericMessage.ORGANIZATION_HAS_PROVIDERS.getMessage(request)));
          }

          List<SSHKey> sshKeys = sshKeyDAO
              .findAllByOrganizationOrderByFriendlyNameAsc(existingOrganization);

          if (null != sshKeys && !sshKeys.isEmpty()) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestResponseSPA(GenericMessage.ORGANIZATION_HAS_SSH_KEYS.getCode(),
                    GenericMessage.ORGANIZATION_HAS_SSH_KEYS.getMessage(request)));
          }

          List<Component> components = componentDAO.findAllByOrganizationOrPublicComponent(existingOrganization, true);

          if (null != components && !components.isEmpty()) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestResponseSPA(GenericMessage.ORGANIZATION_HAS_COMPONENTS.getCode(),
                    GenericMessage.ORGANIZATION_HAS_COMPONENTS.getMessage(request)));
          }

          List<Application> applications = applicationDAO.findAllByOrganization(existingOrganization);

          if (null != applications && !applications.isEmpty()) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestResponseSPA(GenericMessage.ORGANIZATION_HAS_APPLICATIONS.getCode(),
                    GenericMessage.ORGANIZATION_HAS_APPLICATIONS.getMessage(request)));
          }

          List<ApplicationInstance> applicationInstances = applicationInstanceDAO
              .findAllByOrganization(existingOrganization);

          if (null != applicationInstances && !applicationInstances.isEmpty()) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestResponseSPA(GenericMessage.ORGANIZATION_HAS_APPLICATION_INSTANCES.getCode(),
                    GenericMessage.ORGANIZATION_HAS_APPLICATION_INSTANCES.getMessage(request)));
          }


          organizationDAO.delete(existingOrganization);
*/
        } else {
            throw new GenericBusinessException(GenericMessage.NOT_AUTHORIZED.getCode(), GenericMessage.NOT_AUTHORIZED);
        }
    }
}
