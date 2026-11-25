# =====================================================================
# Agente GoCD do projeto Acervo
# Base oficial do GoCD + ferramentas de build exigidas pelo pipeline:
#   - JDK 17 (Eclipse Temurin)  -> compilação e testes (Maven Wrapper)
#   - Docker CLI + Compose      -> empacotamento e implantação
#   - jq                        -> utilitário para scripts
# A infraestrutura de CI também é um item de configuração versionado.
# =====================================================================
ARG GOCD_VERSION=v26.2.0

FROM docker:29-cli AS docker-cli
FROM eclipse-temurin:17-jdk AS jdk

FROM gocd/gocd-agent-ubuntu-24.04:${GOCD_VERSION}
USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends jq \
 && rm -rf /var/lib/apt/lists/*
COPY --from=jdk /opt/java/openjdk /opt/jdk-17
COPY --from=docker-cli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=docker-cli /usr/local/libexec/docker/cli-plugins/ /usr/local/libexec/docker/cli-plugins/
ENV JAVA_HOME=/opt/jdk-17 \
    PATH=/opt/jdk-17/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
# Diretório do cache Maven (montado como volume para acelerar builds)
RUN mkdir -p /home/go/.m2 && chown -R go:root /home/go/.m2
USER go
