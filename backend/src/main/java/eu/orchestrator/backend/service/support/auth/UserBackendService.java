package eu.orchestrator.backend.service.support.auth;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.TOConverter;
import eu.orchestrator.backend.util.Patterns;
import eu.orchestrator.repository.dao.UserDAO;
import eu.orchestrator.repository.domain.Country;
import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.util.Util;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QUser.user;

@Service
@Transactional(rollbackOn = Exception.class)
public class UserBackendService {

    private static final Logger logger = Logger.getLogger(UserBackendService.class.getName());

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private CountryService countryService;


    public User fetchById(Long id, User authenticatedUser) {
        Optional<User> userOP = userDAO.findById(id);
        if (userOP.isPresent() && userOP.get().hasEditAllowance(authenticatedUser)) {
            return userOP.get();
        } else {
            throw new NotAuthorizedException(GenericMessage.USER_NOT_AUTHORIZED.getCode(), GenericMessage.USER_NOT_AUTHORIZED);
        }
    }

    public void save(User user) {
        userDAO.save(user);
    }

    public Page fetchUsers(Pageable pageable, String filters, User fUser, User authenticatedUser) {
        BooleanExpression predicate = user.eq(user);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                User filterUser = new Gson().fromJson(new String(Base64.decodeBase64(filters)), User.class);
                if (null != filterUser) {
                    proceedWithRequestBody = false;
                    if (null != filterUser.getUsername() && !filterUser.getUsername().isEmpty()) {
                        predicate = predicate.and(user.username.containsIgnoreCase(filterUser.getUsername())
                                .or(user.firstName.containsIgnoreCase(filterUser.getUsername())).or(user.lastName.containsIgnoreCase(filterUser.getUsername())
                                        .or(user.email.containsIgnoreCase(filterUser.getUsername()))).or(user.phone.containsIgnoreCase(filterUser.getUsername())
                                        .or(user.country.name.containsIgnoreCase(filterUser.getUsername()))));
                    }
                    if (null != filterUser.getEnabled()) {
                        predicate = predicate.and(user.enabled.eq(filterUser.getEnabled()));
                    }
                    if (null != filterUser.getFirstLogin()) {
                        predicate = predicate.and(user.firstLogin.eq(filterUser.getFirstLogin()));
                    }
                    if (null != filterUser.getRole()) {
                        predicate = predicate.and(user.role.eq(filterUser.getRole()));
                    }
                    if (null != filterUser.getOrganization()) {
                        predicate = predicate.and(user.organization.eq(filterUser.getOrganization()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fUser) {
                if (null != fUser.getUsername() && !fUser.getUsername().isEmpty()) {
                    predicate = predicate.and(user.username.containsIgnoreCase(fUser.getUsername()).or(user.firstName.containsIgnoreCase(fUser.getUsername()))
                            .or(user.lastName.containsIgnoreCase(fUser.getUsername()).or(user.email.containsIgnoreCase(fUser.getUsername())))
                            .or(user.phone.containsIgnoreCase(fUser.getUsername()).or(user.country.name.containsIgnoreCase(fUser.getUsername()))));
                }
                if (null != fUser.getEnabled()) {
                    predicate = predicate.and(user.enabled.eq(fUser.getEnabled()));
                }
                if (null != fUser.getFirstLogin()) {
                    predicate = predicate.and(user.firstLogin.eq(fUser.getFirstLogin()));
                }
                if (null != fUser.getRole()) {
                    predicate = predicate.and(user.role.eq(fUser.getRole()));
                }
                if (null != fUser.getOrganization()) {
                    predicate = predicate.and(user.organization.eq(fUser.getOrganization()));
                }
            }
        }
        if (!authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.USER_NOT_AUTHORIZED.getCode(), GenericMessage.USER_NOT_AUTHORIZED);
        }
        Page<User> page;
        if (pageable.getPageSize() > 100) {
            page = userDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = userDAO.findAll(predicate, pageable);
        }
        return new TOConverter(User.class.getName(), page, pageable, authenticatedUser).convertToTOWithPermissions();
    }

    public void create(User user, User authenticatedUser) {
        if (!authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.USER_NOT_AUTHORIZED.getCode(), GenericMessage.USER_NOT_AUTHORIZED);
        }
        // Check if username exists
        if (userDAO.findByUsername(user.getUsername()).isPresent()) {
            throw new GenericBusinessException(GenericMessage.USERNAME_ALREADY_EXISTS.getCode(), GenericMessage.USERNAME_ALREADY_EXISTS);
        }
        // Check if username is valid
        if (user.getUsername().length() < 4) {
            throw new GenericBusinessException(GenericMessage.USERNAME_TOO_SHORT.getCode(), GenericMessage.USERNAME_TOO_SHORT);
        }
        // Check if password is valid
        if (!Util.isValidPassword(user.getPassword())) {
            throw new GenericBusinessException(GenericMessage.PASSWORD_POLICY_REQUIREMENTS.getCode(), GenericMessage.PASSWORD_POLICY_REQUIREMENTS);
        }
        // Check if email is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(user.getEmail()).matches()) {
            throw new GenericBusinessException(GenericMessage.EMAIL_INVALID_FORMAT.getCode(), GenericMessage.EMAIL_INVALID_FORMAT);
        } else {
            // Check if email exists
            if (userDAO.findByEmail(user.getEmail()).isPresent()) {
                throw new GenericBusinessException(GenericMessage.EMAIL_ALREADY_EXISTS.getCode(), GenericMessage.EMAIL_ALREADY_EXISTS);
            }
        }
        if (!user.getPassword().equals(user.getVerifyPassword())) {
            throw new GenericBusinessException(GenericMessage.USER_PASSWORD_NO_MATCH.getCode(), GenericMessage.USER_PASSWORD_NO_MATCH);
        }
        if (!authenticatedUser.isAdmin()) {
            if (null != authenticatedUser.getOrganization()) {
                user.setOrganization(authenticatedUser.getOrganization());
                user.setRole(authenticatedUser.getRole());
            } else {
                user.setOrganization(null);
                user.setRole(authenticatedUser.getRole());
            }
        }
        // Check if organization and role are valid
        if (!user.getRole().equals("ORGANIZATIONADMIN") && !user.getRole().equals("USER")) {
            throw new GenericBusinessException(GenericMessage.USER_CHANGE_ROLE_NOT_EXIST.getCode(), GenericMessage.USER_CHANGE_ROLE_NOT_EXIST);
        }
        Organization organization = organizationService.fetchById(user.getOrganization().getId(), authenticatedUser);
        //Optional<Organization> organizationOptional = organizationDAO.findById(user.getOrganization().getId());
        // Check if organization id exist
        if (organization == null) {
            throw new GenericBusinessException(GenericMessage.ORGANIZATION_NOT_EXISTS.getCode(), GenericMessage.ORGANIZATION_NOT_EXISTS);
        }
        // Check if organization is not Admin_Organization
        if (organization.getName().equals("Admin_Organization")) {
            throw new GenericBusinessException(GenericMessage.ORGANIZATION_NOT_AUTHORIZED.getCode(), GenericMessage.ORGANIZATION_NOT_AUTHORIZED);
        }
        user.setOrganization(organization);
        Country country = countryService.fetchCountryByName(user.getCountry().getName());
        //check if country exist
        if (country == null) {
            throw new GenericBusinessException(GenericMessage.COUNTRY_NOT_EXIST.getCode(), GenericMessage.COUNTRY_NOT_EXIST);
        }
        user.setCountry(country);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDateCreated(new Date());
        user.setNotificationEmailEnabled(true);
        user.setNotificationWebEnabled(true);
        user.setFirstLogin(true);
        user.setEnabled(true);
        userDAO.save(user);
    }

    public void update(User user, User authenticatedUser) {
        Optional<User> existingUserOP = userDAO.findById(user.getId());
        if (existingUserOP.isPresent() && existingUserOP.get().hasEditAllowance(authenticatedUser)) {
            // Check if password is match with verify password
            if (!user.getNewPassword().equals(user.getVerifyPassword())) {
                throw new GenericBusinessException(GenericMessage.USER_PASSWORD_NO_MATCH.getCode(), GenericMessage.USER_PASSWORD_NO_MATCH);
            }
            // Check if password is valid
            if (!Util.isValidPassword(user.getNewPassword())) {
                throw new GenericBusinessException(GenericMessage.PASSWORD_POLICY_REQUIREMENTS.getCode(), GenericMessage.PASSWORD_POLICY_REQUIREMENTS);
            }
            //check if role || organization changed
            if (!existingUserOP.get().getOrganization().getName().equals(user.getOrganization().getName())
                    || !existingUserOP.get().getRole().equals(user.getRole())) {
                throw new GenericBusinessException(GenericMessage.USER_CHANGE_ROLE_OR_ORGANIZATION.getCode(), GenericMessage.USER_CHANGE_ROLE_OR_ORGANIZATION);
            }
            User existingUser = existingUserOP.get();
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setPhone(user.getPhone());
            existingUser.setPassword(passwordEncoder.encode(user.getNewPassword()));
            existingUser.setCountry(countryService.fetchCountryById(user.getCountry().getId()));
            existingUser.setEnabled(user.getEnabled() == null ? existingUser.getEnabled() : user.getEnabled());
            userDAO.save(existingUser);
        } else {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public void changeStatus(Long id, User authenticatedUser) {
        if (!authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.USER_NOT_AUTHORIZED.getCode(), GenericMessage.USER_NOT_AUTHORIZED);
        }
        // Check if user already exists or not
        User existingUser = fetchById(id, authenticatedUser);
        if (existingUser != null) {
            existingUser.setEnabled(Boolean.FALSE.equals(existingUser.getEnabled()));
            userDAO.save(existingUser);
        } else {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public void delete(Long id, User authenticatedUser) {
        if (!authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.USER_NOT_AUTHORIZED.getCode(), GenericMessage.USER_NOT_AUTHORIZED);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA(GenericMessage.USER_NOT_AUTHORIZED.getCode(),
//                            GenericMessage.USER_NOT_AUTHORIZED.getMessage(request)));
        }
        User existingUser = fetchById(id, authenticatedUser);
        if (existingUser != null) {
            existingUser.setEnabled(false);
            userDAO.save(existingUser);
                 /* // Check if user is used
          List<Provider> providers = providerDAO
              .findAllByUser(existingUser);

          if (null != providers && !providers.isEmpty()) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestResponseSPA(GenericMessage.USER_HAS_PROVIDERS.getCode(),
                    GenericMessage.USER_HAS_PROVIDERS.getMessage(request)));
          }

          List<SSHKey> sshKeys = sshKeyDAO
              .findAllByUserOrderByFriendlyNameAsc(existingUser);

          if (null != sshKeys && !sshKeys.isEmpty()) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestResponseSPA(GenericMessage.USER_HAS_SSH_KEYS.getCode(),
                    GenericMessage.USER_HAS_SSH_KEYS.getMessage(request)));
          }

          List<Component> components = componentDAO.findAllByUser(existingUser);

          if (null != components && !components.isEmpty()) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestResponseSPA(GenericMessage.USER_HAS_COMPONENTS.getCode(),
                    GenericMessage.USER_HAS_COMPONENTS.getMessage(request)));
          }

          List<Application> applications = applicationDAO.findAllByUser(existingUser);

          if (null != applications && !applications.isEmpty()) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestResponseSPA(GenericMessage.USER_HAS_APPLICATIONS.getCode(),
                    GenericMessage.USER_HAS_APPLICATIONS.getMessage(request)));
          }

          List<ApplicationInstance> applicationInstances = applicationInstanceDAO
              .findAllByUser(existingUser);

          if (null != applicationInstances && !applicationInstances.isEmpty()) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestResponseSPA(GenericMessage.USER_HAS_APPLICATION_INSTANCES.getCode(),
                    GenericMessage.USER_HAS_APPLICATION_INSTANCES.getMessage(request)));
          }

          userDAO.delete(existingUser);*/
        } else {
            throw new NotAuthorizedException(GenericMessage.NOT_AUTHORIZED.getCode(), GenericMessage.NOT_AUTHORIZED);
        }
    }

}
