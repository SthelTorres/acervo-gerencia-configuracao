#!/usr/bin/env bash
# ETAPA DE COMMIT - Empacotamento: gera a imagem Docker a partir do JAR já testado
# e publica no repositório de artefatos (Docker Registry).
source "$(dirname "$0")/comum.sh"

JAR="target/acervo-${VERSAO}.jar"
# O GoCD entrega o artefato buscado (fetch) em target/jar/
if [[ -d target/jar ]]; then cp target/jar/* target/; fi
[[ -f "$JAR" ]] || { erro "JAR $JAR não encontrado (deveria vir do estágio de compilação)"; exit 1; }
if [[ -f "${JAR}.sha256" ]]; then
  log "Conferindo integridade do artefato recebido"
  sha256sum -c "${JAR}.sha256"
fi

log "Construindo imagem ${IMAGEM}:${VERSAO}"
docker build \
  --build-arg VERSAO="${VERSAO}" \
  --build-arg COMMIT="${COMMIT}" \
  --build-arg DATA_BUILD="$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  -t "${IMAGEM}:${VERSAO}" .

log "Publicando no registry ${REGISTRY}"
docker push "${IMAGEM}:${VERSAO}"

DIGEST="$(docker inspect --format '{{index .RepoDigests 0}}' "${IMAGEM}:${VERSAO}")"
mkdir -p target
cat > target/imagem.properties <<PROPS
versao=${VERSAO}
commit=${COMMIT}
imagem=${IMAGEM}:${VERSAO}
digest=${DIGEST}
pipeline=${GO_PIPELINE_NAME:-local}/${GO_PIPELINE_COUNTER:-0}
PROPS
cat target/imagem.properties
