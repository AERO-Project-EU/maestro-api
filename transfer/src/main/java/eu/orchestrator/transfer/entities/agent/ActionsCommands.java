package eu.orchestrator.transfer.entities.agent;

/**
 * @author Panagiotis Parthenis
 */
public class ActionsCommands {

  private String id;

  private String command;

  public ActionsCommands() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  @Override
  public String toString() {
    return "ActionsCommands{" +
        "id='" + id + '\'' +
        ", command='" + command + '\'' +
        '}';
  }
}
