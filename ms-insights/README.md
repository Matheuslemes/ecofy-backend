# ms-insights — EcoFy

> Microsserviço responsável pela geração de insights financeiros, consolidação de métricas, gerenciamento de metas e composição do dashboard do EcoFy.

> 🇬🇧 **English summary first.**
> 🇧🇷 **Documentação técnica completa em Português abaixo.**

---

## 🇬🇧 English Summary

### Responsibility

The `ms-insights` service owns the financial insights domain within EcoFy.

Its main responsibilities are:

* Generating financial insights by user and period.
* Consolidating financial metrics as typed snapshots.
* Composing the dashboard response.
* Managing financial goals.
* Publishing insight creation events.
* Consuming categorized transaction and budget alert events.
* Handling unavailable external data explicitly.

### Technology stack

* Java 25
* Spring Boot 4
* Spring Security
* OAuth2 Resource Server
* PostgreSQL
* Flyway
* Apache Kafka
* Maven
* JUnit 5
* Mockito

### Architecture

The service follows Hexagonal Architecture:

```text
core/domain
core/application
core/port/in
core/port/out
adapters/in
adapters/out
config
```

### Service configuration

| Property     | Value        |
| ------------ | ------------ |
| Port         | `8085`       |
| Context path | `/insights`  |
| Database     | PostgreSQL   |
| Messaging    | Apache Kafka |

Local base URL:

```text
http://localhost:8085/insights
```

### Main endpoints

| Method | Resource path                            | Protection          | Description                         |
| ------ | ---------------------------------------- | ------------------- | ----------------------------------- |
| `GET`  | `/api/insights/v1/dashboard/{userId}`    | JWT in production   | Returns insights, metrics and goals |
| `POST` | `/api/insights/v1/generate`              | JWT in production   | Generates insights idempotently     |
| `POST` | `/api/insights/v1/goals`                 | JWT in production   | Creates a financial goal            |
| `PUT`  | `/api/insights/v1/goals/{goalId}`        | JWT in production   | Updates a financial goal            |
| `GET`  | `/api/insights/v1/goals/{goalId}`        | JWT in production   | Returns a financial goal            |
| `GET`  | `/api/insights/v1/goals?userId={userId}` | JWT in production   | Lists goals by user                 |
| `GET`  | `/actuator/health`                       | Public              | Reports service health              |
| `GET`  | `/actuator/info`                         | Public              | Reports service information         |
| `GET`  | `/actuator/prometheus`                   | Public/configurable | Exposes Prometheus metrics          |

The context path must be included in full requests:

```text
/insights/api/insights/v1/dashboard/{userId}
```

### Security

Business endpoints are protected according to the active profile.

* Development and test environments may enable `permit-all`.
* Production requires a valid JWT.
* JWT validation uses the JWKS endpoint exposed by `ms-auth`.
* Error responses do not expose stack traces.

Main property:

```text
ecofy.insights.security.permit-all
```

Environment variable:

```text
INS_SECURITY_PERMIT_ALL
```

### Kafka integration

The service consumes:

* `eco.transaction.categorized`
* `eco.budget.alert`

The service publishes:

* `eco.insight.created`

Consumers distinguish unrecoverable payload errors from transient failures:

* Poison payloads are acknowledged and logged.
* Transient failures are retried with backoff.
* A Dead Letter Topic is not implemented yet.

### External integrations

External clients for categorization and budgeting are disabled by default.

When enabled:

* A successful empty response means that no external data exists.
* An integration failure raises `ExternalDataUnavailableException`.
* External unavailability is returned as HTTP `503`.

### Known limitations

* No transactional Outbox.
* No Dead Letter Topic.
* `InsightRebuildService` is currently a placeholder.
* Insight generation keeps a database transaction open during external I/O.
* External HTTP clients are placeholders and disabled by default.
* Integration tests with real PostgreSQL and Kafka are still pending.

---

# 🇧🇷 Documentação técnica

## 1. Visão geral

O `ms-insights` é o microsserviço responsável pela experiência analítica do EcoFy.

