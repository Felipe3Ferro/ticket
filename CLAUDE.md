# CLAUDE.md — TicketHigh: `ticket-catalog` Microsserviço

## 1. Contexto de Negócio

Este serviço é o **microsserviço de catálogo** da plataforma **TicketHigh**, uma plataforma
de venda de ingressos projetada para suportar altíssima concorrência. Toda decisão de código
deve priorizar consistência arquitetural, performance sob carga e rastreabilidade de eventos.

Entidades centrais:

- `Event` — representa um show, festival ou jogo. Possui capacidade máxima, data e local.
- `Batch` / `Ticket` — lotes de ingressos vinculados a um evento, com preço e quantidade.

> ⚠️ **Foco crítico:** evite queries N+1 a todo custo. Use fetch joins onde necessário.
> A alta concorrência é o cenário de produção — otimize sempre para leitura em escala.

---

## 2. Stack Tecnológica

| Tecnologia                      | Versão / Detalhe                                               |
|---------------------------------|----------------------------------------------------------------|
| Java                            | 21 — use records, sealed classes e pattern matching            |
| Spring Boot                     | 4.0.3 — atenção às novas especificações de autoconfiguração    |
| Banco de Dados                  | PostgreSQL                                                     |
| Cache                           | Redis (`@Cacheable`, `@CacheEvict`)                            |
| Mensageria                      | Apache Kafka (`KafkaTemplate` + `JacksonJsonSerializer`)       |
| Observabilidade                 | Micrometer + Prometheus + Grafana + Spring Actuator            |
| Migrations                      | Flyway (`src/main/resources/db/migration`)                     |
| Mapeamento                      | MapStruct + Lombok                                             |
| Build                           | Gradle                                                         |

---

## 3. Estrutura de Pacotes (Canônica)
```
com.tickethigh.catalog
├── app/
│   ├── controller/       # REST Controllers (@RestController)
│   ├── dto/              # Records: XxxRequestDTO, XxxResponseDTO
│   ├── exception/        # GlobalExceptionHandler + ErroResponse record
│   ├── mapper/           # Interfaces MapStruct (XxxMapper)
│   └── service/          # Application Services (XxxService)
├── domain/
│   ├── entity/           # Entidades JPA puras (sem lógica de infraestrutura)
│   └── repository/       # Interfaces Spring Data JPA
└── infra/
    ├── messaging/        # Producers e Consumers Kafka
    └── config/           # Beans de configuração (Redis, Kafka, Micrometer, etc.)
```

---

## 4. Regras Inegociáveis

### 4.1 Blindagem de API
- **NUNCA** retorne ou receba Entidades JPA nos Controllers
- Use `record` para todos os DTOs: `XxxRequestDTO`, `XxxResponseDTO`
- Todo mapeamento passa pelo MapStruct — proibido mapeamento manual em service/controller

### 4.2 Validação e Tratamento de Erros
- Todas as entradas no Controller devem usar `jakarta.validation.constraints`
- Erros de validação são tratados **exclusivamente** no `GlobalExceptionHandler`
- O retorno de erro deve sempre usar o record `ErroResponse`:
```java
record ErroResponse(LocalDateTime timestamp, Integer status, String message, List<String> errors) {}
```

### 4.3 Cache com Redis
- Leituras pesadas ou frequentes: `@Cacheable(value = "xxx", key = "#id")`
- Após toda mutação (create/update/delete): `@CacheEvict` no método do service
- Nomes de cache devem ser constantes definidas em `CacheConstants`

### 4.4 Mensageria — Event-Driven
- Criar ou alterar recursos-chave **obrigatoriamente** publica no Kafka
- Use `KafkaTemplate.send(...).whenComplete((result, ex) -> { ... })` para confirmar entrega
- Tópicos devem ser constantes definidas em `KafkaTopics` — nunca strings literais
- Payload das mensagens: DTOs serializados como JSON — **nunca** entidades JPA
- Serializer obrigatório: `JacksonJsonSerializer` (`JsonSerializer` está deprecated desde Kafka 4.0)

