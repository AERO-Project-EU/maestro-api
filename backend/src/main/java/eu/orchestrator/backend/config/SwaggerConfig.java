package eu.orchestrator.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String AUTH_TOKEN = "auth_token";

    @Value("${project.name}")
    private String PROJECT_NAME;


    @Bean
    public GroupedOpenApi backendGroup() {

        String[] packagesToScan;


        switch (PROJECT_NAME){
            case "datacloud" :

                packagesToScan = new String[]{"eu.orchestrator.backend.rest.projects"};
                break;

            default:

                packagesToScan = new String[]{"eu.orchestrator.backend.rest", "eu.orchestrator.metric.rest", "eu.orchestrator.elasticity.rest"};
        }

        return GroupedOpenApi.builder().group("backend")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addSecurityItem(new SecurityRequirement().addList(AUTH_TOKEN));
                    return operation;
                })
                .packagesToScan(packagesToScan)
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
                .title(PROJECT_NAME + " API calls - Backend [backend] Module")
                .description("Documentation for " + PROJECT_NAME + " backend [backend] module")
                .version("v1.6.0");
    }

}