O serviço recebe e consolida informações financeiras para:

* Gerar insights por usuário e período.
* Calcular e armazenar snapshots de métricas.
* Compor os dados exibidos no dashboard.
* Criar e gerenciar metas financeiras.
* Reagir a transações categorizadas.
* Reagir a alertas de orçamento.
* Publicar eventos de insights para outros microsserviços.

O serviço não é responsável pela autenticação dos usuários, pela categorização primária das transações ou pelo gerenciamento dos orçamentos. Essas informações são obtidas por eventos Kafka ou integrações HTTP com os respectivos serviços.

---

## 2. Stack tecnológica

| Tecnologia             | Responsabilidade           |
| ---------------------- | -------------------------- |
| Java 25                | Linguagem principal        |
| Spring Boot 4          | Framework da aplicação     |
| Spring Security        | Autenticação e autorização |
| OAuth2 Resource Server | Validação de JWT           |
| PostgreSQL             | Persistência relacional    |
| Flyway                 | Versionamento do schema    |
| Apache Kafka           | Integração assíncrona      |
| Maven Wrapper          | Build e dependências       |
| Micrometer             | Métricas e observabilidade |
| JUnit 5                | Testes automatizados       |
| Mockito                | Testes unitários isolados  |

---

## 3. Arquitetura

O serviço segue Arquitetura Hexagonal, mantendo regras de negócio separadas dos mecanismos de entrada e saída.

Estrutura principal:

```text
src/main/java/br/com/ecofy/ms_insights
├── core
│   ├── domain
│   ├── application
│   └── port
├── adapters
│   ├── in
│   └── out
└── config
```

### Responsabilidade das camadas

| Camada             | Responsabilidade                                    |
| ------------------ | --------------------------------------------------- |
| `core/domain`      | Entidades, value objects, enums e regras de domínio |
| `core/application` | Serviços de aplicação, commands e results           |
| `core/port/in`     | Casos de uso expostos pela aplicação                |
| `core/port/out`    | Contratos de persistência, mensageria e integrações |
| `adapters/in`      | Controllers REST e consumers Kafka                  |
| `adapters/out`     | Persistência, publicação Kafka e clientes HTTP      |
| `config`           | Segurança, Kafka, propriedades e infraestrutura     |

---

## 4. Configuração do serviço

| Configuração | Valor padrão |
| ------------ | ------------ |
| Porta        | `8085`       |
| Context path | `/insights`  |
| Banco        | PostgreSQL   |
| Mensageria   | Apache Kafka |

URL base local:

```text
http://localhost:8085/insights
```

Exemplo de endpoint completo:

```text
http://localhost:8085/insights/api/insights/v1/dashboard/{userId}
```

---

## 5. Responsabilidades funcionais

### 5.1 Geração de insights

O serviço gera insights financeiros com base em:

* Usuário.
* Período.
* Granularidade.
* Dados categorizados.
* Informações de orçamento.
* Métricas financeiras consolidadas.

Cada insight pode conter:

* Tipo.
* Score.
* Título.
* Resumo.
* Payload estruturado.
* Período analisado.
* Data de criação.

### 5.2 Métricas

As métricas são armazenadas como snapshots tipados.

Entre os tipos suportados pelo contrato atual estão:

```text
TOTAL_SPENT
INCOME
SAVINGS_RATE
```

O serviço retorna métricas por meio de objetos `MetricSnapshotResponse`, evitando estruturas genéricas ou listas aninhadas artificialmente.

### 5.3 Dashboard

O dashboard consolida:

* Insights.
* Métricas.
* Metas financeiras.

A composição é retornada por meio de `InsightsBundleResponse`.

### 5.4 Metas financeiras

O serviço oferece operações para:

* Criar uma meta.
* Atualizar uma meta.
* Consultar uma meta.
* Listar metas por usuário.

As metas possuem valor-alvo, moeda e status.

### 5.5 Integração orientada a eventos

O serviço reage a eventos relacionados a:

* Transações categorizadas.
* Alertas de orçamento.

