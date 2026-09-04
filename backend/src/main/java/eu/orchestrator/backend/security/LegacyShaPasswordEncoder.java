package eu.orchestrator.backend.security;

import eu.orchestrator.transfer.util.Util;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Legacy SHA-1 password encoder, kept only so that pre-existing (unprefixed) SHA-1 password hashes
 * continue to verify after the migration to BCrypt. It is wired into the {@code DelegatingPasswordEncoder}
 * as the encoder for the {@code {sha}} id and as the default-for-matches (bare, unprefixed hashes).
 *
 * <p>New and changed passwords are encoded with BCrypt (see {@code WebSecurityConfig#passwordEncoder()}),
 * so nothing new is ever hashed with SHA-1.
 */
public class LegacyShaPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return Util.createAlgorithm(rawPassword.toString(), Util.ALGORITHM.SHA.name());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword != null && encode(rawPassword).equals(encodedPassword);
    }
}
