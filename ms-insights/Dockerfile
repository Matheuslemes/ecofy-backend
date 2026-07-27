# syntax=docker/dockerfile:1.7
# =============================================================================
# EcoFy — ms-insights (build multi-stage)
#   Estágio 1: compila o JAR com Maven + JDK 25.
#   Estágio 2: executa em runtime enxuto (JRE 25) como usuário sem privilégios.
# =============================================================================

# ---- Estágio 1: build -------------------------------------------------------
ARG MAVEN_IMAGE=maven:3.9-eclipse-temurin-25
ARG RUNTIME_IMAGE=eclipse-temurin:25-jre

FROM ${MAVEN_IMAGE} AS build
WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp package -Dmaven.test.skip=true

# ---- Estágio 2: runtime -----------------------------------------------------
FROM ${RUNTIME_IMAGE} AS runtime
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 ecofy \
    && useradd --uid 10001 --gid ecofy --no-create-home \
       --home-dir /nonexistent --shell /usr/sbin/nologin ecofy

COPY --from=build --chown=10001:10001 /build/target/*.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError" \
    TZ=UTC

USER 10001:10001
EXPOSE 8085

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD ["curl", "--fail", "--silent", "--show-error", "--max-time", "3", "http://127.0.0.1:8085/insights/actuator/health"]

STOPSIGNAL SIGTERM
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