Após a geração de um insight elegível, publica um evento `insight.created`.

---

## 6. Endpoints

Os paths abaixo são relativos ao context path `/insights`.

### 6.1 Dashboard

| Método | Endpoint                              | Descrição                                     |
| ------ | ------------------------------------- | --------------------------------------------- |
| `GET`  | `/api/insights/v1/dashboard/{userId}` | Retorna insights, métricas e metas do usuário |

Exemplo:

```bash
curl -i \
  "http://localhost:8085/insights/api/insights/v1/dashboard/{userId}" \
  -H "Authorization: Bearer {accessToken}"
```

### 6.2 Geração de insights

| Método | Endpoint                    | Descrição                               |
| ------ | --------------------------- | --------------------------------------- |
| `POST` | `/api/insights/v1/generate` | Gera insights para um usuário e período |

Headers:

```http
Authorization: Bearer {accessToken}
Idempotency-Key: {unique-key}
Content-Type: application/json
```

Exemplo:

```bash
curl -i -X POST \
  "http://localhost:8085/insights/api/insights/v1/generate" \
  -H "Authorization: Bearer {accessToken}" \
  -H "Idempotency-Key: generate-user-period-2026-01" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "00000000-0000-0000-0000-000000000000",
    "start": "2026-01-01",
    "end": "2026-01-31",
    "granularity": "MONTHLY"
  }'
```

### 6.3 Metas

| Método | Endpoint                                 | Descrição               |
| ------ | ---------------------------------------- | ----------------------- |
| `POST` | `/api/insights/v1/goals`                 | Cria uma meta           |
| `PUT`  | `/api/insights/v1/goals/{goalId}`        | Atualiza uma meta       |
| `GET`  | `/api/insights/v1/goals/{goalId}`        | Consulta uma meta       |
| `GET`  | `/api/insights/v1/goals?userId={userId}` | Lista metas por usuário |

Exemplo de criação:

```json
{
  "userId": "00000000-0000-0000-0000-000000000000",
  "name": "Reserva de emergência",
  "targetCents": 1000000,
  "currency": "BRL",
  "status": "ACTIVE"
}
```

O valor monetário é enviado em centavos:

```text
1000000 = R$ 10.000,00
```

### 6.4 Actuator

| Método | Endpoint               | Descrição                      |
| ------ | ---------------------- | ------------------------------ |
| `GET`  | `/actuator/health`     | Estado de saúde                |
| `GET`  | `/actuator/info`       | Informações da aplicação       |
| `GET`  | `/actuator/prometheus` | Métricas no formato Prometheus |

---

## 7. Segurança

O serviço atua como OAuth2 Resource Server.

A validação dos tokens JWT utiliza o endpoint JWKS publicado pelo `ms-auth`.

Configuração principal:

```text
JWT_JWKS_URI=http://localhost:8081/auth/.well-known/jwks.json
```

### 7.1 Segurança por profile

| Profile           | `permit-all` | Comportamento da API                |
| ----------------- | -----------: | ----------------------------------- |
| `default` / `dev` |       `true` | JWT opcional nas rotas configuradas |
| `test`            |       `true` | Facilita testes isolados            |
| `prod`            |      `false` | JWT obrigatório                     |

Propriedade:

```text
ecofy.insights.security.permit-all
```

Variável de ambiente:

```text
INS_SECURITY_PERMIT_ALL
```

Em produção:

```env
INS_SECURITY_PERMIT_ALL=false
```

### 7.2 Tratamento seguro de erros

A aplicação não expõe stack traces nas respostas HTTP.

Configuração esperada:

```properties
server.error.include-stacktrace=never
```

Detalhes técnicos das exceções permanecem restritos aos logs internos.

---

## 8. Validações REST

As requisições utilizam Bean Validation e validações complementares no domínio.

### 8.1 `GenerateInsightsRequest`

Campos obrigatórios:

* `userId`
* `start`
* `end`
* `granularity`

A regra de período exige:

```text
start <= end
```

Um período inválido retorna:

```http
400 Bad Request
```

### 8.2 `CreateGoalRequest`

