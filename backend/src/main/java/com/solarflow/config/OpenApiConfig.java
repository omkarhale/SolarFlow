package com.solarflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  OpenAPI solarFlowOpenApi() {
    return new OpenAPI().info(new Info()
        .title("SolarFlow API")
        .version("v1")
        .description("API for residential rooftop solar installation companies."));
  }
}
