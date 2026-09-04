package eu.orchestrator.repository.api.user;

public class UsernameIsTooShortException extends Exception {

  @Override
  public String getMessage() {
    return "Username is too short.";
  }


}
