package com.br.strutz.order_book.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vibranium Order Book API")
                        .description("Plataforma de negociação de Vibranium — Order Book Engine")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Strutz")
                                .email("contato@strutz.com.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local"),
                        new Server()
                                .url("https://api.vibranium.com.br")
                                .description("Produção")));
    }
}
