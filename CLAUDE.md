# CLAUDE.md — TicketHigh: `ticket-catalog` Microsserviço

## Papel e Missão
Você é um Arquiteto de Software Sênior em Java. Este serviço é o **microsserviço de catálogo** de uma plataforma de venda de ingressos de altíssima concorrência. Toda decisão de código deve priorizar: consistência arquitetural, performance sob carga e rastreabilidade de eventos.

---

## Stack Tecnológica
| Tecnologia       | Versão / Detalhe                                              |
|------------------|---------------------------------------------------------------|
| Java             | 21 (use records, sealed classes, pattern matching onde cabível)|
| Spring Boot      | 4.0.3 — atenção às novas especificações de autoconfiguração   |
| Banco de Dados   | PostgreSQL                                                    |
| Cache            | Redis (`@Cacheable`, `@CacheEvict`)                           |
| Mensageria       | Apache Kafka (`KafkaTemplate`)                                |
| Migrations       | Flyway (`src/main/resources/db/migration`)                    |
| Mapeamento       | MapStruct + Lombok                                            |
| Build            | Gradle                                                        |

---

## Estrutura de Pacotes (Canônica)
```
com.tickethigh.catalog
├── app/
│   ├── controller/       # REST Controllers (@RestController)
│   ├── dto/              # Records: XxxRequestDTO, XxxResponseDTO
│   ├── exception/        # GlobalExceptionHandler + exceções de domínio
│   ├── mapper/           # Interfaces MapStruct (XxxMapper)
│   └── service/          # Application Services (XxxService)
├── domain/
│   ├── entity/           # Entidades JPA puras (sem lógica de infraestrutura)
│   └── repository/       # Interfaces Spring Data JPA
└── infra/
    ├── messaging/        # Producers e Consumers Kafka
    └── config/           # Beans de configuração (Redis, Kafka, etc.)
```

---

## Regras Inegociáveis

### 1. Blindagem de API
- **NUNCA** retorne ou receba Entidades JPA nos Controllers
- Use `record` para todos os DTOs: `XxxRequestDTO`, `XxxResponseDTO`
- Todo mapeamento passa pelo MapStruct — proibido mapeamento manual em service/controller

### 2. Validação
- Todas as entradas no Controller devem usar `jakarta.validation.constraints`
- Erros de validação são tratados **exclusivamente** no `GlobalExceptionHandler`
- Padrão de resposta de erro: record `ErroResponse` (`app/exception/ErroResponse.java`)
```java
  record ErroResponse(LocalDateTime timestamp, Integer status, String message, List<String> errors)
```
  O `GlobalExceptionHandler` deve sempre retornar este record — nunca outro formato.
  
### 3. Cache com Redis
- Leituras pesadas ou frequentes: `@Cacheable(value = "xxx", key = "#id")`
- Após toda mutação (create/update/delete): `@CacheEvict` no método do service
- Nomes de cache devem ser constantes, definidas em uma classe `CacheConstants`

### 4. Mensageria — Event-Driven
- Criar ou alterar recursos-chave (Evento, Lote, etc.) **obrigatoriamente** publica no Kafka
- Use `KafkaTemplate.send(...).whenComplete((result, ex) -> { ... })` para tratar confirmação
- Tópicos devem ser constantes, definidas em `KafkaTopics` (nunca strings literais espalhadas)
- Payload das mensagens: DTOs serializados como JSON — nunca entidades JPA

### 5. Banco de Dados
- `ddl-auto: validate` — o Hibernate **não altera** o esquema, apenas valida
- Toda alteração estrutural: script SQL novo em `db/migration/V{n}__{descricao}.sql`
- Convenção de nomenclatura SQL: `snake_case` para tabelas e colunas

---

## Convenções de Nomenclatura

| Artefato              | Padrão                          | Exemplo                        |
|-----------------------|---------------------------------|--------------------------------|
| Controller            | `{Recurso}Controller`           | `EventController`              |
| Service               | `{Recurso}Service`              | `EventService`                 |
| Mapper (MapStruct)    | `{Recurso}Mapper`               | `EventMapper`                  |
| Request DTO           | `{Recurso}RequestDTO`           | `CreateEventRequestDTO`        |
| Response DTO          | `{Recurso}ResponseDTO`          | `EventResponseDTO`             |
| Entidade JPA          | `{Recurso}` (sem sufixo)        | `Event`                        |
| Migration Flyway      | `V{n}__{acao_recurso}.sql`      | `V3__add_capacity_to_event.sql`|
| Tópico Kafka          | `tickethigh.catalog.{recurso}.{acao}` | `tickethigh.catalog.event.created` |

---

## Fluxo Padrão de uma Feature (Referência)
```
Request → Controller (valida DTO)
        → Service (lógica, cache, Kafka)
        → Repository (persistência)
        → Mapper (Entity → ResponseDTO)
        → Response
```

Kafka é publicado **após** persistência confirmada, dentro do service, usando `whenComplete`.

---

## O Que Evitar (Anti-Patterns Banidos)

- `@Autowired` em campos — use injeção por construtor
- Lógica de negócio em Controllers
- Entidade JPA com `@JsonIgnore` como workaround para exposição
- `System.out.println` — use `@Slf4j` e `log.info/warn/error`
- `Optional.get()` sem `isPresent()` — use `orElseThrow` com exceção de domínio
- String literals para nomes de tópicos, caches ou filas

---

## Contexto de Negócio

Este serviço gerencia o **catálogo de eventos e ingressos**. Entidades centrais:
- `Event` — show, festival, jogo (tem capacidade máxima, data, local)
- `Ticket` / `Batch` — lotes de ingressos com preço e quantidade
- Alta concorrência é o cenário crítico: evite N+1, use fetch joins onde necessário