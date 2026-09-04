package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.List;

public class KeyOperatorPair implements Serializable {

    private String key;
    private String operator;
    private List<String> values;

    public KeyOperatorPair() {
    }

    public KeyOperatorPair(Builder builder) {
        this.key = builder.key;
        this.operator = builder.operator;
        this.values = builder.values;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public static class Builder
    {
        private String key;
        private String operator;
        private List<String> values;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public Builder values(List<String> values) {
            this.values = values;
            return this;
        }

        public KeyOperatorPair build() {
            return new KeyOperatorPair(this);
        }

    }

}
