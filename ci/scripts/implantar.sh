#!/usr/bin/env bash
# Implanta a versão rotulada pelo pipeline no ambiente informado.
# Uso: ci/scripts/implantar.sh <homologacao|producao>
source "$(dirname "$0")/comum.sh"
AMBIENTE="${1:?informe o ambiente: homologacao | producao}"
exec "$(dirname "$0")/../../deploy/deploy.sh" "$AMBIENTE" "$VERSAO"
