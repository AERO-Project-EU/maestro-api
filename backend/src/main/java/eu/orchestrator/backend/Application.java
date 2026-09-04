package eu.orchestrator.backend;

import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkBackend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.serviceloader.ServiceListFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;

@ComponentScan({
        "eu.orchestrator.backend",
        "eu.orchestrator.metric",
        "eu.orchestrator.repository.service",
        "eu.orchestrator.elasticity"})
@EnableJpaRepositories(basePackages = {"eu.orchestrator.repository.dao"})
// Mongo repositories are enabled only when mongo.enabled=true (see MongoConfig).
@EntityScan(basePackages = {"eu.orchestrator.repository.domain"})
@SpringBootApplication
@EnableScheduling
public class Application {

    final static Logger logger = Logger.getLogger(Application.class.getName());

    @Value("${app.settings.timezone}")
    String timezone;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(timezone));
        logger.info("Starting application in timezone " + timezone + " (" + new Date() + ")");
    }

    @Bean
    public ServiceListFactoryBean elasticityFrameworkAdapters() {
        ServiceListFactoryBean serviceListFactoryBean = new ServiceListFactoryBean();
        serviceListFactoryBean.setServiceType(ElasticityFrameworkBackend.class);
        return serviceListFactoryBean;
    }

}
