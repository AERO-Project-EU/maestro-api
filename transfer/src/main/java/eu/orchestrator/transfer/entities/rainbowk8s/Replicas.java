package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class Replicas implements Serializable {

    private int min;
    private int max;
//    private int initialCount;
//    private String setType;


    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

//    public int getInitialCount() {
//        return initialCount;
//    }
//
//    public void setInitialCount(int initialCount) {
//        this.initialCount = initialCount;
//    }
//
//    public String getSetType() {
//        return setType;
//    }
//
//    public void setSetType(String setType) {
//        this.setType = setType;
//    }


    public static class Builder {
        private int min;
        private int max;
//        private int initialCount;
//        private String setType;

        public Replicas.Builder withMin(int min) {
            this.min = min;
            return this;
        }

        public Replicas.Builder withMax(int max) {
            this.max = max;
            return this;
        }

//        public Replicas.Builder withInitialCount(int initialCount) {
//            this.initialCount = initialCount;
//            return this;
//        }
//
//        public Replicas.Builder withSetType(String setType) {
//            this.setType = setType;
//            return this;
//        }

        public Replicas build() {
            Replicas replicas = new Replicas();

            replicas.setMin(this.min);
            replicas.setMax(this.max);
//            replicas.setInitialCount(this.initialCount);
//            replicas.setSetType(this.setType);

            return replicas;
        }
    }
}
