# syntax=docker/dockerfile:1
# =====================================================================
# Imagem de execução do Acervo.
# O JAR NÃO é compilado aqui: ele é produzido e testado na Etapa de Commit
# do pipeline e reaproveitado em todos os ambientes ("build once, deploy many").
#
#   docker build --build-arg VERSAO=1.0.7 --build-arg COMMIT=<sha> -t acervo:1.0.7 .
# =====================================================================

# ---- Estágio 1: extrai as camadas do JAR do Spring Boot -------------
FROM eclipse-temurin:17-jre AS camadas
WORKDIR /build
COPY target/acervo-*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extraido

# ---- Estágio 2: imagem final enxuta, usuário sem privilégios --------
FROM eclipse-temurin:17-jre
ARG VERSAO=dev
ARG COMMIT=desconhecido
ARG DATA_BUILD=desconhecida

LABEL org.opencontainers.image.title="Acervo" \
      org.opencontainers.image.description="Sistema de Gestão de Biblioteca - TP Gerência de Configuração" \
      org.opencontainers.image.version="${VERSAO}" \
      org.opencontainers.image.revision="${COMMIT}" \
      org.opencontainers.image.created="${DATA_BUILD}" \
      org.opencontainers.image.source="https://github.com/grupo-gcs/acervo" \
      org.opencontainers.image.licenses="MIT"

RUN groupadd --system acervo && useradd --system --gid acervo --home-dir /app acervo
WORKDIR /app

# Camadas ordenadas da menos para a mais volátil (melhor reaproveitamento de cache/registry)
COPY --from=camadas --chown=acervo:acervo /build/extraido/dependencies/ ./
COPY --from=camadas --chown=acervo:acervo /build/extraido/spring-boot-loader/ ./
COPY --from=camadas --chown=acervo:acervo /build/extraido/snapshot-dependencies/ ./
COPY --from=camadas --chown=acervo:acervo /build/extraido/application/ ./

USER acervo
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    ACERVO_VERSAO="${VERSAO}"

HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=6 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health/liveness >/dev/null 2>&1 || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
