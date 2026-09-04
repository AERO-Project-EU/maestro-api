package eu.orchestrator.transfer.entities.soc;

import java.io.Serializable;
import java.util.Map;

public class OutputDroolsAction implements Serializable {

  private Type type;
  private String url;
  private String context;
  private RestMethod restMethod;
  private SocAction socAction;
  private String brokerTopicName;
  private String graphHexID;
  private String graphInstanceHexID;
  private String componentNodeHexID;
  private String componentNodeInstanceHexID;
  private Map<String,String> header;


  public enum Type {
    SOC_ACTION,
    COMMAND,
    INTERACT_WITH_WAZUH,
    INTERACT_WITH_OWLH,
    REST_CALL,
    PUSH_MESSAGE_TO_KAFKA,
    PUSH_MESSAGE_TO_RABBITMQ;
  }

  public enum SocAction {
    BPF_BLACKLIST;
  }

  public enum RestMethod {
    POST,
    GET,
    PUT,
    DELETE;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public RestMethod getRestMethod() {
    return restMethod;
  }

  public void setRestMethod(RestMethod restMethod) {
    this.restMethod = restMethod;
  }

  public SocAction getSocAction() {
    return socAction;
  }

  public void setSocAction(SocAction socAction) {
    this.socAction = socAction;
  }

  public String getBrokerTopicName() {
    return brokerTopicName;
  }

  public void setBrokerTopicName(String brokerTopicName) {
    this.brokerTopicName = brokerTopicName;
  }

  public String getGraphHexID() {
    return graphHexID;
  }

  public void setGraphHexID(String graphHexID) {
    this.graphHexID = graphHexID;
  }

  public String getGraphInstanceHexID() {
    return graphInstanceHexID;
  }

  public void setGraphInstanceHexID(String graphInstanceHexID) {
    this.graphInstanceHexID = graphInstanceHexID;
  }

  public String getComponentNodeHexID() {
    return componentNodeHexID;
  }

  public void setComponentNodeHexID(String componentNodeHexID) {
    this.componentNodeHexID = componentNodeHexID;
  }

  public String getComponentNodeInstanceHexID() {
    return componentNodeInstanceHexID;
  }

  public void setComponentNodeInstanceHexID(String componentNodeInstanceHexID) {
    this.componentNodeInstanceHexID = componentNodeInstanceHexID;
  }

  public Map<String, String> getHeader() {
    return header;
  }

  public void setHeader(Map<String, String> header) {
    this.header = header;
  }
}
