package eu.orchestrator.backend.config;

import eu.orchestrator.repository.dao.TokenDAO;
import eu.orchestrator.repository.dao.UserDAO;
import eu.orchestrator.backend.security.LegacyShaPasswordEncoder;
import eu.orchestrator.backend.security.StatelessAuthenticationFilter;
import eu.orchestrator.backend.security.StatelessLoginFilter;
import eu.orchestrator.backend.security.StatelessTokenFilter;
import eu.orchestrator.backend.security.TokenAuthenticationService;
import eu.orchestrator.backend.security.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserService userService;
    private final TokenAuthenticationService tokenAuthenticationService;
    private final TokenDAO tokenDAO;
    private final UserDAO userDAO;

    @Autowired
    public WebSecurityConfig(UserService userService,
                             TokenAuthenticationService tokenAuthenticationService,
                             TokenDAO tokenDAO,
                             UserDAO userDAO) {
        this.userService = userService;
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.tokenDAO = tokenDAO;
        this.userDAO = userDAO;
    }

    /**
     * BCrypt for all new/changed passwords ({bcrypt} prefix); pre-existing unprefixed SHA-1 hashes
     * still verify via the legacy encoder (default-for-matches). This is the single migration point —
     * the same bean is used both for login matching and for encoding at registration/password-change.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("sha", new LegacyShaPasswordEncoder());
        DelegatingPasswordEncoder encoder = new DelegatingPasswordEncoder("bcrypt", encoders);
        // Existing DB hashes have no {id} prefix -> route them to the legacy SHA-1 encoder.
        encoder.setDefaultPasswordEncoderForMatches(new LegacyShaPasswordEncoder());
        return encoder;
    }

    // Replaces the AuthenticationManagerBuilder-based configureGlobal().
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager)
            throws Exception {
        http
                .authenticationManager(authenticationManager)
                // Stateless: no HTTP session
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Allow anonymous resource requests on the following URIs
                        .requestMatchers(HttpMethod.GET, "/api/v1/external/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/callback/**", "/api/v1/auth/register",
                                "/api/v1/scaling/**").permitAll()
                        .requestMatchers("/").permitAll()
                        // All other requests need to be authenticated
                        .anyRequest().authenticated())
                // custom JSON based authentication by POST of {"username":"<name>","password":"<password>"} which sets the token header upon authentication
                .addFilterBefore(new StatelessLoginFilter("/api/v1/auth/login", tokenAuthenticationService,
                        authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new StatelessTokenFilter("/api/v1/auth/token/{name}/{days}", tokenAuthenticationService, tokenDAO, userDAO,
                        authenticationManager), UsernamePasswordAuthenticationFilter.class)
                // custom Token based authentication based on the header previously given to the client
                .addFilterBefore(new StatelessAuthenticationFilter(tokenAuthenticationService), UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                // The @Bean-based HttpSecurity applies form-login/basic/logout by default; the previous
                // stateless config did not. Disable them to preserve the token-only behavior.
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

}
