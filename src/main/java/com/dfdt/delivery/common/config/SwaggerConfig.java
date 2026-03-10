package com.dfdt.delivery.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    String jwtSchemeName = "AUTHORIZATION";
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

    // SecurityScheme 설정
    Components components = new Components()
            .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                    .name(jwtSchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080").description("Local Server"))
                .addServersItem(new Server().url("http://3.36.165.220").description("Dev Server"))
                .addServersItem(new Server().url("http://43.200.87.63").description("Prod Server"))
                .info(new Info()
                        .title("API 명세서"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}