### 4.5 Banco de Dados
- `ddl-auto: validate` — o Hibernate **não altera** o esquema, apenas valida
- Toda alteração estrutural: script SQL em `db/migration/V{n}__{descricao}.sql`
- Convenção de nomenclatura SQL: `snake_case` para tabelas e colunas

### 4.6 Observabilidade
- Todos os endpoints expostos via Spring Actuator
- Prometheus configurado para scrape das métricas em `/actuator/prometheus`
- Métricas customizadas de negócio via `MeterRegistry` do Micrometer
- Dashboards de visualização no Grafana

---

## 5. Convenções de Nomenclatura

| Artefato              | Padrão                                | Exemplo                              |
|-----------------------|---------------------------------------|--------------------------------------|
| Controller            | `{Recurso}Controller`                 | `EventoController`                   |
| Service               | `{Recurso}Service`                    | `EventoService`                      |
| Mapper (MapStruct)    | `{Recurso}Mapper`                     | `EventoMapper`                       |
| Request DTO           | `{Acao}{Recurso}RequestDTO`           | `CreateEventoRequestDTO`             |
| Response DTO          | `{Recurso}ResponseDTO`                | `EventoResponseDTO`                  |
| Entidade JPA          | `{Recurso}` (sem sufixo)              | `Evento`                             |
| Migration Flyway      | `V{n}__{acao_recurso}.sql`            | `V3__add_capacity_to_evento.sql`     |
| Tópico Kafka          | `tickethigh.catalog.{recurso}.{acao}` | `tickethigh.catalog.evento.criado`   |
| Constantes de Cache   | `UPPER_SNAKE_CASE` em `CacheConstants`| `CACHE_EVENTOS`                      |
| Tabelas SQL           | `snake_case` no plural                | `eventos`, `lotes_ingresso`          |

---

## 6. Fluxo Padrão de uma Feature
```
Request HTTP
    → Controller (valida DTO com jakarta.validation)
    → Service (lógica de negócio, cache, publica no Kafka)
    → Repository (persistência via Spring Data JPA)
    → Kafka (publicado via whenComplete após persistência confirmada)
    → Mapper (Entity → ResponseDTO via MapStruct)
    → Response HTTP
```

> Kafka é publicado **após** persistência confirmada, dentro do service,
> usando `whenComplete` para tratar sucesso e falha de forma assíncrona.

---

## 7. Estratégia de Testes (⚠️ ATENÇÃO ESPECIAL SPRING BOOT 4)

### 7.1 Testes de Controller — `@WebMvcTest`
```java
// ✅ Import correto no Spring Boot 4
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

// ✅ Obrigatório: MockitoBean (Spring Boot 4)
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// ✅ Obrigatório: Jackson 3
import tools.jackson.databind.ObjectMapper;
```

**Proibições absolutas em testes de controller:**
- ❌ `import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest` — pacote do Spring Boot 3
- ❌ `@MockBean` — deprecated, substituído por `@MockitoBean`
- ❌ `import com.fasterxml.jackson` — Jackson 2, substituído por `tools.jackson`

**Padrão obrigatório de nomenclatura de testes:**
```java
@Test
@DisplayName("Deve [comportamento esperado] quando [condição]")
void nomeDoMetodo_shouldReturnX_whenY() { ... }
```

**Estrutura obrigatória — Arrange / Act / Assert:**
```java
// Arrange — prepara dados e mocks
// Act — executa a ação
// Assert — verifica o resultado
```

**Cobertura mínima obrigatória por endpoint:**
- ✅ Cenário feliz (200/201)
- ✅ Validação inválida (400) — validar campos do `ErroResponse`
- ✅ Recurso não encontrado (404)

