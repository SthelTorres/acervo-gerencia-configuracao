# Guia de Contribuição e Convenções de Gerência de Configuração

Este guia resume, de forma operacional, as regras definidas no **Plano de Gerenciamento de Configuração** (`docs/`).

## 1. Fluxo de trabalho (GitHub Flow)

```
main ●────●─────────●────────●──────► (sempre implantável; protegida)
          \        /  \      /
 feature   ●──●──●     ●──●─●   (branches curtas, 1 a 3 dias)
```

1. Toda mudança nasce de uma **Solicitação de Mudança (SM)** ou **Relato de Defeito** registrado como *issue*.
2. Após aprovação na triagem (CCB), crie a branch a partir da `main` atualizada.
3. Abra um *Pull Request*; o merge exige revisão aprovada e o build verde.
4. O merge na `main` dispara automaticamente o pipeline no GoCD.

### Nomenclatura de branches
| Prefixo | Uso | Exemplo |
|---|---|---|
| `feature/` | nova funcionalidade | `feature/12-renovacao-emprestimo` |
| `fix/` | correção de defeito | `fix/27-calculo-multa` |
| `hotfix/` | correção urgente em produção | `hotfix/31-erro-login` |
| `docs/` | documentação | `docs/atualiza-pgc` |
| `ci/` | pipeline e infraestrutura | `ci/gate-cobertura` |

## 2. Mensagens de commit (Conventional Commits)

```
<tipo>(escopo opcional): descrição no imperativo

[corpo opcional]
Refs: #<número da issue>
```
Tipos: `feat`, `fix`, `test`, `docs`, `refactor`, `ci`, `build`, `chore`, `perf`.

## 3. Versionamento (SemVer)

* **Versão de build**: `MAJOR.MINOR.<contador do pipeline>` (ex.: `1.0.42`) — gerada pelo GoCD e gravada no JAR, na imagem Docker (`org.opencontainers.image.version`) e exposta em `/actuator/info`.
* **Linha de base de release**: tag Git anotada `vMAJOR.MINOR.PATCH` criada pelo Gerente de Configuração após cada implantação em produção (`scripts/criar-baseline.sh`).
* Mudanças incompatíveis incrementam MAJOR; novas funcionalidades, MINOR (o contador do pipeline é o PATCH).

## 4. Regras para itens de configuração
* **Banco de dados**: toda mudança de esquema é uma nova migração `V<n>__descricao.sql`. Migrações aplicadas são imutáveis.
* **Configuração por ambiente**: nunca codificar valores de ambiente no código; use `application.yml` + variáveis de ambiente (`deploy/env/*.env`).
* **Segredos**: nunca versionar senhas reais; usar variáveis seguras do GoCD.
* **Dependências**: versões sempre fixadas (`pom.xml`, imagens Docker com tag explícita).

## 5. Antes de abrir o Pull Request
```bash
./mvnw verify                      # unidade + integração + cobertura >= 80%
java -jar target/acervo-*.jar &     # sobe localmente
./mvnw -f acceptance-tests/pom.xml test -Dacervo.baseUrl=http://localhost:8080
```
