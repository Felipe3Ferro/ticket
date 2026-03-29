package br.ferro.ticket.catalog.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("TicketHigh — Catalog API")
                .description(
                    """
                    Microsserviço de catálogo de eventos e ingressos da plataforma TicketHigh.

                    Responsável por gerenciar o ciclo de vida de **Eventos** e seus **Tipos de Ingresso**,
                    publicando eventos de domínio no Kafka após cada mutação.

                    ### Recursos disponíveis
                    - **Eventos** — criação, listagem e consulta de shows, festivais e jogos
                    - **Tipos de Ingresso** — setores com preço e quantidade disponível por evento
                    """)
                .version("v1.0.0")
                .contact(new Contact().name("Felipe Ferro").email("felipeferro189@gmail.com"))
                .license(
                    new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")))
        .servers(
            List.of(
                new Server()
                    .url("http://localhost:8081")
                    .description("Ambiente de desenvolvimento local")));
  }
}
