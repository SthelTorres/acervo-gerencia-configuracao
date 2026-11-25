#!/usr/bin/env bash
# Retorna um ambiente para uma versão anterior já publicada no registry.
# Uso: deploy/rollback.sh <homologacao|producao> <versao-anterior>
# Alternativa no GoCD: reexecutar ("Rerun") o estágio de implantação de uma execução anterior do pipeline.
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AMBIENTE="${1:?Uso: rollback.sh <ambiente> <versao>}"
VERSAO="${2:?Uso: rollback.sh <ambiente> <versao>}"
echo "[rollback] Retornando ${AMBIENTE} para a versão ${VERSAO}"
ACERVO_AMBIENTE_EFEMERO=false exec "${DIR}/deploy.sh" "$AMBIENTE" "$VERSAO"
