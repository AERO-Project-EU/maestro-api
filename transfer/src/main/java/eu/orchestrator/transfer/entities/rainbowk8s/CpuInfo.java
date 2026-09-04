package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CpuInfo implements Serializable {

    private List<String> architectures;


    public List<String> getArchitectures() {
        if(architectures == null) {
            architectures = new ArrayList<>();
        }
        return architectures;
    }

    public void setArchitectures(List<String> architectures) {
        this.architectures = architectures;
    }


    public static class Builder {
        private List<String> architectures;

        public CpuInfo.Builder withArchitectures(List<String> architectures) {
            this.architectures = architectures;
            return this;
        }

        public CpuInfo build() {
            CpuInfo cpuInfo = new CpuInfo();

            cpuInfo.setArchitectures(this.architectures);

            return cpuInfo;
        }
    }

}
