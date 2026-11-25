#!/usr/bin/env bash
# ETAPA DE COMMIT - Compilação (build) e empacotamento do JAR versionado.
source "$(dirname "$0")/comum.sh"

log "Compilando Acervo ${VERSAO} (commit ${COMMIT:0:8})"
java -version 2>&1 | head -1
$MVN -Drevision="${VERSAO}" clean package -DskipTests

log "Artefato gerado:"
ls -lh target/acervo-*.jar
sha256sum target/acervo-"${VERSAO}".jar | tee target/acervo-"${VERSAO}".jar.sha256
