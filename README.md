# ticket-catalog

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-8-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-4.0-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

Microsserviço de catálogo da plataforma **TicketHigh** — gerencia eventos e tipos de ingresso com foco em alta concorrência, arquitetura event-driven e cache de alta performance.

---

## Arquitetura

```
Request → Controller → Service → Repository → Kafka → Response
                                     ↕
                                   Redis
```

Cada operação de escrita publica um evento no Apache Kafka após a persistência confirmada. Leituras frequentes são servidas via cache Redis para minimizar carga no banco.

---

## Stack

| Tecnologia      | Versão   | Responsabilidade                          |
|-----------------|----------|-------------------------------------------|
| Java            | 21       | Records, sealed classes, pattern matching |
| Spring Boot     | 4.0      | Framework principal                       |
| PostgreSQL      | 15       | Persistência relacional                   |
| Redis           | 8        | Cache de leituras                         |
| Apache Kafka    | 4.0      | Mensageria assíncrona (event-driven)      |
| Flyway          | —        | Migrations de banco de dados              |
| MapStruct       | 1.6      | Mapeamento Entity ↔ DTO                   |
| Gradle          | 8        | Build                                     |

---

## Estrutura de pacotes

```
src/main/java/br/ferro/ticket/catalog/
├── app/
│   ├── controller/     # REST Controllers — apenas roteamento HTTP
│   ├── dto/            # Records de entrada e saída (XxxRequestDTO, XxxResponseDTO)
│   ├── exception/      # GlobalExceptionHandler + ErroResponse
│   ├── mapper/         # Interfaces MapStruct
│   └── service/        # Lógica de negócio, cache e publicação Kafka
├── domain/
│   ├── entity/         # Entidades JPA puras
│   └── repository/     # Interfaces Spring Data JPA
└── infra/
    ├── config/         # Redis, CacheConstants, OpenAPI
    ├── kafka/          # KafkaProducerConfig
    └── messaging/      # Producers Kafka + KafkaTopics
```

---

## Pré-requisitos

- Java 21
- Docker e Docker Compose

---

## Subindo a infraestrutura

```bash
docker compose up -d
```

| Serviço    | Porta | Descrição              |
|------------|-------|------------------------|
| PostgreSQL | 5432  | Banco de dados         |
| Redis      | 6379  | Cache                  |
| Kafka      | 9092  | Mensageria             |
| Prometheus | 9090  | Coleta de métricas     |
| Grafana    | 3000  | Dashboard de métricas  |

---

## Rodando a aplicação

```bash
./gradlew bootRun
```

---

## API

Documentação interativa disponível via Swagger UI com a aplicação rodando:

```
http://localhost:8081/swagger-ui.html
```

### Eventos — `GET | POST | DELETE`

| Método   | Endpoint                  | Descrição              | Status     |
|----------|---------------------------|------------------------|------------|
| `POST`   | `/api/v1/eventos`         | Cadastrar evento       | `201`       |
| `GET`    | `/api/v1/eventos`         | Listar eventos         | `200`       |
| `GET`    | `/api/v1/eventos/{id}`    | Buscar evento por ID   | `200 / 404` |
| `DELETE` | `/api/v1/eventos/{id}`    | Remover evento         | `204 / 404` |

### Tipos de Ingresso — `GET | POST | DELETE`

| Método   | Endpoint                                        | Descrição                       | Status      |
|----------|-------------------------------------------------|---------------------------------|-------------|
| `POST`   | `/api/v1/eventos/{id}/tipos-ingresso`           | Cadastrar tipo de ingresso      | `201`       |
| `GET`    | `/api/v1/eventos/{id}/tipos-ingresso`           | Listar tipos de um evento       | `200 / 404` |
| `GET`    | `/api/v1/eventos/{id}/tipos-ingresso/{tipoId}`  | Buscar tipo por ID              | `200 / 404` |
| `DELETE` | `/api/v1/eventos/{id}/tipos-ingresso/{tipoId}`  | Remover tipo de ingresso        | `204 / 404` |

---

## Tópicos Kafka

| Tópico                                   | Publicado quando               |
|------------------------------------------|--------------------------------|
| `tickethigh.catalog.evento.criado`       | Ao cadastrar um evento         |
| `tickethigh.catalog.evento.removido`     | Ao remover um evento           |
| `tickethigh.catalog.tipo-ingresso.criado`| Ao cadastrar um tipo de ingresso |

---

## Testes

```bash
# Executa todos os testes
./gradlew test

# Verifica formatação
./gradlew spotlessCheck

# Corrige formatação
./gradlew spotlessApply
```

---

## Migrations

Scripts em `src/main/resources/db/migration` seguindo o padrão Flyway:

```
V{n}__{descricao_da_alteracao}.sql
```

O Hibernate está configurado com `ddl-auto: validate` — toda alteração estrutural no banco obrigatoriamente passa por uma migration.

---

## Convenção de commits

```
<gitmoji> <tipo>(<escopo>): <título em português no imperativo>
```

| Tipo       | Gitmoji | Quando usar                              |
|------------|---------|------------------------------------------|
| `feat`     | ✨       | Nova funcionalidade                      |
| `fix`      | 🐛       | Correção de bug                          |
| `refactor` | ♻️       | Refatoração sem mudança de comportamento |
| `test`     | ✅       | Adição ou correção de testes             |
| `docs`     | 📝       | Documentação                             |
| `chore`    | 🔧       | Configuração, build, dependências        |
| `perf`     | ⚡       | Melhoria de performance                  |
