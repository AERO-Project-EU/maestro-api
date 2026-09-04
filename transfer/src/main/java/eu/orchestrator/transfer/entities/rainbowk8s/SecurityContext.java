package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class SecurityContext implements Serializable {

    private boolean privileged;


    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    public static class Builder {
        private boolean privileged;

        public SecurityContext.Builder withPrivileged(boolean privileged) {
            this.privileged = privileged;
            return this;
        }

        public SecurityContext build() {
            SecurityContext securityContext = new SecurityContext();

            securityContext.setPrivileged(this.privileged);

            return securityContext;
        }
    }
}
