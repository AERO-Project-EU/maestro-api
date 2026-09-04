package eu.orchestrator.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 7/9/23
 */
@Configuration
public class K8sConfig {

    @Bean(name = "k8sLabels")
    @ConfigurationProperties( prefix = "kubernetes.labels" )
    public List<String> k8sLabels(){
        return new ArrayList<String>();
    }

}
