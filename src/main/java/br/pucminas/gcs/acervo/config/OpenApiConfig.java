package br.pucminas.gcs.acervo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentação interativa da API (Swagger UI em /swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI acervoOpenApi(ObjectProvider<BuildProperties> buildProperties) {
        String versao = buildProperties.stream().map(BuildProperties::getVersion).findFirst().orElse("dev");
        return new OpenAPI().info(new Info()
                .title("Acervo - API de Gestão de Biblioteca")
                .description("API REST para cadastro de livros e leitores e controle de empréstimos.")
                .version(versao)
                .license(new License().name("MIT")));
    }
}