Regras principais:

| Campo         | Regra                                   |
| ------------- | --------------------------------------- |
| `userId`      | Obrigatório                             |
| `name`        | Obrigatório e limitado a 120 caracteres |
| `targetCents` | Deve ser maior que zero                 |
| `currency`    | Código de três letras                   |
| `status`      | Opcional; padrão `ACTIVE`               |

### 8.3 `UpdateGoalRequest`

Os campos são opcionais, mas devem ser válidos quando informados.

Regras:

* `targetCents` deve ser maior que zero.
* `currency` deve possuir três letras.
* `name` deve possuir no máximo 120 caracteres.
* Alterações monetárias devem manter valor e moeda coerentes.

Requisições inválidas retornam um `ApiErrorResponse` padronizado.

---

## 9. Contrato do dashboard

Exemplo de `InsightsBundleResponse`:

```json
{
  "insights": [
    {
      "id": "00000000-0000-0000-0000-000000000001",
      "userId": "00000000-0000-0000-0000-000000000000",
      "type": "SPENDING_BREAKDOWN",
      "score": 80,
      "title": "Distribuição de despesas",
      "summary": "A maior parte das despesas está concentrada em alimentação.",
      "payload": {},
      "createdAt": "2026-01-31T12:00:00Z"
    }
  ],
  "metrics": [
    {
      "id": "00000000-0000-0000-0000-000000000002",
      "userId": "00000000-0000-0000-0000-000000000000",
      "metricType": "TOTAL_SPENT",
      "valueCents": 150000,
      "currency": "BRL",
      "createdAt": "2026-01-31T12:00:00Z"
    }
  ],
  "goals": [
    {
      "id": "00000000-0000-0000-0000-000000000003",
      "userId": "00000000-0000-0000-0000-000000000000",
      "name": "Reserva de emergência",
      "targetCents": 1000000,
      "currency": "BRL",
      "status": "ACTIVE",
      "createdAt": "2026-01-01T12:00:00Z",
      "updatedAt": "2026-01-31T12:00:00Z"
    }
  ]
}
```

O campo `metrics` utiliza:

```text
List<MetricSnapshotResponse>
```

O contrato é tipado e plano, permitindo serialização previsível e integração direta com frontend e OpenAPI.

---

## 10. Idempotência

A geração de insights é protegida por idempotência persistida.

Porta utilizada:

```text
IdempotencyPort.tryAcquire(key, ttlSeconds)
```

O cliente pode enviar:

```http
Idempotency-Key: {unique-key}
```

Quando o header não é informado, o serviço pode construir uma chave com base em:

```text
insights.generate|userId|start|end|granularity
```

Exemplo:

```text
insights.generate|user-123|2026-01-01|2026-01-31|MONTHLY
```

Uma reutilização conflitante resulta em:

```http
409 Conflict
```

O TTL padrão é configurável:

```text
INS_IDEMPOTENCY_TTL_SECONDS=86400
```

O valor padrão corresponde a 24 horas.

---

## 11. Eventos Kafka

### 11.1 Tópicos

| Tópico padrão                 | Direção  | Propriedade                                           |
| ----------------------------- | -------- | ----------------------------------------------------- |
| `eco.transaction.categorized` | Consome  | `ecofy.insights.topics.categorized-transaction-topic` |
| `eco.budget.alert`            | Consome  | `ecofy.insights.topics.budget-alert-topic`            |
| `eco.insight.created`         | Publica  | `ecofy.insights.topics.insight-created-topic`         |
| `eco.report.ready`            | Opcional | `ecofy.insights.topics.report-ready-topic`            |

### 11.2 Evento `insight.created`

O evento é publicado para integração com o `ms-notification`.

Exemplo:

