#!/usr/bin/env bash
# Avisa o GoCD que há um novo commit (dispara o pipeline imediatamente, sem esperar o polling).
# Instale como hook: ln -sf ../../scripts/notificar-gocd.sh .git/hooks/post-commit
# Com GitHub, o equivalente é um webhook para http://<gocd>/go/api/webhooks/github/notify
curl -fsS -X POST "${GOCD_URL:-http://localhost:8153}/go/api/admin/materials/git/notify" \
  -H 'Accept: application/vnd.go.cd.v2+json' -H 'Content-Type: application/json' \
  -d "{\"repository_url\": \"${GOCD_REPO_URL:-/repos/acervo}\"}" >/dev/null 2>&1 \
  && echo "[gocd] pipeline notificado" || echo "[gocd] servidor GoCD indisponível (o polling detectará o commit)"
exit 0