### 7.2 Testes de Service — Unitários Puros
```java
@ExtendWith(MockitoExtension.class) // sem Spring
class EventoServiceTest {
    @Mock private EventoRepository eventoRepository;
    @Mock private EventoMapper eventoMapper;
    @Mock private EventoProducer eventoProducer;
    @InjectMocks private EventoService eventoService;
}
```

- Zero Spring context — apenas Mockito puro
- Verificar obrigatoriamente: `verify(repository).save(...)` e `verify(producer).enviar(...)`

### 7.3 Pirâmide de Testes
```
        /\
       /E2E\               ← @SpringBootTest (poucos)
      /------\
     /Integração\          ← @WebMvcTest, @DataJpaTest (médio volume)
    /------------\
   /  Unitários   \        ← JUnit + Mockito puro (maioria)
  /----------------\
```

---

## 8. Anti-Patterns Banidos

| Anti-Pattern                          | Motivo                                              | Alternativa                        |
|---------------------------------------|-----------------------------------------------------|------------------------------------|
| `@Autowired` em campos                | Dificulta testes e viola imutabilidade              | Injeção por construtor             |
| Lógica de negócio no Controller       | Viola separação de responsabilidades                | Mover para o Service               |
| `@JsonIgnore` em entidade JPA         | Expõe entidade na camada HTTP                       | Usar DTOs com MapStruct            |
| `JsonSerializer` (Kafka)              | Deprecated desde Spring Kafka 4.0                   | Usar `JacksonJsonSerializer`       |
| `@MockBean` em testes                 | Deprecated no Spring Boot 4                         | Usar `@MockitoBean`                |
| `com.fasterxml.jackson` em testes     | Jackson 2, incompatível com Spring Boot 4           | Usar `tools.jackson.databind`      |
| Strings literais para tópicos/caches  | Magic strings espalhadas, difícil manutenção        | Usar `KafkaTopics` e `CacheConstants` |
| `Optional.get()` sem verificação      | Causa `NoSuchElementException` em runtime           | Usar `orElseThrow` com exceção de domínio |
| `System.out.println`                  | Sem rastreabilidade em produção                     | Usar `@Slf4j` com `log.info/warn/error` |
| Queries N+1                           | Destrói performance em alta concorrência            | Usar fetch joins ou `@EntityGraph` |

---

## 9. Padronização de Git — Conventional Commits com Gitmoji

### Formato obrigatório
```
<gitmoji> <tipo>(<escopo>): <título em português no imperativo>

<corpo explicando o PORQUÊ da mudança — obrigatório para feat e fix>
```

### Tipos e Gitmojis

| Tipo       | Gitmoji | Quando usar                                      |
|------------|---------|--------------------------------------------------|
| `feat`     | ✨       | Nova funcionalidade                              |
| `fix`      | 🐛       | Correção de bug                                  |
| `refactor` | ♻️       | Refatoração sem mudança de comportamento         |
| `test`     | ✅       | Adição ou correção de testes                     |
| `docs`     | 📝       | Documentação                                     |
| `chore`    | 🔧       | Configuração, build, dependências                |
| `perf`     | ⚡       | Melhoria de performance                          |

### Exemplos corretos
```
✨ feat(evento): adicionar endpoint de criação de evento

Implementa o POST /api/v1/eventos com validação via jakarta.validation,
publicação no Kafka via whenComplete e cache Redis invalidado via @CacheEvict.
O endpoint segue o fluxo padrão definido no CLAUDE.md.
```
```
🐛 fix(kafka): corrigir serialização do payload do evento

O JsonSerializer estava deprecated desde Kafka 4.0 causando warnings no build.
Substituído por JacksonJsonSerializer compatível com Jackson 3.
```
```
✅ test(evento): adicionar testes do controller com cenários de erro

Cobre cenário feliz (201), validação inválida (400) e não encontrado (404).
Usa @MockitoBean e tools.jackson conforme exigido pelo Spring Boot 4.
```