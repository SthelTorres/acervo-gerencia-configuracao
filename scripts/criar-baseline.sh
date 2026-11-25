#!/usr/bin/env bash
# Cria a linha de base (baseline) de uma release: tag Git anotada apontando para o
# commit que gerou a versão implantada em produção.
# Uso: scripts/criar-baseline.sh <versao> [commit]   (ex.: scripts/criar-baseline.sh 1.0.12)
# Responsável: Gerente de Configuração (após aprovação do CCB e implantação em produção).
set -euo pipefail
VERSAO="${1:?Uso: criar-baseline.sh <versao> [commit]}"
COMMIT="${2:-}"
if [[ -z "$COMMIT" ]]; then
  # Descobre o commit pela própria aplicação em produção (rastreabilidade)
  COMMIT="$(curl -fsS "${ACERVO_URL_PRODUCAO:-http://localhost:8080}/actuator/info" | sed -E 's/.*"full":"([0-9a-f]{40})".*/\1/')"
fi
git tag -a "v${VERSAO}" "$COMMIT" -m "Release ${VERSAO} - linha de base de produção"
echo "Linha de base v${VERSAO} criada em ${COMMIT}. Publique com: git push origin v${VERSAO}"
