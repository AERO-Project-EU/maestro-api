package eu.orchestrator.repository.api.user;

public class WeakPasswordException extends Exception {

  private final int minimumCharacters;

  public WeakPasswordException(int minimumCharacters) {
    this.minimumCharacters = minimumCharacters;
  }

  @Override
  public String getMessage() {
    return "Password is weak. Password must be at least " + minimumCharacters + " characters";
  }

}
