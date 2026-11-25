#!/usr/bin/env bash
# =====================================================================
# SCRIPT DE IMPLANTAÇÃO DO ACERVO
#
# Uso:   deploy/deploy.sh <homologacao|producao> <versao>
# Exemplo: deploy/deploy.sh producao 1.0.12
#
# 1. Carrega a configuração do ambiente (deploy/env/<ambiente>.env)
# 2. Obtém a imagem versionada do registry (artefato imutável)
# 3. Sobe aplicação + banco com Docker Compose e aguarda o health check
# 4. Executa o smoke test (saúde, versão implantada, API e interface)
# 5. Em PRODUÇÃO, se algo falhar, faz rollback automático para a versão anterior
# =====================================================================
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AMBIENTE="${1:?Uso: deploy.sh <homologacao|producao> <versao>}"
VERSAO="${2:?Uso: deploy.sh <homologacao|producao> <versao>}"
ENV_FILE="${DIR}/env/${AMBIENTE}.env"

log()  { printf '\033[1;34m[deploy %s] %s\033[0m\n' "$(date '+%H:%M:%S')" "$*"; }
erro() { printf '\033[1;31m[deploy] %s\033[0m\n' "$*" >&2; }

[[ -f "$ENV_FILE" ]] || { erro "Ambiente desconhecido: ${AMBIENTE} (esperado: homologacao | producao)"; exit 2; }
set -a
# shellcheck source=/dev/null
source "$ENV_FILE"
set +a

export ACERVO_IMAGEM="${ACERVO_REGISTRY:-localhost:5050}/acervo"
export ACERVO_VERSAO="$VERSAO"
COMPOSE=(docker compose -p "acervo-${AMBIENTE}" -f "${DIR}/docker-compose.yml")
URL="http://${ACERVO_HOST:-localhost}:${ACERVO_PORTA}"

versao_em_execucao() {
  local id
  id="$("${COMPOSE[@]}" ps -q app 2>/dev/null || true)"
  [[ -n "$id" ]] && docker inspect --format '{{ index .Config.Labels "org.opencontainers.image.version" }}' "$id" 2>/dev/null || true
}

subir() {
  ACERVO_VERSAO="$1" "${COMPOSE[@]}" up -d --wait --wait-timeout 180
}

ANTERIOR="$(versao_em_execucao)"
log "Ambiente: ${AMBIENTE} | em execução: ${ANTERIOR:-nenhuma} | nova versão: ${VERSAO}"

log "Obtendo imagem ${ACERVO_IMAGEM}:${VERSAO}"
docker pull "${ACERVO_IMAGEM}:${VERSAO}"

if [[ "${ACERVO_AMBIENTE_EFEMERO:-false}" == "true" ]]; then
  log "Ambiente efêmero: removendo instância e dados anteriores"
  "${COMPOSE[@]}" down -v --remove-orphans
fi

if subir "$VERSAO" && "${DIR}/smoke-test.sh" "$URL" "$VERSAO"; then
  log "Versão ${VERSAO} implantada com sucesso em ${AMBIENTE}: ${URL}"
  exit 0
fi

erro "Falha na implantação da versão ${VERSAO} em ${AMBIENTE}"
"${COMPOSE[@]}" logs --tail 80 app || true

if [[ "$AMBIENTE" == "producao" && -n "$ANTERIOR" && "$ANTERIOR" != "$VERSAO" ]]; then
  log "Rollback automático para a versão ${ANTERIOR}"
  if subir "$ANTERIOR" && "${DIR}/smoke-test.sh" "$URL" "$ANTERIOR"; then
    log "Rollback concluído: ${AMBIENTE} voltou para ${ANTERIOR}"
  else
    erro "Rollback falhou - intervenção manual necessária"
  fi
fi
exit 1
