#!/usr/bin/env bash
# ETAPA DE COMMIT - Suíte de testes de unidade (Surefire) + cobertura mínima de 70%.
source "$(dirname "$0")/comum.sh"

log "Executando testes de unidade"
$MVN -Drevision="${VERSAO}" -Dcobertura.minima=0.70 clean test jacoco:report jacoco:check
