#!/usr/bin/env bash
# Funções e variáveis compartilhadas pelos scripts do pipeline.
set -euo pipefail

# Versão do artefato: rótulo do pipeline no GoCD (ex.: 1.0.12) ou SNAPSHOT local
VERSAO="${GO_PIPELINE_LABEL:-1.0.0-SNAPSHOT}"
# Commit de origem: o GoCD expõe GO_REVISION_<NOME_DO_MATERIAL>
COMMIT="${GO_REVISION_CODIGO:-$(git rev-parse HEAD 2>/dev/null || echo desconhecido)}"
REGISTRY="${ACERVO_REGISTRY:-localhost:5050}"
IMAGEM="${REGISTRY}/acervo"
MVN="./mvnw -B -ntp"

log() { printf '\n\033[1;34m[%s] %s\033[0m\n' "$(date '+%H:%M:%S')" "$*"; }
erro() { printf '\n\033[1;31m[ERRO] %s\033[0m\n' "$*" >&2; }
