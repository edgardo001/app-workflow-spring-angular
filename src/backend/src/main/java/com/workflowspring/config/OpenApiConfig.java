package com.workflowspring.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String apiUrl = System.getenv().getOrDefault("API_URL", "http://localhost:8080");

        return new OpenAPI()
            .info(new Info()
                .title("Workflow Net API")
                .description("Document workflow sequential approval system")
                .version("1.0.0"))
            .addServersItem(new Server().url(apiUrl))
            .addSecurityItem(new SecurityRequirement()
                .addList("bearer-jwt")
                .addList("oauth2"))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"))
                .addSecuritySchemes("oauth2", new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                            .authorizationUrl("/auth/oauth2/authorization/github")
                            .tokenUrl("/auth/oauth2/callback/github")))));
    }
}
