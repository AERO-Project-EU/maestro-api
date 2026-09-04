package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class Limits implements Serializable {

    private String memory;
    private String cpu;


    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }


    public static class Builder {
        private String memory;
        private String cpu;

        public Limits.Builder withMemory(String memory) {
            this.memory = memory;
            return this;
        }

        public Limits.Builder withCpu(String cpu) {
            this.cpu = cpu;
            return this;
        }

        public Limits build() {
            Limits limits = new Limits();

            limits.setCpu(this.cpu);
            limits.setMemory(this.memory);

            return limits;
        }
    }
}
