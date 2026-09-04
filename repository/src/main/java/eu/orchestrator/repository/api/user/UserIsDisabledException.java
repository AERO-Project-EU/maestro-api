package eu.orchestrator.repository.api.user;

public class UserIsDisabledException extends RuntimeException {

    private final String username;

    public UserIsDisabledException(String username) {
        this.username = username;
    }

    @Override
    public String getMessage() {
        return "User account: " + username + " is disabled";
    }

}
