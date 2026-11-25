#!/usr/bin/env bash
# ETAPA DE COMMIT - Suíte de testes de integração (Failsafe): contexto Spring, HTTP, JPA, Flyway.
source "$(dirname "$0")/comum.sh"

log "Executando testes de integração"
$MVN -Drevision="${VERSAO}" -DskipUTs=true -Dcobertura.minima=0.85 clean verify
