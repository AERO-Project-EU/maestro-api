package eu.orchestrator.transfer.entities.policyEngine;


import java.io.Serializable;

public class PolicyEngineAction implements Serializable {
        private String componentNodeInstanceHexID;
        private PolicyEngineActionType type;
        private Integer workersNumber;

        public String getComponentNodeInstanceHexID() {
                return componentNodeInstanceHexID;
        }

        public void setComponentNodeInstanceHexID(String componentNodeInstanceHexID) {
                this.componentNodeInstanceHexID = componentNodeInstanceHexID;
        }

        public PolicyEngineActionType getType() {
                return type;
        }

        public void setType(PolicyEngineActionType type) {
                this.type = type;
        }

        public Integer getWorkersNumber() {
                return workersNumber;
        }

        public void setWorkersNumber(Integer workersNumber) {
                this.workersNumber = workersNumber;
        }

        @Override
        public String toString() {
                return "PolicyEngineAction{" +
                        "componentNodeInstanceHexID='" + componentNodeInstanceHexID + '\'' +
                        ", type=" + type +
                        ", workersNumber=" + workersNumber +
                        '}';
        }
}