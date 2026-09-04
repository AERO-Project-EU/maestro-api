package eu.orchestrator.repository.api.user;

public class UsernameDoesNotExistException extends Exception {

  private final String username;

  public UsernameDoesNotExistException(String username) {
    this.username = username;
  }

  @Override
  public String getMessage() {
    return "User with username " + username + " does not exists";
  }

}
