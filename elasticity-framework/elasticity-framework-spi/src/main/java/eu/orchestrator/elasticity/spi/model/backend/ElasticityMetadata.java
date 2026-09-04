package eu.orchestrator.elasticity.spi.model.backend;

import java.util.List;
import java.util.Map;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 9/8/2019
 */
public class ElasticityMetadata {

    private String name;

    // The elasticityMode list is Optional
    private List<String> elasticityMode;

    private String elasticityImpl;
    private String elasticityBackendImpl;

    private Boolean isUserDefined;

    // The extraMetadata are Optional
    // Extra metadataKey, defaultValue
    private Map<String, String> extraMetadata;

    public ElasticityMetadata() {
        this.elasticityMode = null;
        this.extraMetadata = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getElasticityMode() {
        return elasticityMode;
    }

    public void setElasticityMode(List<String> elasticityMode) {
        this.elasticityMode = elasticityMode;
    }

    public String getElasticityImpl() {
        return elasticityImpl;
    }

    public void setElasticityImpl(String elasticityImpl) {
        this.elasticityImpl = elasticityImpl;
    }

    public String getElasticityBackendImpl() {
        return elasticityBackendImpl;
    }

    public void setElasticityBackendImpl(String elasticityBackendImpl) {
        this.elasticityBackendImpl = elasticityBackendImpl;
    }

    public Boolean getUserDefined() {
        return isUserDefined;
    }

    public void setUserDefined(Boolean userDefined) {
        isUserDefined = userDefined;
    }

    public Map<String, String> getExtraMetadata() {
        return extraMetadata;
    }

    public void setExtraMetadata(Map<String, String> extraMetadata) {
        this.extraMetadata = extraMetadata;
    }
}
