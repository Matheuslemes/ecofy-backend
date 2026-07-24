# ms-users — EcoFy

> 🇬🇧 **English summary first.**
> 🇧🇷 **Documentação completa em Português abaixo.**

O `ms-users` é o microsserviço responsável pelo domínio de usuários no ecossistema **EcoFy**, incluindo perfis, preferências, conexões, contas vinculadas, resolução de contatos e sincronização de dados provenientes do `ms-auth`.

---

## 🇬🇧 English Summary

### Responsibility

The `ms-users` service owns the EcoFy user domain:

* User profiles
* User preferences
* Connections
* Linked accounts
* Contact resolution
* User synchronization from `ms-auth`

Synchronization can occur through:

1. An internal HTTP endpoint protected by an internal token.
2. A Kafka event consumed by the service.

### Technology stack

* Java 25
* Spring Boot 4
* Spring Security
* OAuth2 Resource Server
* PostgreSQL
* Flyway
* Apache Kafka
* Maven
* Hexagonal Architecture

### Service configuration

| Property     | Value        |
| ------------ | ------------ |
| Port         | `8087`       |
| Context path | `/users`     |
| Database     | PostgreSQL   |
| Messaging    | Apache Kafka |

### Main endpoints

| Method         | Endpoint                             | Protection         | Description                                 |
| -------------- | ------------------------------------ | ------------------ | ------------------------------------------- |
| `PUT`          | `/internal/users/{authUserId}`       | `X-Internal-Token` | Creates or updates a profile from `ms-auth` |
| `POST` / `GET` | `/api/users/v1/profile`              | JWT                | Manages the authenticated user's profile    |
| `PUT` / `GET`  | `/api/users/v1/preferences/{userId}` | JWT                | Manages user preferences                    |
| `POST` / `GET` | `/api/users/v1/connections`          | JWT                | Manages user connections                    |
| `GET`          | `/actuator/health`                   | Public             | Reports service health                      |
| `GET`          | `/actuator/info`                     | Public             | Reports service information                 |

### Security

Business endpoints are protected according to the active profile.

The `USR_SECURITY_PERMIT_ALL` property may allow `/api/users/**` requests without JWT authentication in development and test environments.

The `/internal/**` endpoints remain protected in every environment and require:

```http
X-Internal-Token: <internal-token>
```

JWT validation uses the JWKS endpoint exposed by `ms-auth`.

### Authentication synchronization

The primary working synchronization flow is the internal HTTP endpoint:

```http
PUT /users/internal/users/{authUserId}
```

The path variable `authUserId` is treated as the source of truth.

The service performs an idempotent upsert using `externalAuthId`, preventing duplicate user profiles.

### Kafka integration

The service currently consumes:

```text
auth.user.created
```

Expected message:

```text
AuthUserCreatedEventMessage
```

Main fields:

* `userId`
* `externalAuthId`
* `fullName`
* `email`
* `phone`

> ⚠️ `ms-auth` currently publishes `auth.user.registered`. Therefore, the topic names and possibly the event contracts are not fully aligned. Until this integration is corrected, the internal HTTP synchronization remains the primary supported flow.

The service also publishes profile-created and profile-updated events through `UserEventKafkaAdapter`.

### Database

The service uses PostgreSQL with schema versioning managed by Flyway.

### Build and test

```bash
./mvnw clean test
./mvnw clean package
./mvnw spring-boot:run
```

### Known limitations

* Kafka synchronization is not fully aligned with `ms-auth`.
* Internal HTTP synchronization is currently the primary integration path.
* End-to-end Kafka tests are still pending.
* Some core components still depend on Spring abstractions.
* Idempotent retries for creation operations use a simplified resource reconstruction strategy.

---

# 🇧🇷 Documentação

## 1. Visão geral

O `ms-users` centraliza as funcionalidades relacionadas aos usuários do EcoFy.

Suas principais responsabilidades são:

* Manter o perfil do usuário.
* Sincronizar dados cadastrais com o `ms-auth`.
* Gerenciar preferências.
* Gerenciar conexões.
* Gerenciar contas vinculadas.
* Resolver informações de contato.
* Publicar eventos relacionados à criação e à atualização de perfis.
* Garantir idempotência nas operações mutáveis suportadas.

---

## 2. Stack tecnológica