```json
{
  "eventId": "00000000-0000-0000-0000-000000000010",
  "type": "insight.created",
  "userId": "00000000-0000-0000-0000-000000000000",
  "insightId": "00000000-0000-0000-0000-000000000011",
  "insightType": "SPENDING_BREAKDOWN",
  "score": 80,
  "periodStart": "2026-01-01",
  "periodEnd": "2026-01-31",
  "createdAt": "2026-01-31T12:00:00Z",
  "payload": {},
  "metadata": {
    "eventId": "00000000-0000-0000-0000-000000000010",
    "correlationId": null,
    "occurredAt": "2026-01-31T12:00:00Z",
    "source": "ms-insights"
  }
}
```

Campos relevantes para o consumidor:

* `insightType`
* `periodStart`
* `periodEnd`
* `metadata.eventId`
* `userId`
* `insightId`
* `score`

O tópico permanece:

```text
eco.insight.created
```

A publicação registra sucesso ou falha por callback assíncrono.

> A observação assíncrona do resultado não garante atomicidade entre banco e Kafka. Essa garantia depende da futura implementação de Outbox transacional.

---

## 12. Confiabilidade dos consumers

Os consumers diferenciam dois grupos de falhas.

### 12.1 Payload irrecuperável

Exemplos:

* JSON incompatível.
* Campo obrigatório ausente.
* Tipo inválido.
* Evento semanticamente inválido.

Comportamento atual:

1. Registra log em nível `WARN`.
2. Confirma o processamento.
3. Evita retry infinito.

Essas mensagens ainda não são encaminhadas para DLT.

### 12.2 Falha transitória

Exemplos:

* Indisponibilidade do banco.
* Timeout temporário.
* Falha de infraestrutura.
* Dependência externa indisponível.

Comportamento atual:

1. A exceção é relançada.
2. O `DefaultErrorHandler` aplica retry.
3. São realizadas duas novas tentativas.
4. O backoff padrão é de um segundo.

Configuração resumida:

```text
Tentativa inicial + 2 retries
Backoff: 1 segundo
```

---

## 13. Integrações externas

O serviço possui clientes HTTP para:

* `ms-categorization`
* `ms-budgeting`

Esses clientes são placeholders e permanecem desabilitados por padrão.

### 13.1 Client desabilitado

Quando:

```text
enabled=false
```

o serviço não realiza a chamada externa.

A ausência de dados nesse modo é considerada legítima.

### 13.2 Resposta vazia

Uma resposta HTTP bem-sucedida sem itens representa:

```text
Nenhum dado disponível para o período
```

Esse cenário não é tratado como erro.

### 13.3 Falha externa

Quando o client está habilitado e a integração falha, o serviço lança:

```text
ExternalDataUnavailableException
```

Resposta esperada:

```http
503 Service Unavailable
```

Esse comportamento evita que uma indisponibilidade externa seja interpretada incorretamente como ausência de dados financeiros.

---

## 14. Geração e publicação de insights

O score mínimo necessário para publicação é configurável:

```text
INS_MIN_SCORE_TO_PUBLISH
```

Valor padrão:

```text
60
```

Insights abaixo desse limite podem ser persistidos ou processados internamente sem gerar o evento `eco.insight.created`, conforme a regra implementada pelo serviço.

---

## 15. Rebuild de insights

O `InsightRebuildService` existe como estrutura inicial, mas ainda não executa uma reconstrução real.

Comportamento atual:

* Valida o acesso à porta de leitura.
* Registra um aviso nos logs.
* Não recalcula métricas.
* Não recria insights.
* Não republica eventos.
* Não está conectado a endpoint ou scheduler ativo.

Um rebuild completo deverá:

1. Receber usuário e intervalo.
2. Carregar dados históricos.
3. Recalcular métricas.
4. Recalcular tendências.
5. Recriar ou atualizar insights.
6. Persistir os resultados.
7. Publicar eventos por Outbox.
8. Registrar progresso e falhas.
9. Suportar execução idempotente.

---

## 16. Persistência

O serviço utiliza PostgreSQL com migrations Flyway.

Principais grupos de dados:

* Insights.
* Snapshots de métricas.
* Tendências.
* Metas financeiras.
* Registros de idempotência.

Configuração local padrão:

```text
jdbc:postgresql://localhost:5437/ecofy_insights
```

Alterações estruturais devem ser adicionadas por novas migrations.

