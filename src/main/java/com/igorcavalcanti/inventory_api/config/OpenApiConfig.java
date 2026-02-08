package com.igorcavalcanti.inventory_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inventory API",
                version = "v1",
                description = "API para produtos e movimentações de estoque."
        )
)
public class OpenApiConfig {}
