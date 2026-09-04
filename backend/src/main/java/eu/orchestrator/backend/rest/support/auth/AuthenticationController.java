package eu.orchestrator.backend.rest.support.auth;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.response.BasicResponseCode;
import eu.orchestrator.transfer.response.RestResponse;
import eu.orchestrator.backend.service.support.auth.AuthenticationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private static final Logger logger = Logger.getLogger(AuthenticationController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping(value = "/register")
    public ResponseEntity<RestResponseSPA<Serializable>> register(@RequestBody User user, HttpServletRequest request) {
        boolean requiredFields = null != user && null != user.getUsername() && !user.getUsername().isEmpty() && null != user.getPassword()
                && !user.getPassword().isEmpty() && null != user.getFirstName() && !user.getFirstName().isEmpty() && null != user.getLastName()
                && !user.getLastName().isEmpty() && null != user.getEmail() && !user.getEmail().isEmpty() && null != user.getPhone()
                && !user.getPhone().isEmpty() && null != user.getCountry() && null != user.getCountry().getName() && !user.getCountry().getName().isEmpty();
        if (requiredFields) {
            try {
                authenticationService.Register(user);
                return ResponseEntity.status(HttpStatus.CREATED).build();
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @GetMapping(value = "/user")
    public ResponseEntity<RestResponseSPA<User>> getAuthUser(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.USER_AUTHENTICATED_EXISTS.getCode(),
                    GenericMessage.USER_AUTHENTICATED_EXISTS.getMessage(request), authService.getAuthenticatedUser()));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/logout")
    public RestResponse logout(HttpServletRequest request, HttpServletResponse response) {
        if (null == authService.getAuthenticatedUser()) {
            return new RestResponse(BasicResponseCode.EXCEPTION, GenericMessage.NOT_AUTHORIZED.getMessage(request));
        }
        Cookie cookie = new Cookie("auth_token", "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return new RestResponse(BasicResponseCode.SUCCESS, GenericMessage.USER_LOGOUT.getMessage(request));
    }

}
