#!/usr/bin/env bash
# Teste de fumaça pós-implantação.
# Uso: deploy/smoke-test.sh <url-base> [versao-esperada]
set -euo pipefail
URL="${1:?Uso: smoke-test.sh <url-base> [versao-esperada]}"
ESPERADA="${2:-}"

falha() { printf '\033[1;31m[smoke] FALHOU: %s\033[0m\n' "$*" >&2; exit 1; }

for _ in $(seq 1 30); do
  curl -fsS "${URL}/actuator/health" >/dev/null 2>&1 && break
  sleep 2
done

SAUDE="$(curl -fsS "${URL}/actuator/health")" || falha "health check indisponível em ${URL}"
grep -q '"status":"UP"' <<<"$SAUDE" || falha "aplicação não está UP: ${SAUDE}"

INFO="$(curl -fsS "${URL}/actuator/info")" || falha "endpoint /actuator/info indisponível"
if [[ -n "$ESPERADA" ]]; then
  grep -q "\"version\":\"${ESPERADA}\"" <<<"$INFO" || falha "versão implantada difere da esperada (${ESPERADA}): ${INFO}"
fi

curl -fsS "${URL}/api/livros" >/dev/null || falha "API de livros não respondeu"
curl -fsS "${URL}/" | grep -q "<title>Acervo" || falha "interface web não respondeu"

printf '\033[1;32m[smoke] OK - %s respondendo na versão %s\033[0m\n' "$URL" "${ESPERADA:-(não verificada)}"
