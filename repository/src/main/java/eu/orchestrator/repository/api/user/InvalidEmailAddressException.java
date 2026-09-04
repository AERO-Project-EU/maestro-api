package eu.orchestrator.repository.api.user;

public class InvalidEmailAddressException extends Exception {

  private final String email;

  public InvalidEmailAddressException(String email) {
    this.email = (null == email ? "" : email);
  }

  @Override
  public String getMessage() {
    return "Invalid email: '" + email + "'";
  }

}
