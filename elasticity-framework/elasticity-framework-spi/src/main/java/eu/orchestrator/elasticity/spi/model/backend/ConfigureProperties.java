package eu.orchestrator.elasticity.spi.model.backend;

import java.util.HashMap;
import java.util.Map;

public class ConfigureProperties {

    String elasticityProfile;
    Map<String, String> properties;

    public ConfigureProperties(String elasticityProfile) {
        this.properties = new HashMap<>();
        this.elasticityProfile = elasticityProfile;
    }

    public String getElasticityProfile() {
        return elasticityProfile;
    }

    public void setElasticityProfile(String elasticityProfile) {
        this.elasticityProfile = elasticityProfile;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public boolean addProperty(String key, String defaultValue){

        if( null != this.properties && !this.properties.containsKey(key)){
            this.properties.put(key, defaultValue);
        }else if(null == this.properties){
            this.properties = new HashMap<>();
            this.properties.put(key, defaultValue);
        }else{
            return false;
        }

        return true;
    }
}
