package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class NameMountPathPair implements Serializable {

    private String name;
    private String mountPath;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMountPath() {
        return mountPath;
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }


    public NameMountPathPair() {
    }

    public NameMountPathPair(String name, String mountPath) {
        this.name = name;
        this.mountPath = mountPath;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NameMountPathPair that = (NameMountPathPair) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return mountPath != null ? mountPath.equals(that.mountPath) : that.mountPath == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (mountPath != null ? mountPath.hashCode() : 0);
        return result;
    }
}
