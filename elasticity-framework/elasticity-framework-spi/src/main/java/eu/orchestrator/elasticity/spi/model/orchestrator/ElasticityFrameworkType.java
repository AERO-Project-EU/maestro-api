package eu.orchestrator.elasticity.spi.model.orchestrator;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 26/7/2019
 */
public enum ElasticityFrameworkType {
    HORIZONTAL("HORIZONTAL"),
    VERTICAL("VERTICAL"),
    DIAGONAL("DIAGONAL"),
    LAMBDA_FUNCTION("LAMBDA_FUNCTION"),
    NONE("NONE");

    private String elasticityFrameworkType;

    ElasticityFrameworkType(String elasticityFrameworkType) {
        this.elasticityFrameworkType = elasticityFrameworkType;
    }

    public String getElasticityFrameworkType() {
        return elasticityFrameworkType;
    }

    public void setElasticityFrameworkType(String elasticityFrameworkType) {
        this.elasticityFrameworkType = elasticityFrameworkType;
    }
}
