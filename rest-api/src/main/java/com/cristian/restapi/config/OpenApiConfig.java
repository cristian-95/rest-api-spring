package com.cristian.restapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("REST API's implementada utilizando Java 21, Spring Boot 3.2.0 e Docker")
                        .version("v1")
                        .description("Uma API simples para fixar conhecimentos de Spring Boot, Docker, Testes, Autenticação e outros conceitos")
                        .termsOfService("TODO: implementar termos de serviço")
                        .license(
                                new License()
                                        .name("Apache 2.0")
                                        .url("https://www.apache.org/licenses/LICENSE-2.0")
                        )
                );
    }
}
