package eu.orchestrator.repository.api.user;

public class UserDoesNotExistException extends Exception {

  private final String id;

  public UserDoesNotExistException(String id) {
    this.id = id;
  }

  @Override
  public String getMessage() {
    return "User with ID " + id + " does not exists";
  }

}
