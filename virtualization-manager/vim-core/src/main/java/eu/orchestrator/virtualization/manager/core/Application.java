package eu.orchestrator.virtualization.manager.core;

import eu.orchestrator.spi.adapter.ProviderAdapter;
import org.springframework.beans.factory.serviceloader.ServiceListFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Konstantinos Theodosiou
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }

    // Load provider adapters
    @Bean
    public ServiceListFactoryBean providerAdapters() {
        ServiceListFactoryBean serviceListFactoryBean = new ServiceListFactoryBean();
        serviceListFactoryBean.setServiceType(ProviderAdapter.class);
        return serviceListFactoryBean;
    }

}
