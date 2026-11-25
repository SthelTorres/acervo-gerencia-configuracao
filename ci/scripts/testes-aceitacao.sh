#!/usr/bin/env bash
# ETAPA DE TESTE DE ACEITAÇÃO - cenários BDD (Cucumber) contra o ambiente de homologação.
source "$(dirname "$0")/comum.sh"
source "$(dirname "$0")/../../deploy/env/homologacao.env"

URL="http://${ACERVO_HOST:-localhost}:${ACERVO_PORTA}"
log "Executando testes de aceitação contra ${URL} (versão esperada ${VERSAO})"
export ACERVO_BASE_URL="$URL" ACERVO_VERSAO_ESPERADA="$VERSAO" ACERVO_COMMIT_ESPERADO="$COMMIT"
$MVN -f acceptance-tests/pom.xml clean test -Dacervo.baseUrl="$URL"
