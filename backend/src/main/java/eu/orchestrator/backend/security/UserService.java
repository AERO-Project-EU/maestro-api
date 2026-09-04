package eu.orchestrator.backend.security;

import eu.orchestrator.repository.dao.UserDAO;
import eu.orchestrator.repository.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class UserService implements UserDetailsService {

    private final static Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private final AccountStatusUserDetailsChecker detailsChecker = new AccountStatusUserDetailsChecker();

    private final String USER_NOT_FOUND = "User: %s has not been found to the database";

    @Autowired
    UserDAO userDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Attempt to fetch user from database
        User user = userDAO.findByUsername(username).orElseThrow(() -> {
            LOGGER.severe(String.format(USER_NOT_FOUND, username));
            return new UsernameNotFoundException(String.format(USER_NOT_FOUND, username));
        });

        //Set the roles of current user
        Set<GrantedAuthority> roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(user.getRole()));

        //Create a new authenticated user
        org.springframework.security.core.userdetails.User authenticatedUser = new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), user.getEnabled(), true, true, true, roles);

        //Check Details of current user
        detailsChecker.check(authenticatedUser);

        return authenticatedUser;
    }


}
