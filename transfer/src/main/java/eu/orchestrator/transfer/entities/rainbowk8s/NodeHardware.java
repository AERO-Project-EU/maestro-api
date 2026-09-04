package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class NodeHardware implements Serializable {

    private CpuInfo cpuInfo;


    public CpuInfo getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(CpuInfo cpuInfo) {
        this.cpuInfo = cpuInfo;
    }


    public static class Builder {
        private CpuInfo cpuInfo;

        public NodeHardware.Builder withCpuInfo(CpuInfo cpuInfo) {
            this.cpuInfo = cpuInfo;
            return this;
        }

        public NodeHardware build() {
            NodeHardware nodeHardware = new NodeHardware();

            nodeHardware.setCpuInfo(this.cpuInfo);

            return nodeHardware;
        }
    }
}
