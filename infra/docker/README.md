# Execução local — EcoFy

Três modos de execução, do mais simples ao mais granular.

## Estrutura

```text
infra/docker/
├── docker-compose.infra.yml     # Kafka, 6× PostgreSQL, MongoDB, 2× Redis, maildev
├── docker-compose.apps.yml      # os 8 serviços da aplicação
├── api-gateway/docker-compose.yml
├── ms-auth/docker-compose.yml
├── ms-users/docker-compose.yml
├── ms-ingestion/docker-compose.yml
├── ms-categorization/docker-compose.yml
├── ms-budgeting/docker-compose.yml
├── ms-insights/docker-compose.yml
└── ms-notification/docker-compose.yml
```

O `docker-compose.yml` da raiz apenas inclui `infra` + `apps`.
Os composes por serviço contêm **somente a aplicação** e se anexam à rede
`ecofy-net` como externa — eles não recriam bancos nem broker, o que evita
conflito de `container_name` com a infraestrutura.

---

## Modo 1 — Stack completa

```bash
docker compose up -d --build
docker compose ps
```

O gateway responde em `http://localhost:8080`. A primeira execução compila os
8 serviços dentro das imagens e leva alguns minutos; as seguintes reaproveitam
o cache de dependências.

```bash
docker compose logs -f ms-categorization    # logs de um serviço
docker compose down                          # derruba, preservando volumes
docker compose down -v                       # derruba e apaga os dados
```

---

## Modo 2 — Um serviço por vez (recomendado para depurar)

Suba a infraestrutura uma única vez:

```bash
docker compose -f infra/docker/docker-compose.infra.yml up -d
```

Depois suba apenas o serviço em análise:

```bash
docker compose -f infra/docker/ms-categorization/docker-compose.yml up -d --build
docker compose -f infra/docker/ms-categorization/docker-compose.yml logs -f
docker compose -f infra/docker/ms-categorization/docker-compose.yml down
```

Cada arquivo documenta no cabeçalho de quais dependências o serviço precisa.
Como o Kafka é compartilhado, um serviço isolado continua produzindo e
consumindo normalmente — os eventos apenas ficam acumulados nos tópicos até
que o consumidor correspondente suba.

**Ordem sugerida para exercitar o pipeline ponta a ponta:**

```text
ms-auth → ms-users → ms-ingestion → ms-categorization → ms-budgeting
        → ms-insights → ms-notification → api-gateway
```

---

## Modo 3 — Infraestrutura em container, serviço na IDE

Útil para depurar com breakpoints.

```bash
docker compose -f infra/docker/docker-compose.infra.yml up -d
cd ms-budgeting && ./mvnw spring-boot:run
```

Os `application.yml` já apontam para `localhost` nas portas publicadas abaixo,
então normalmente nenhuma variável extra é necessária. Para sobrescrever, use
os nomes documentados em `.env.example`.

---

## Portas

| Serviço | Host | Contexto | Health |
|---|---:|---|---|
| api-gateway | 8080 | — | `/actuator/health` |
| ms-auth | 8081 | `/auth` | `/auth/actuator/health` |
| ms-ingestion | 8082 | `/ingestion` | `/ingestion/actuator/health` |
| ms-categorization | 8083 | `/categorization` | `/categorization/actuator/health` |
| ms-budgeting | 8084 | `/budgeting` | `/budgeting/actuator/health` |
| ms-insights | 8085 | `/insights` | `/insights/actuator/health` |
| ms-notification | 8086 | `/notification` | `/notification/actuator/health` |
| ms-users | 8087 | `/users` | `/users/actuator/health` |

| Infraestrutura | Host | Interno (na rede) |
|---|---:|---|
| Kafka | 19092 | `kafka:9092` |
| PostgreSQL auth | 5432 | `postgres-auth:5432` |
| PostgreSQL ingestion | 5434 | `postgres-ingestion:5432` |
| PostgreSQL categorization | 5435 | `postgres-categorization:5432` |
| PostgreSQL budgeting | 5436 | `postgres-budgeting:5432` |
| PostgreSQL insights | 5437 | `postgres-insights:5432` |
| PostgreSQL users | 5438 | `postgres-users:5432` |
| MongoDB | 27017 | `mongo-notification:27017` |
| Redis auth | 6379 | `redis-auth:6379` |
| Redis insights | 6380 | `redis-insights:6379` |
| Maildev (UI / SMTP) | 1080 / 1025 | `maildev:1025` |

> Dentro da rede, os bancos atendem sempre na porta padrão (5432, 6379). As
> portas distintas existem apenas para não colidir no host.

---

## Verificações rápidas

```bash
# Saúde de todos os serviços
for p in 8080:"" 8081:auth 8082:ingestion 8083:categorization \
         8084:budgeting 8085:insights 8086:notification 8087:users; do
  port=${p%%:*}; ctx=${p#*:}
  echo -n "$port ${ctx:-root}: "
  curl -s "http://localhost:$port/${ctx:+$ctx/}actuator/health" | head -c 60; echo
done

# Tópicos criados
docker exec ecofy-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --list

# Conteúdo de uma DLT
docker exec ecofy-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic eco.categorization.request.dlt --from-beginning --max-messages 5
```

---

## Problemas comuns

**Um serviço fica reiniciando.** Veja os logs (`docker compose logs <serviço>`).
A causa mais frequente é a infraestrutura ainda subindo: o `depends_on` espera
o healthcheck, mas na primeira execução o Flyway pode competir com o banco em
inicialização. Um `docker compose restart <serviço>` resolve.

**`network ecofy-net not found`** ao usar um compose isolado. A infraestrutura
não está no ar; suba-a primeiro (Modo 2).

**Porta em uso.** Outro processo local ocupa a porta. Ajuste o mapeamento no
compose correspondente ou libere a porta.

**Kafka não aceita conexão do host.** O listener externo é `localhost:19092`;
`9092` é o listener interno da rede e não funciona a partir do host.
