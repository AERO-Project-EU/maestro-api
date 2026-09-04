package eu.orchestrator.backend.security;

import eu.orchestrator.repository.dao.UserDAO;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.toSet;

public final class TokenHandler {

    private static final long TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 10; //Ten days
    private static final long ONE_DAY_TIME = 1000 * 60 * 60 * 24; //One day
    private static final String ROLES_DELIMITER = ",";
    private static final Logger LOGGER = Logger.getLogger(TokenHandler.class.getName());
    private final String signerSecret;
    private final String TOKEN_ISSUER = "orchestrator-app";
    private final String CLAIM_ROLE_NAME = "role";
    UserDAO userDAO;

    public TokenHandler(String signerSecret, UserDAO userDAO) {
        this.signerSecret = signerSecret;
        this.userDAO = userDAO;
    }

    /**
     * Returns a UserAuthantication object containing the principal and the role of the authenticated user.
     *
     * @param token The encrypted token to be parsed
     * @return UserAuthentication object
     */
    public UsernamePasswordAuthenticationToken parseUserFromToken(String token,
            HttpServletRequest httpRequest) {
        //Trim "Bearer " prefix from token
        token = token.substring(7);

        try {
            //Parse JWT
            SignedJWT jwt = JWTSecurityHandler.parseSignedJWT(token, signerSecret, httpRequest);
            //Check if the token has not exprired yet
            if (Date.from(Instant.ofEpochMilli(System.currentTimeMillis())).getTime() < jwt
                    .getJWTClaimsSet().getExpirationTime().getTime()) {
                //Set the roles of current user
                Set<GrantedAuthority> roles = Arrays.asList(
                                ((String) jwt.getJWTClaimsSet().getClaim(CLAIM_ROLE_NAME)).split(ROLES_DELIMITER))
                        .stream().map(SimpleGrantedAuthority::new).collect(toSet());

                // TODO
                eu.orchestrator.repository.domain.User user = userDAO
                        .findByUsername(jwt.getJWTClaimsSet().getSubject()).get();

                if (null != user && user.getEnabled()) {

                    //Token is not expired, return valid Authentication
                    return new UsernamePasswordAuthenticationToken(jwt.getJWTClaimsSet().getSubject(), "",
                            roles);
                    //return new UserAuthentication(new UserModel(jwt.getJWTClaimsSet().getSubject(), "", roles));
                }
            }//Token is expired
            else {
                //TODO: Handle token expiration issue
                LOGGER.log(Level.WARNING, "Token has been expired for user: {0}",
                        jwt.getJWTClaimsSet().getSubject());
//                LOGGER.log(Level.WARNING, "Request: " + new GsonBuilder().setPrettyPrinting().create().toJson(Util.serializeHttpServletRequestHeaders(httpRequest)));
            }
        } catch (ParseException | JOSEException | SignatureNotVerifiedException ex) {
//            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
//            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a signed & encrypted JWT.
     *
     * @param user The user object which the ClaimSet will be build on
     * @return An encrypted JWT for the specific user
     */
    public String createTokenForUser(User user) {
        //Try to create a JWT for specific user
        try {
            LOGGER.info(String
                    .format("Trying to create JWT for user: %s and role(s): %s", user.getUsername(),
                            user.getAuthorities().stream().map(auth -> auth.getAuthority())
                                    .collect(Collectors.joining(ROLES_DELIMITER))));
            // Prepare JWT with claims set
            JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issueTime(new Date())
                    .issuer(TOKEN_ISSUER)
                    .jwtID(UUID.randomUUID().toString())
                    .expirationTime(
                            Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME)))
                    .claim(CLAIM_ROLE_NAME,
                            user.getAuthorities().stream().map(auth -> "ROLE_".concat(auth.getAuthority()))
                                    .collect(Collectors.joining(ROLES_DELIMITER))).build();
            return JWTSecurityHandler.createSignedToken(signerSecret, jwtClaims);
        } catch (JOSEException ex) {
            LOGGER.severe(ex.getMessage());
        }
        return null;
    }

    /**
     * Creates a signed & encrypted JWT.
     *
     * @param user         The user object which the ClaimSet will be build on
     * @param daysToExpiry The amount of days that the token will be valid
     * @return An encrypted JWT for the specific user
     */
    public String createTokenForApi(User user, int daysToExpiry) {
        //Try to create a JWT for specific user
        try {
            LOGGER.info(String
                    .format("Trying to create JWT for user: %s and role(s): %s", user.getUsername(),
                            user.getAuthorities().stream().map(auth -> auth.getAuthority())
                                    .collect(Collectors.joining(ROLES_DELIMITER))));
            // Prepare JWT with claims set
            JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issueTime(new Date())
                    .issuer(TOKEN_ISSUER)
                    .jwtID(UUID.randomUUID().toString())
                    .expirationTime(
                            Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + ONE_DAY_TIME * daysToExpiry)))
                    .claim(CLAIM_ROLE_NAME,
                            user.getAuthorities().stream().map(auth -> "ROLE_".concat(auth.getAuthority()))
                                    .collect(Collectors.joining(ROLES_DELIMITER))).build();
            return JWTSecurityHandler.createSignedToken(signerSecret, jwtClaims);
        } catch (JOSEException ex) {
            LOGGER.severe(ex.getMessage());
        }
        return null;
    }

}
