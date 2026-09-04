package eu.orchestrator.backend.service.support.auth;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.util.Patterns;
import eu.orchestrator.repository.dao.CountryDAO;
import eu.orchestrator.repository.dao.OrganizationDAO;
import eu.orchestrator.repository.dao.UserDAO;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.util.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class AuthenticationService {

    @Autowired
    CountryDAO countryDAO;
    @Autowired
    private UserDAO userDAO;

    @Autowired
    OrganizationDAO organizationDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void Register(User user) {
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

        boolean isEnabled = true;
        if (user.getEnabled() != null) {
            isEnabled = user.getEnabled();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.RoleName.USER.name());
        user.setDateCreated(new Date());
        user.setNotificationEmailEnabled(true);
        user.setNotificationWebEnabled(true);
        user.setCountry(countryDAO.findByName(user.getCountry().getName()).get());
        user.setFirstLogin(true);
        user.setEnabled(isEnabled);
        user.setOrganization(organizationDAO.findByName(user.getOrganization().getName()).get());
        userDAO.save(user);
    }


}
