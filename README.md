# 🎫 ticket-catalog

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-4.0-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

Microsserviço de catálogo da plataforma **TicketHigh** — responsável por gerenciar eventos
e lotes de ingressos com foco em altíssima concorrência.

## 🏗️ Arquitetura

Este serviço faz parte de uma plataforma de microsserviços baseada em
**Event-Driven Architecture (EDA)**, onde cada operação relevante publica um evento
no Apache Kafka para notificar os demais serviços da plataforma.
```
Request → Controller → Service → Repository → Kafka → Response
```

## 🚀 Stack Tecnológica

| Tecnologia        | Descrição                              |
|-------------------|----------------------------------------|
| Java 21           | Records, sealed classes, pattern matching |
| Spring Boot 4     | Framework principal                    |
| PostgreSQL        | Banco de dados relacional              |
| Redis             | Cache de alta performance              |
| Apache Kafka      | Mensageria assíncrona                  |
| Flyway            | Migrations de banco de dados           |
| MapStruct         | Mapeamento entre entidades e DTOs      |
| Gradle            | Build tool                             |

## 📦 Estrutura do Projeto
```
src/main/java/com/tickethigh/catalog/
├── app/
│   ├── controller/     # REST Controllers
│   ├── dto/            # Records de entrada e saída
│   ├── exception/      # GlobalExceptionHandler + ErroResponse
│   ├── mapper/         # Interfaces MapStruct
│   └── service/        # Application Services
├── domain/
│   ├── entity/         # Entidades JPA
│   └── repository/     # Interfaces Spring Data JPA
└── infra/
    ├── messaging/      # Producers e Consumers Kafka
    └── config/         # Configurações de infraestrutura
```

## ⚙️ Pré-requisitos

- Java 21
- Docker e Docker Compose

## 🐳 Subindo a infraestrutura
```bash
docker compose up -d
```

Isso sobe os seguintes serviços:

| Serviço    | Porta |
|------------|-------|
| PostgreSQL | 5432  |
| Redis      | 6379  |
| Kafka      | 9092  |

## ▶️ Rodando o projeto
```bash
./gradlew bootRun
```

## 📖 Documentação da API

Com o projeto rodando, acesse o Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

## 🧪 Testes
```bash
# Roda todos os testes
./gradlew test

# Verifica formatação do código
./gradlew spotlessCheck

# Corrige formatação do código
./gradlew spotlessApply
```

## 🗄️ Migrations

As migrations ficam em `src/main/resources/db/migration` seguindo o padrão:
```
V{n}__{descricao_da_alteracao}.sql
```

O Hibernate está configurado com `ddl-auto: validate` — toda alteração estrutural
no banco **obrigatoriamente** passa por um script Flyway.

## 📬 Tópicos Kafka

| Tópico                              | Quando é publicado        |
|-------------------------------------|---------------------------|
| `tickethigh.catalog.evento.criado`  | Ao criar um novo evento   |

## 🤝 Contribuindo

Este projeto segue o padrão **Conventional Commits com Gitmoji**:
```
✨ feat(evento): adicionar endpoint de criação de evento

Implementa o POST /api/v1/eventos com validação, cache Redis e publicação no Kafka.
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