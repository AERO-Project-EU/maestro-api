package eu.orchestrator.repository.api.user;

public class UsernameAlreadyExistsException extends Exception {

  private final String username;

  public UsernameAlreadyExistsException(String username) {
    this.username = username;
  }

  @Override
  public String getMessage() {
    return "Username '" + username + "' already exists";
  }

}
