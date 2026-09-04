package eu.orchestrator.backend.security;

public class SignatureNotVerifiedException extends Exception {

    public SignatureNotVerifiedException(String message) {
        super(message);
    }
}