| Tecnologia             | Finalidade                            |
| ---------------------- | ------------------------------------- |
| Java 25                | Linguagem principal                   |
| Spring Boot 4          | Framework da aplicação                |
| Spring Security        | Autenticação e autorização            |
| OAuth2 Resource Server | Validação de tokens JWT               |
| PostgreSQL             | Persistência relacional               |
| Flyway                 | Versionamento do schema               |
| Apache Kafka           | Comunicação assíncrona                |
| Maven Wrapper          | Build e gerenciamento de dependências |
| JUnit 5                | Testes                                |
| Mockito                | Testes unitários isolados             |

---

## 3. Arquitetura

O serviço segue uma arquitetura hexagonal, separando o domínio das tecnologias de entrada e saída.

Estrutura principal:

```text
src/main/java/br/com/ecofy/ms_users
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

| Camada             | Responsabilidade                                                 |
| ------------------ | ---------------------------------------------------------------- |
| `core/domain`      | Entidades, value objects, enums e regras de domínio              |
| `core/application` | Serviços de aplicação, commands e results                        |
| `core/port/in`     | Casos de uso expostos pela aplicação                             |
| `core/port/out`    | Contratos necessários para persistência e integrações            |
| `adapters/in`      | Controllers HTTP, filtros e consumers Kafka                      |
| `adapters/out`     | Persistência, publicação Kafka e integrações externas            |
| `config`           | Configurações de segurança, propriedades, Kafka e infraestrutura |

---

## 4. Configuração do serviço

| Configuração   | Valor         |
| -------------- | ------------- |
| Porta padrão   | `8087`        |
| Context path   | `/users`      |
| Banco de dados | PostgreSQL    |
| Schema padrão  | `ecofy_users` |
| Mensageria     | Apache Kafka  |

URL local base:

```text
http://localhost:8087/users
```

---

## 5. Responsabilidades funcionais

### 5.1 Perfil

O serviço mantém o perfil do usuário por meio da entidade `EcoUserProfile`.

Os dados podem ser:

* Criados pelo fluxo de sincronização com o `ms-auth`.
* Atualizados pelo próprio usuário.
* Consultados por endpoints autenticados.
* Atualizados por eventos Kafka, quando o contrato estiver alinhado.

### 5.2 Preferências

O serviço gerencia preferências como:

* Tema da aplicação.
* Idioma e locale.
* Moeda padrão.
* Timezone.
* Formato de data.
* Canais de notificação.

### 5.3 Conexões

O serviço mantém conexões associadas ao usuário, incluindo informações como:

* Tipo da conexão.
* Provider.
* Metadados.
* Identificador da conta externa.
* Situação da vinculação.

### 5.4 Contas vinculadas

As contas vinculadas representam identidades ou integrações externas associadas ao perfil EcoFy.

### 5.5 Resolução de contato

O serviço pode resolver dados de contato persistidos no perfil, como:

* E-mail.
* Telefone.
* Nome completo.
* Informações complementares de contato.

---

## 6. Endpoints principais

### 6.1 Sincronização interna

| Método | Endpoint                       | Proteção           | Descrição                                        |
| ------ | ------------------------------ | ------------------ | ------------------------------------------------ |
| `PUT`  | `/internal/users/{authUserId}` | `X-Internal-Token` | Cria ou atualiza o perfil enviado pelo `ms-auth` |

### 6.2 Perfil

| Método | Endpoint                | Proteção | Descrição                                |
| ------ | ----------------------- | -------- | ---------------------------------------- |
| `POST` | `/api/users/v1/profile` | JWT      | Cria o perfil do usuário                 |
| `GET`  | `/api/users/v1/profile` | JWT      | Consulta o perfil do usuário autenticado |

### 6.3 Preferências

| Método | Endpoint                             | Proteção                | Descrição                |
| ------ | ------------------------------------ | ----------------------- | ------------------------ |
| `PUT`  | `/api/users/v1/preferences/{userId}` | JWT + `Idempotency-Key` | Atualiza as preferências |
| `GET`  | `/api/users/v1/preferences/{userId}` | JWT                     | Consulta as preferências |

### 6.4 Conexões e contas vinculadas

As operações relacionadas a conexões e contas vinculadas são disponibilizadas sob:

```text
/api/users/**
```

Essas rotas exigem JWT nos ambientes em que `permit-all` está desabilitado.

### 6.5 Actuator

| Método | Endpoint           | Proteção | Descrição                      |
| ------ | ------------------ | -------- | ------------------------------ |
| `GET`  | `/actuator/health` | Público  | Verifica a saúde do serviço    |
| `GET`  | `/actuator/info`   | Público  | Exibe informações da aplicação |

> A configuração de desenvolvimento pode liberar `/api/users/**`, mas nunca libera automaticamente `/internal/**`.

---

## 7. Integração com o ms-auth

O `ms-users` recebe dados cadastrais do `ms-auth` por dois mecanismos.

### 7.1 Sincronização por HTTP interno

O `ms-auth` realiza a chamada:

```http
PUT /users/internal/users/{authUserId}
```

Header obrigatório:

```http
X-Internal-Token: <internal-token>
```

O `authUserId` recebido no path é considerado a fonte de verdade para a operação.

Mesmo que o payload contenha um campo `authUserId`, o identificador do path deve prevalecer.

### 7.2 Upsert de perfil

A sincronização utiliza `externalAuthId` para localizar um perfil existente.

O comportamento esperado é:

1. Localizar o perfil por `externalAuthId`.
2. Criar o perfil quando nenhum registro for encontrado.
3. Atualizar o registro existente quando ele já estiver persistido.
4. Evitar a criação de perfis duplicados.

### 7.3 Campos sincronizados

Os principais campos sincronizados incluem:

* `authUserId`
* `externalAuthId`
* `email`
* `fullName`
* `firstName`
* `lastName`
* `phone`
* `emailVerified`
* `status`
* `locale`

Os campos `emailVerified` e `locale` são persistidos junto aos demais dados recebidos do serviço de autenticação.

### 7.4 Exemplo de chamada

```bash
curl -i -X PUT \
  "http://localhost:8087/users/internal/users/{authUserId}" \
  -H "Content-Type: application/json" \
  -H "X-Internal-Token: local-internal-token" \
  -d '{
    "authUserId": "{authUserId}",
    "email": "user@ecofy.com",
    "firstName": "First",
    "lastName": "Last",
    "emailVerified": true,
    "status": "ACTIVE",
    "locale": "pt-BR"
  }'
```

---

## 8. Segurança

O serviço possui dois mecanismos de autenticação distintos:

1. JWT para a API de negócio.
2. Token interno para comunicação entre microsserviços.

---

### 8.1 Segurança da API de negócio

As rotas `/api/users/**` são protegidas pelo Spring Security.

Em produção, o serviço atua como OAuth2 Resource Server e valida tokens JWT utilizando o JWKS publicado pelo `ms-auth`.

Configuração principal:

```text
JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json
```

A propriedade abaixo controla a liberação das rotas de negócio:

```text
USR_SECURITY_PERMIT_ALL
```

Comportamento esperado:

| Valor   | Comportamento                                                     |
| ------- | ----------------------------------------------------------------- |
| `true`  | Permite acesso sem JWT às rotas configuradas para desenvolvimento |
| `false` | Exige JWT válido                                                  |

> Essa propriedade não deve liberar os endpoints internos.

---

### 8.2 Segurança do endpoint interno

As rotas `/internal/**` são protegidas pelo `InternalTokenAuthenticationFilter`.

Header exigido:

```http
X-Internal-Token: <token>
```

Propriedades relacionadas:

```properties
ecofy.users.internal.enabled=true
ecofy.users.internal.token=local-internal-token
```

Variável de ambiente correspondente:

```text
INTERNAL_TOKEN
```

### 8.3 Comportamento da autenticação interna

| Situação                          | Resultado                        |
| --------------------------------- | -------------------------------- |
| Header ausente                    | `401 Unauthorized`               |
| Token inválido                    | `401 Unauthorized`               |
| Token não configurado             | `401 Unauthorized`               |
| Autenticação interna desabilitada | `401 Unauthorized`               |
| Token válido                      | Autenticação com `ROLE_INTERNAL` |

Após a autenticação, as rotas internas exigem:

```java
hasRole("INTERNAL")
```

A proteção permanece ativa mesmo quando:

```text
USR_SECURITY_PERMIT_ALL=true
```

Essa estratégia segue o princípio de **fail closed**: em caso de configuração ausente ou inválida, o acesso é negado.

---

## 9. Política de preferências

A política adotada para valores vazios é:

> Valor vazio ou composto apenas por espaços representa a remoção da preferência.

Exemplo:

```json
{
  "DEFAULT_CURRENCY": ""
}
```

Nesse caso, a preferência `DEFAULT_CURRENCY` é removida.

### 9.1 Regras

* `""` remove a preferência.
* `"   "` remove a preferência.
* Valores `null` não são persistidos.
* A coluna `pref_value` permanece com restrição `NOT NULL`.
* Valores inválidos resultam em erro de validação.
* Chaves desconhecidas são ignoradas para preservar compatibilidade futura.

### 9.2 Exemplo de validação

Uma moeda padrão deve utilizar um código compatível com o formato ISO 4217, como:

```text
BRL
```

Um valor inválido pode resultar em:

```http
400 Bad Request
```

Código de erro:

```text
BUSINESS_VALIDATION
```

---

## 10. Idempotência

Operações mutáveis suportadas utilizam o header:

```http
Idempotency-Key: <unique-key>
```

A idempotência evita que uma mesma requisição seja aplicada mais de uma vez em casos como:

* Retry do cliente.
* Timeout após processamento.
* Reenvio por falha de rede.
* Requisições concorrentes.
* Duplicação acidental.

### 10.1 Resultados possíveis

O registro da chave retorna um `IdempotencyOutcome`.

| Resultado    | Condição                     | Comportamento                        |
| ------------ | ---------------------------- | ------------------------------------ |
| `REGISTERED` | Chave ainda não utilizada    | Executa a operação                   |
| `DUPLICATE`  | Mesma chave e mesmo hash     | Não reaplica; retorna o estado atual |
| `CONFLICT`   | Mesma chave e hash diferente | Retorna conflito                     |

### 10.2 Retry legítimo

Quando a mesma chave é utilizada com exatamente o mesmo conteúdo:

```text
mesma Idempotency-Key + mesmo request hash
```

o resultado é:

```text
DUPLICATE
```

Esse cenário representa um retry legítimo e não deve retornar `409`.

### 10.3 Conflito

Quando a mesma chave é reutilizada com conteúdo diferente:

```text
mesma Idempotency-Key + request hash diferente
```

o serviço retorna:

```http
409 Conflict
```

Código de erro:

```text
IDEMPOTENCY_VIOLATION
```

### 10.4 Concorrência

O banco possui uma restrição única equivalente a:

```text
(operation, idem_key)
```

Quando duas requisições concorrentes tentam registrar a mesma chave:

1. Uma delas registra a chave.
2. A outra recebe a violação de unicidade.
3. A violação é capturada.
4. O serviço consulta o registro existente.
5. O resultado é reclassificado como `DUPLICATE` ou `CONFLICT`.

Esse tratamento evita que uma corrida legítima resulte em erro interno `500`.

---

## 11. Integração Kafka

### 11.1 Eventos consumidos

O serviço está configurado para consumir:

```text
auth.user.created
```

Propriedade:

```text
ecofy.users.topics.auth-user-created
```

Consumer:

```text
AuthUserCreatedEventConsumer
```

Contrato esperado:

```text
AuthUserCreatedEventMessage
```

Campos principais:

```text
userId
externalAuthId
fullName
email
phone
```

Ao receber o evento, o serviço realiza upsert do perfil por `externalAuthId`.

---

### 11.2 Eventos publicados

O serviço publica eventos relacionados ao perfil por meio do:

```text
UserEventKafkaAdapter
```

Tópico atual:

```text
eco.user.event
```

Os eventos representam operações como:

* Perfil criado.
* Perfil atualizado.

---

### 11.3 Incompatibilidade conhecida

Existe uma incompatibilidade entre os nomes dos tópicos utilizados pelos serviços:

| Serviço                      | Tópico                 |
| ---------------------------- | ---------------------- |
| `ms-users` espera consumir   | `auth.user.created`    |
| `ms-auth` publica atualmente | `auth.user.registered` |

Essa diferença impede que a sincronização por Kafka seja considerada completamente funcional.

Além do nome do tópico, o payload dos eventos também deve ser revisado para garantir compatibilidade entre:

* Nome dos campos.
* Tipos dos campos.
* Campos obrigatórios.
* Versionamento do contrato.
* Estratégia de serialização.

Até que essa integração seja corrigida, a sincronização HTTP interna é o fluxo principal e funcional.

---

## 12. Banco de dados e migrations

O serviço utiliza PostgreSQL.

O versionamento do schema é gerenciado pelo Flyway.

Configuração local esperada:

```text
jdbc:postgresql://localhost:5438/ecofy_users
```

Schema padrão:

```text
ecofy_users
```

O Flyway deve ser responsável pela criação e pela evolução de:

* Perfis.
* Preferências.
* Conexões.
* Contas vinculadas.
* Registros de idempotência.
* Constraints.
* Índices.
* Colunas de versionamento otimista, quando aplicável.

> Alterações estruturais devem ser realizadas por novas migrations. Migrations já aplicadas não devem ser alteradas em ambientes compartilhados.

---

## 13. Variáveis de ambiente

| Variável                  | Valor padrão em desenvolvimento                | Descrição                                    |
| ------------------------- | ---------------------------------------------- | -------------------------------------------- |
| `DB_URL`                  | `jdbc:postgresql://localhost:5438/ecofy_users` | URL JDBC                                     |
| `DB_USER`                 | Configuração local                             | Usuário do PostgreSQL                        |
| `DB_PASS`                 | Configuração local                             | Senha do PostgreSQL                          |
| `DB_SCHEMA`               | `ecofy_users`                                  | Schema utilizado pelo serviço                |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:19092`                              | Endereço do Kafka                            |
| `JWT_JWKS_URI`            | `http://localhost:8081/.well-known/jwks.json`  | JWKS publicado pelo `ms-auth`                |
| `INTERNAL_TOKEN`          | `local-internal-token`                         | Token utilizado em `/internal/**`            |
| `USR_SECURITY_PERMIT_ALL` | `true`                                         | Libera rotas de negócio em desenvolvimento   |
| `USR_IDEMPOTENCY_TTL`     | `PT24H`                                        | Tempo de retenção das chaves de idempotência |

### Exemplo local

```env
DB_URL=jdbc:postgresql://localhost:5438/ecofy_users
DB_USER=postgres
DB_PASS=postgres
DB_SCHEMA=ecofy_users

KAFKA_BOOTSTRAP_SERVERS=localhost:19092
JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json

INTERNAL_TOKEN=local-internal-token
USR_SECURITY_PERMIT_ALL=true
USR_IDEMPOTENCY_TTL=PT24H
```

> Os valores acima são apenas para desenvolvimento local. Segredos reais não devem ser versionados no repositório.

---

## 14. Profiles

| Profile           | Segurança da API        | Banco                                | Kafka                   |
| ----------------- | ----------------------- | ------------------------------------ | ----------------------- |
| `default` / `dev` | `permit-all` habilitado | PostgreSQL local                     | Broker local            |
| `test`            | `permit-all` habilitado | H2 em memória nos testes de contexto | Listeners desabilitados |
| `prod`            | JWT obrigatório         | Configuração externa                 | SASL/SSL                |

Independentemente do profile utilizado:

```text
/internal/** exige X-Internal-Token
```

---

## 15. Execução local

### 15.1 Pré-requisitos

* JDK 25
* Docker ou PostgreSQL local
* Kafka local, quando os listeners estiverem habilitados
* Porta `8087` disponível
* `ms-auth` disponível para validação JWT, quando `permit-all` estiver desabilitado

### 15.2 Executar a aplicação

Linux ou macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

### 15.3 Verificar a aplicação

```bash
curl -i http://localhost:8087/users/actuator/health
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

---

## 16. Build

### 16.1 Executar testes

Linux ou macOS:

```bash
./mvnw clean test
```

Windows:

```powershell
.\mvnw.cmd clean test
```

### 16.2 Gerar o artefato

Linux ou macOS:

```bash
./mvnw clean package
```

Windows:

```powershell
.\mvnw.cmd clean package
```

O arquivo JAR será gerado em:

```text
target/
```

### 16.3 Executar o JAR

```bash
java -jar target/*.jar
```

---

## 17. Testes

A estratégia atual inclui testes unitários e teste de inicialização do contexto.

### 17.1 Testes unitários

Os testes unitários utilizam:

* JUnit 5.
* Mockito.
* Execução sem PostgreSQL real.
* Execução sem Kafka real.
* Serviços e adapters testados de forma isolada.

### 17.2 Teste de contexto

O teste `contextLoads`, executado com `@SpringBootTest`, utiliza:

* H2 em memória.
* Listeners Kafka desabilitados.
* Configuração específica de teste.
* Nenhuma dependência obrigatória de infraestrutura externa.

### 17.3 Escopo ainda não coberto

Ainda são necessários testes de integração para validar:

* Consumo real de eventos Kafka.
* Publicação real de eventos.
* Compatibilidade entre `ms-auth` e `ms-users`.
* Persistência em PostgreSQL.
* Concorrência real na idempotência.
* Segurança JWT integrada com o JWKS do `ms-auth`.

---

## 18. Respostas de erro

Os principais códigos de erro relacionados às regras documentadas incluem:

| HTTP  | Código                   | Situação                              |
| ----- | ------------------------ | ------------------------------------- |
| `400` | `BUSINESS_VALIDATION`    | Dados ou preferências inválidas       |
| `401` | Código de autenticação   | JWT ou token interno ausente/inválido |
| `403` | Código de autorização    | Usuário autenticado sem permissão     |
| `409` | `IDEMPOTENCY_VIOLATION`  | Reutilização conflitante da chave     |
| `500` | Erro interno padronizado | Falha inesperada                      |

As respostas devem preservar o contrato padronizado do ecossistema EcoFy, incluindo informações como:

* Timestamp.
* Status HTTP.
* Código do erro.
* Mensagem.
* Correlation ID.
* Path.
* Detalhes de validação, quando aplicável.

---

## 19. Observabilidade

O serviço disponibiliza endpoints do Spring Boot Actuator:

```text
/users/actuator/health
/users/actuator/info
```

A evolução de observabilidade deve contemplar:

* Correlation ID propagado entre serviços.
* MDC nos logs.
* Métricas por endpoint.
* Métricas de consumo Kafka.
* Métricas de idempotência.
* Contadores de sincronizações criadas e atualizadas.
* Métricas de falha no endpoint interno.
* Métricas de falha na validação JWT.

---

## 20. Limitações conhecidas

### 20.1 Incompatibilidade Kafka

O `ms-users` espera `auth.user.created`, enquanto o `ms-auth` publica `auth.user.registered`.

O nome do tópico e o contrato do payload precisam ser unificados.

### 20.2 Ausência de teste Kafka ponta a ponta

O consumer possui cobertura unitária, mas ainda não há validação com broker real ou embedded Kafka.

### 20.3 Dependências de framework no core

Alguns componentes do núcleo ainda podem depender de abstrações do Spring.

Uma arquitetura hexagonal mais rígida deve manter o core independente do framework.

### 20.4 Reconstrução de resultado idempotente

Em retries de operações de criação, a recuperação do resultado anterior utiliza uma estratégia simplificada baseada na busca do recurso equivalente.

### 20.5 Observabilidade incompleta

A propagação de correlation ID, MDC e métricas detalhadas ainda precisa ser concluída em todos os fluxos.

### 20.6 Documentação dos eventos

Os eventos ainda precisam de uma especificação formal contendo:

* Nome canônico do tópico.
* Versão do evento.
* Schema do payload.
* Campos obrigatórios.
* Campos opcionais.
* Chave de particionamento.
* Política de retry.
* Estratégia de DLT.
* Compatibilidade entre versões.

---

## 21. Próximos passos

1. Alinhar o tópico Kafka entre `ms-auth` e `ms-users`.
2. Padronizar o contrato de `AuthUserCreatedEventMessage`.
3. Adicionar testes de integração com Kafka.
4. Adicionar testes de integração com PostgreSQL.
5. Validar concorrência de idempotência com banco real.
6. Remover dependências Spring restantes do core.
7. Padronizar correlation ID e MDC.
8. Adicionar métricas de negócio e integração.
9. Documentar os eventos com versionamento explícito.
10. Validar autorização baseada no proprietário do recurso.
11. Revisar encoding dos arquivos e mensagens.
12. Documentar todos os endpoints e contratos HTTP de forma detalhada.

---

## 22. Resumo operacional

| Item                             | Situação                                   |
| -------------------------------- | ------------------------------------------ |
| Persistência de perfil           | Implementada                               |
| Preferências                     | Implementadas                              |
| Conexões                         | Implementadas                              |
| Linked accounts                  | Implementadas                              |
| Sincronização HTTP com `ms-auth` | Funcional                                  |
| Proteção por token interno       | Implementada                               |
| JWT em produção                  | Implementado                               |
| Idempotência                     | Implementada                               |
| Consumo Kafka                    | Implementado, mas com contrato desalinhado |
| Publicação de eventos de usuário | Implementada                               |
| Testes unitários                 | Implementados                              |
| Testes Kafka ponta a ponta       | Pendentes                                  |
| Observabilidade completa         | Parcial                                    |
| Independência total do core      | Parcial                                    |

---

## 23. Licença

Este serviço faz parte do projeto **EcoFy**.

Consulte o repositório principal para informações sobre licença, arquitetura global, execução do ecossistema e padrões compartilhados.
