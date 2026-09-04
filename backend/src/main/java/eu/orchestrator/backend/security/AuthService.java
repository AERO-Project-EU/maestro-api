package eu.orchestrator.backend.security;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.repository.api.user.UserIsDisabledException;
import eu.orchestrator.repository.dao.UserDAO;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.common.exception.NotAuthorizedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class AuthService {

    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    @Autowired
    UserDAO userDAO;

    /**
     * Retrieve the current logged-in user
     *
     * @return An instance of CurrentUser object
     */
    public static Authentication getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Check if a user has a JWT AccessToken
     *
     * @return True if user has a JWT in headers, otherwise returns false
     */
    public static boolean hasAccessToken() {
        return SecurityContextHolder.getContext()
                .getAuthentication() instanceof UsernamePasswordAuthenticationToken;
    }

    /**
     * Returns currently authenticated user from database
     *
     * @return An instance of User object
     */
    public User getAuthenticatedUser() {

        User user = userDAO.findByUsername(getCurrentUser().getName()).orElseThrow(() ->
                new NotAuthorizedException(GenericMessage.NOT_AUTHORIZED.getCode(), GenericMessage.NOT_AUTHORIZED));

        if (!user.getEnabled()) {
            throw new UserIsDisabledException(getCurrentUser().getName());
        }

        //Return actual user
        return user;

    }
}
