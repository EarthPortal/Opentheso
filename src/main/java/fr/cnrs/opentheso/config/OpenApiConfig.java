package fr.cnrs.opentheso.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Opentheso API", version = "2.0"),
        tags = {
                @Tag(name = "Api v2", description = "Nouvelle version des endpoints"),
                @Tag(name = "Api v1", description = "Ancienne version des endpoints")
        },
        security = @SecurityRequirement(name = "apiKey")
)
@SecurityScheme(
        name = "apiKey",
        description = "Clé d'API v1",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "API-KEY"
)
public class OpenApiConfig {
    // ---------------------------
    // Bean OpenAPI pour configuration avancée
    // ---------------------------
    @Bean
    public io.swagger.v3.oas.models.OpenAPI customOpenAPI() {
        return new io.swagger.v3.oas.models.OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Opentheso API avancée")
                        .version("2.0")
                        .description("API sécurisée avec gestion avancée de la clé API"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("ApiKeyAuth",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .name("X-API-KEY") // header alternatif
                                        .description("Clé d'API V2")
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY)
                                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                        )
                )
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList("ApiKeyAuth"));
    }
}
