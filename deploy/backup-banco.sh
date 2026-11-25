#!/usr/bin/env bash
# Backup do banco de dados de um ambiente (padrão: produção) com pg_dump.
# Mantém as N cópias mais recentes (padrão: 7). Agendável no cron, ex.:
#   0 2 * * *  /caminho/acervo/deploy/backup-banco.sh producao
# Restauração:
#   gunzip -c backups/<arquivo>.sql.gz | docker compose -p acervo-producao exec -T banco psql -U acervo acervo
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AMBIENTE="${1:-producao}"
DESTINO="${ACERVO_BACKUP_DIR:-${DIR}/../backups}"
RETENCAO="${ACERVO_BACKUP_RETENCAO:-7}"

mkdir -p "$DESTINO"
ARQUIVO="${DESTINO}/acervo-${AMBIENTE}-$(date +%Y%m%d-%H%M%S).sql.gz"

docker compose -p "acervo-${AMBIENTE}" -f "${DIR}/docker-compose.yml" exec -T banco \
  pg_dump -U acervo --clean --if-exists acervo | gzip > "$ARQUIVO"
echo "[backup] ${AMBIENTE}: $(du -h "$ARQUIVO" | cut -f1) gravados em ${ARQUIVO}"

# Retenção: remove os backups mais antigos além do limite
ls -1t "${DESTINO}"/acervo-"${AMBIENTE}"-*.sql.gz 2>/dev/null | tail -n +$((RETENCAO + 1)) | xargs -r rm -f
echo "[backup] mantidas as ${RETENCAO} cópias mais recentes"
