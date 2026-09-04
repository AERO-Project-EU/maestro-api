package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class DeploymentLimitationsOptimizationVariablesTO implements Serializable {

    private String componentNodeHexID;
    private String friendliness;
    private String distance;
    private String cost;

    public String getComponentNodeHexID() {
        return componentNodeHexID;
    }

    public void setComponentNodeHexID(String componentNodeHexID) {
        this.componentNodeHexID = componentNodeHexID;
    }

    public String getFriendliness() {
        return friendliness;
    }

    public void setFriendliness(String friendliness) {
        this.friendliness = friendliness;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }
}