Migrations já aplicadas em ambientes compartilhados não devem ser modificadas.

---

## 17. Variáveis de ambiente

| Variável                            | Valor padrão em desenvolvimento                    | Descrição                        |
| ----------------------------------- | -------------------------------------------------- | -------------------------------- |
| `DB_URL`                            | `jdbc:postgresql://localhost:5437/ecofy_insights`  | URL JDBC                         |
| `DB_USER`                           | Configuração local                                 | Usuário do banco                 |
| `DB_PASS`                           | Configuração local                                 | Senha do banco                   |
| `KAFKA_BOOTSTRAP_SERVERS`           | `localhost:19092`                                  | Endereço do Kafka                |
| `JWT_JWKS_URI`                      | `http://localhost:8081/auth/.well-known/jwks.json` | JWKS do `ms-auth`                |
| `INS_SECURITY_PERMIT_ALL`           | `true` em desenvolvimento                          | Libera as rotas de negócio       |
| `INS_IDEMPOTENCY_TTL_SECONDS`       | `86400`                                            | TTL de idempotência              |
| `INS_MIN_SCORE_TO_PUBLISH`          | `60`                                               | Score mínimo para publicação     |
| `INS_CLIENT_CATEGORIZATION_ENABLED` | `false`                                            | Habilita client de categorização |
| `INS_CLIENT_BUDGETING_ENABLED`      | `false`                                            | Habilita client de budgeting     |

Exemplo local:

```env
DB_URL=jdbc:postgresql://localhost:5437/ecofy_insights
DB_USER=postgres
DB_PASS=postgres

KAFKA_BOOTSTRAP_SERVERS=localhost:19092
JWT_JWKS_URI=http://localhost:8081/auth/.well-known/jwks.json

INS_SECURITY_PERMIT_ALL=true
INS_IDEMPOTENCY_TTL_SECONDS=86400
INS_MIN_SCORE_TO_PUBLISH=60

INS_CLIENT_CATEGORIZATION_ENABLED=false
INS_CLIENT_BUDGETING_ENABLED=false
```

> Credenciais e segredos de produção não devem ser versionados.

---

## 18. Execução local

### 18.1 Pré-requisitos

* JDK 25.
* Docker ou PostgreSQL local.
* Kafka local para testar mensageria.
* Porta `8085` disponível.
* `ms-auth` acessível quando JWT for obrigatório.

### 18.2 Executar com Maven Wrapper

Linux ou macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

### 18.3 Verificar o serviço

