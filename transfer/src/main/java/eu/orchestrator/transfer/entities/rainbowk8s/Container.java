package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Container implements Serializable {

    private String name;
    private String image;
    private Resources resources;
    private Set<NameValuePair> env;
    private Set<NameMountPathPair> volumeMounts;
    private SecurityContext securityContext;
    private List<ContainerPortSection> ports;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public Set<NameValuePair> getEnv() {
        return env;
    }

    public void setEnv(Set<NameValuePair> env) {
        this.env = env;
    }

    public Set<NameMountPathPair> getVolumeMounts() {
        return volumeMounts;
    }

    public void setVolumeMounts(Set<NameMountPathPair> volumeMounts) {
        this.volumeMounts = volumeMounts;
    }

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public List<ContainerPortSection> getPorts() {
        if (ports == null) {
            ports = new ArrayList<>();
        }
        return ports;
    }

    public void setPorts(List<ContainerPortSection> ports) {
        this.ports = ports;
    }


    public static class Builder {

        private String name;
        private String image;
        private Resources resources;
        private Set<NameValuePair> env;
        private Set<NameMountPathPair> volumeMounts;
        private SecurityContext securityContext;
        private List<ContainerPortSection> ports;

        public Container.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Container.Builder withImage(String image) {
            this.image = image;
            return this;
        }

        public Container.Builder withResources(Resources resources) {
            this.resources = resources;
            return this;
        }

        public Container.Builder withEnv(Set<NameValuePair> env) {
            this.env = env;
            return this;
        }

        public Container.Builder withVolumeMounts(Set<NameMountPathPair> volumeMounts) {
            this.volumeMounts = volumeMounts;
            return this;
        }

        public Container.Builder withSecurityContext(SecurityContext securityContext) {
            this.securityContext = securityContext;
            return this;
        }

        public Container.Builder withPorts(List<ContainerPortSection> ports) {
            this.ports = ports;
            return this;
        }

        public Container build() {
            Container container = new Container();

            container.setName(this.name);
            container.setImage(this.image);
            container.setResources(this.resources);
            container.setEnv(this.env);
            container.setVolumeMounts(this.volumeMounts);
            container.setSecurityContext(this.securityContext);
            container.setPorts(this.ports);

            return container;
        }
    }
}
