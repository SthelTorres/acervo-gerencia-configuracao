#!/usr/bin/env bash
# ETAPA DE LANÇAMENTO - promove a imagem aprovada (tag "producao") e gera as notas de release.
source "$(dirname "$0")/comum.sh"

log "Promovendo ${IMAGEM}:${VERSAO} -> ${IMAGEM}:producao"
docker pull "${IMAGEM}:${VERSAO}" >/dev/null
docker tag "${IMAGEM}:${VERSAO}" "${IMAGEM}:producao"
docker push "${IMAGEM}:producao"

mkdir -p target/release
NOTAS="target/release/RELEASE_NOTES.md"
{
  echo "# Acervo ${VERSAO}"
  echo
  echo "- **Data da implantação:** $(date '+%d/%m/%Y %H:%M:%S %Z')"
  echo "- **Commit:** ${COMMIT}"
  echo "- **Imagem:** ${IMAGEM}:${VERSAO}"
  echo "- **Pipeline:** ${GO_PIPELINE_NAME:-local} #${GO_PIPELINE_COUNTER:-0} (aprovado por ${GO_TRIGGER_USER:-n/d})"
  echo
  echo "## Alterações incluídas (últimos commits da linha principal)"
  echo
  git log -15 --no-merges --pretty='- %h %s (%an, %ad)' --date=short "${COMMIT}" 2>/dev/null || echo "- (histórico indisponível)"
  echo
  echo "## Itens de configuração"
  echo
  echo "| Item | Versão |"
  echo "|---|---|"
  echo "| Aplicação (JAR/imagem) | ${VERSAO} |"
  echo "| Esquema do banco (Flyway) | $(ls src/main/resources/db/*/V*__*.sql | sed -E 's/.*\/V([0-9]+)__.*/\1/' | sort -n | tail -1) |"
  echo "| Java (runtime) | 17 (eclipse-temurin) |"
  echo "| PostgreSQL | 17 |"
} > "$NOTAS"
cat "$NOTAS"