```bash
curl -i \
  "http://localhost:8085/insights/actuator/health"
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

---

## 19. Build e testes

### 19.1 Executar testes

Linux ou macOS:

```bash
./mvnw clean test
```

Windows:

```powershell
.\mvnw.cmd clean test
```

### 19.2 Executar com profile de teste

```bash
./mvnw clean test -Dspring.profiles.active=test
```

### 19.3 Gerar o pacote

```bash
./mvnw clean package
```

O artefato será gerado em:

```text
target/
```

### 19.4 Executar o JAR

```bash
java -jar target/*.jar
```

---

## 20. Estratégia de testes

A suíte atual utiliza principalmente:

* Testes unitários.
* JUnit 5.
* Mockito.
* Testes de controllers com `@WebMvcTest`.
* Execução sem PostgreSQL real.
* Execução sem Kafka real.

Os testes cobrem componentes isolados sem exigir infraestrutura externa.

Ainda devem ser adicionados testes de integração para:

* Persistência com PostgreSQL.
* Migrations Flyway.
* Consumers Kafka.
* Publicação Kafka.
* Retry e tratamento de mensagens inválidas.
* Idempotência com concorrência real.
* Integração JWT com o `ms-auth`.
* Fallback dos clientes externos.
* Contrato entre `ms-insights` e `ms-notification`.

---

## 21. Observabilidade

O serviço utiliza Spring Boot Actuator e Micrometer.

Endpoints disponíveis:

```text
/insights/actuator/health
/insights/actuator/info
/insights/actuator/prometheus
```

A observabilidade deve abranger:

* Geração de insights.
* Insights publicados.
* Insights abaixo do score mínimo.
* Falhas de publicação Kafka.
* Retries dos consumers.
* Payloads irrecuperáveis.
* Falhas de integrações externas.
* Respostas `503`.
* Criação e atualização de metas.
* Conflitos de idempotência.
* Duração da geração.
* Correlation ID e MDC.

---

## 22. Limitação transacional da geração

O método de geração mantém uma transação aberta durante diferentes etapas:

1. Registro de idempotência.
2. Chamadas externas.
3. Cálculo dos insights.
4. Persistência.
5. Publicação Kafka.

Essa abordagem pode prolongar a transação durante operações de rede.

Riscos:

* Maior tempo de retenção de conexão com o banco.
* Locks mantidos durante I/O externo.
* Aumento da probabilidade de timeout.
* Rollback após uma chamada externa já concluída.
* Falta de atomicidade entre persistência e Kafka.

Evolução recomendada:

1. Executar chamadas externas fora da transação.
2. Abrir uma transação curta para persistência.
3. Gravar o evento em uma tabela Outbox.
4. Publicar o evento posteriormente.
5. Marcar o registro Outbox como processado.

---

## 23. Limitações conhecidas

### 23.1 Ausência de Outbox

A persistência do insight e a publicação Kafka não fazem parte de uma única operação atômica.

### 23.2 Ausência de DLT

Mensagens irrecuperáveis são registradas e confirmadas, mas não são armazenadas em uma Dead Letter Topic.

### 23.3 Rebuild incompleto

O `InsightRebuildService` ainda não reconstrói dados.

### 23.4 Transação longa

A geração mantém a transação aberta durante chamadas externas.

### 23.5 Clientes externos provisórios

Os clientes HTTP são placeholders e estão desabilitados por padrão.

### 23.6 Testes sem infraestrutura real

A suíte atual não valida Kafka e PostgreSQL reais.

### 23.7 Observabilidade parcial

Correlation ID, MDC e métricas de negócio ainda precisam ser aplicados de forma uniforme em todos os fluxos.

---

## 24. Próximos passos

1. Implementar Outbox transacional para `eco.insight.created`.
2. Adicionar DLT aos consumers Kafka.
3. Implementar rebuild real de insights.
4. Remover I/O externo da transação de geração.
5. Adicionar testes com Testcontainers.
6. Validar contratos Kafka com os serviços produtores e consumidores.
7. Implementar métricas de negócio.
8. Completar a propagação de correlation ID.
9. Padronizar MDC em controllers, services e consumers.
10. Finalizar os clientes HTTP externos.
11. Adicionar retry, timeout e circuit breaker às integrações.
12. Documentar versionamento dos eventos.
13. Definir chaves de particionamento Kafka.
14. Documentar políticas de retenção e replay.

---

## 25. Resumo de implementação

| Recurso                             | Situação      |
| ----------------------------------- | ------------- |
| Geração de insights                 | Implementada  |
| Dashboard consolidado               | Implementado  |
| Métricas tipadas                    | Implementadas |
| CRUD de metas                       | Implementado  |
| Idempotência                        | Implementada  |
| Consumo de transações categorizadas | Implementado  |
| Consumo de alertas de orçamento     | Implementado  |
| Publicação de `insight.created`     | Implementada  |
| Fallback externo explícito          | Implementado  |
| Retry Kafka                         | Implementado  |
| Dead Letter Topic                   | Pendente      |
| Outbox transacional                 | Pendente      |
| Rebuild real                        | Pendente      |
| Testes com Kafka real               | Pendentes     |
| Testes com PostgreSQL real          | Pendentes     |
| Observabilidade completa            | Parcial       |

---

## 26. Licença

Este microsserviço faz parte do projeto **EcoFy**.

Consulte o repositório principal para informações sobre:

* Arquitetura do ecossistema.
* Execução integrada.
* Licença.
* Contratos compartilhados.
* Configuração de infraestrutura.
* Fluxos de eventos.