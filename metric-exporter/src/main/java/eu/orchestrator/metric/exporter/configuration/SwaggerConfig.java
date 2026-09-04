package eu.orchestrator.metric.exporter.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String AUTH_TOKEN = "auth_token";

    @Bean
    public GroupedOpenApi backendGroup() {
        return GroupedOpenApi.builder().group("metric_exporter")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addSecurityItem(new SecurityRequirement().addList(AUTH_TOKEN));
                    return operation;
                })
                .packagesToScan("eu.orchestrator.metric.exporter.rest.api")
                .build();
    }

    @Bean
    public OpenAPI maestroBackendOpenAPI() {
        return new OpenAPI().components(new Components()
                .addSecuritySchemes(
                        AUTH_TOKEN,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name(AUTH_TOKEN)
                                .scheme("apiKey")
                )
        ).info(info());
    }

    private Info info() {
        return new Info()
                .title("Maestro API calls - Metric Exporter [metric_exporter] Module")
                .description("Documentation for maestro metric exporter [metric_exporter] module")
                .version("v1.6.0");
    }

}
