#!/bin/bash
# Executado pelo entrypoint oficial do GoCD antes de iniciar o servidor.
# No primeiro boot (sem configuração), copia o modelo versionado preenchendo os marcadores.
set -e
DESTINO=/godata/config/cruise-config.xml
if [ -f "$DESTINO" ]; then
  echo "[acervo] cruise-config.xml já existe - mantendo configuração atual"
  exit 0
fi
uuid() { cat /proc/sys/kernel/random/uuid; }
mkdir -p /godata/config
sed -e "s|__CHAVE_AGENTES__|${GOCD_AGENT_AUTO_REGISTER_KEY:?defina GOCD_AGENT_AUTO_REGISTER_KEY}|" \
    -e "s|__SEGREDO_WEBHOOK__|${GOCD_WEBHOOK_SECRET:-$(uuid)}|" \
    -e "s|__ID_SERVIDOR__|$(uuid)|" \
    -e "s|__CHAVE_TOKENS__|$(uuid)|" \
    -e "s|__URL_REPOSITORIO__|${ACERVO_REPO_URL:-/repos/acervo}|" \
    /seed/cruise-config.modelo.xml > "$DESTINO"
echo "[acervo] cruise-config.xml inicial criado (config repo: ${ACERVO_REPO_URL:-/repos/acervo})"
