package eu.orchestrator.backend.security;

import eu.orchestrator.repository.dao.TokenDAO;
import eu.orchestrator.repository.dao.UserDAO;
import eu.orchestrator.repository.domain.Token;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.transfer.UserTokenTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class StatelessTokenFilter extends AbstractAuthenticationProcessingFilter {

    private final TokenAuthenticationService tokenAuthenticationService;
    private final TokenDAO tokenDAO;
    private final UserDAO userDAO;

    public StatelessTokenFilter(String urlMapping,
            TokenAuthenticationService tokenAuthenticationService, TokenDAO tokenDAO, UserDAO userDAO,
            AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(urlMapping));
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.tokenDAO = tokenDAO;
        this.userDAO = userDAO;
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        final User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
        final UsernamePasswordAuthenticationToken loginToken = new UsernamePasswordAuthenticationToken(
                user.getUsername(), user.getPassword());
        return getAuthenticationManager().authenticate(loginToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication)
            throws IOException, ServletException {

        Boolean proceed = true;

        String requestedUri = request.getRequestURI();
        if (requestedUri == null || requestedUri.isEmpty()) {
            proceed = false;
        } else {

            String[] requestedUriSplitted = Arrays.copyOfRange(requestedUri.split("/"), 1, requestedUri.split("/").length);
            String[] checkedUriElem = new String[]{"api", "v1", "auth", "token"};

            if (requestedUriSplitted.length != (checkedUriElem.length + 2)) {
                proceed = false;
            } else {
                for (int i = 0; i < requestedUriSplitted.length - 2; i++) {
                    if (!requestedUriSplitted[i].equals(checkedUriElem[i])) {
                        proceed = false;
                        break;
                    }
                }

                if (proceed) {
                    String tokenName = requestedUriSplitted[requestedUriSplitted.length - 2];
                    String daysVariable = requestedUriSplitted[requestedUriSplitted.length - 1];
                    String regexDays = "[0-9]+";
                    String regexName = "[A-z]\\w+\\d*";
                    // Compile the ReGex
                    Pattern daysPattern = Pattern.compile(regexDays);
                    Pattern namePattern = Pattern.compile(regexName);
                    Matcher daysMatcher = daysPattern.matcher(daysVariable);
                    Matcher nameMatcher = namePattern.matcher(tokenName);

                    if (daysMatcher.matches() && nameMatcher.matches()) {

                        Optional<Token> tokenCheck = tokenDAO.findByName(tokenName);
                        Optional<User> user = userDAO.findByUsername(((UserDetails) authentication.getPrincipal()).getUsername());

                        if (tokenCheck.isPresent() || !user.isPresent()) {
                            proceed = false;
                        } else {

                            Integer days = Integer.parseInt(daysVariable);

                            // Add the custom token as HTTP header to the response
                            Cookie authenticationCookie = tokenAuthenticationService.addAccessToken(response, (UserDetails) authentication.getPrincipal(),
                                    days);
                            // Add the authentication to the Security context
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            Date currentDate = new Date();

                            // convert date to calendar
                            Calendar c = Calendar.getInstance();
                            c.setTime(currentDate);

                            c.add(Calendar.DATE, days - 1); //same with c.add(Calendar.DAY_OF_MONTH, 1);
                            c.add(Calendar.HOUR, 23);
                            c.add(Calendar.MINUTE, 58);

                            Date expirationDate = c.getTime();

                            UserTokenTO userToken = new UserTokenTO();
                            userToken.setUsername(((UserDetails) authentication.getPrincipal()).getUsername());
                            userToken.setExpirationDate(expirationDate);
                            userToken.setToken(authenticationCookie.getValue());
                            userToken.setName(tokenName);

                            Token newToken = new Token();
                            newToken.setName(tokenName);
                            newToken.setToken(authenticationCookie.getValue());
                            newToken.setExpirationDate(expirationDate);
                            newToken.setUser(user.get());
                            newToken.setOrganization(user.get().getOrganization());
                            newToken.setDateCreated(new Date());
                            tokenDAO.save(newToken);

                            String userTokenJson = new ObjectMapper().writeValueAsString(userToken);

                            response.getWriter().write(userTokenJson);
                            response.getWriter().flush();
                        }
                    } else {
                        proceed = false;
                    }
                }
            }
        }

        if (!proceed) {
            SecurityContextHolder.getContext().setAuthentication(null);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            Cookie cookie = new Cookie("auth_token", "");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
            return;
        }
    }
}
