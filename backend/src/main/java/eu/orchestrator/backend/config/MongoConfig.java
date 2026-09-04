package eu.orchestrator.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB support is opt-in. It is activated only when {@code mongo.enabled=true}
 * (see application-mongo.yml / application-production.yml). When disabled, Spring Boot's
 * Mongo auto-configuration is excluded (see spring.autoconfigure.exclude in application.yml),
 * so the application boots without a MongoDB instance and without any connection attempts.
 * The {@code OrchestratorApplicationInstanceDAO} injections in the OSS/slice services are
 * optional and guarded, so those code paths simply skip Mongo persistence while disabled.
 */
@Configuration
@ConditionalOnProperty(name = "mongo.enabled", havingValue = "true")
@EnableMongoRepositories(basePackages = "eu.orchestrator.document.repository.dao")
public class MongoConfig {
}
