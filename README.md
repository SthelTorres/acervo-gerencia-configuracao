# 📚 Acervo — Sistema de Gestão de Biblioteca

Trabalho Prático de **Gerência de Configuração e Evolução de Software**.
O Acervo é uma aplicação web (API REST + interface) para cadastro de livros e leitores e controle de empréstimos,
entregue por um **pipeline de implantação no GoCD** com Integração Contínua, Entrega Contínua e implantação em contêineres Docker.

| | |
|---|---|
| **Linguagem / Framework** | Java 17 · Spring Boot 4.1 · Spring Data JPA · Flyway |
| **Build** | Apache Maven 3.10 (Maven Wrapper) |
| **Testes** | JUnit 6 · Mockito · AssertJ · Spring Boot Test · Cucumber 8 (BDD) · REST Assured · JaCoCo |
| **CI/CD** | GoCD 26.2 (Pipeline as Code em `ci.gocd.yaml`) |
| **Empacotamento / Implantação** | Docker · Docker Compose · Docker Registry · PostgreSQL 17 |

## Equipe

| Integrante | Papel no Plano de GC |
|---|---|
| Giuliano Libanio Beghini Percope | Gerente de Projeto · Presidente do CCB |
| Sthel Felipe Torres | Gerente de Configuração |
| Arthur Araujo Mendonça | Engenheiro DevOps / Integrador |
| Víctor Gabriel Cruz Pereira | Arquiteto de Software / Desenvolvedor |
| Nayarisson Natan Oliveira Moroni | Desenvolvedor |
| Vitor Hugo Dutra Marinho | Analista de Qualidade (testes) |

## Funcionalidades

* Livros: cadastro com validação de **ISBN-10/13**, pesquisa por título/autor/categoria, controle de exemplares.
* Leitores: categorias **Aluno** (3 livros/14 dias), **Professor** (5/30) e **Visitante** (1/7).
* Empréstimos: retirada, devolução com **multa de R$ 2,00/dia** (teto R$ 50,00), renovação (1×), bloqueio por atraso.
* Painel de indicadores, documentação **Swagger** (`/swagger-ui.html`), **health check** e **versão/commit** em execução (`/actuator/info`).

## Estrutura do repositório

```
acervo/
├── ci.gocd.yaml               # Pipeline de implantação (GoCD - Pipeline as Code)
├── ci/scripts/                # Scripts executados por cada etapa do pipeline
├── deploy/                    # Script de implantação, smoke test, rollback e configuração por ambiente
│   ├── deploy.sh
│   ├── docker-compose.yml
│   └── env/{homologacao,producao}.env
├── infra/gocd/                # Infraestrutura de CI como código (servidor, agentes, registry)
├── Dockerfile                 # Imagem de execução (camadas, usuário não-root, rótulos OCI)
├── pom.xml                    # Build Maven (versão CI-friendly ${revision})
├── src/main/java/...          # Código-fonte (domínio, serviços, API REST)
├── src/main/resources/db/     # Migrações versionadas do banco (Flyway)
├── src/test/java/...          # Testes de unidade (*Test) e integração (*IT)
├── acceptance-tests/          # Testes de aceitação BDD (Cucumber, caixa-preta)
├── scripts/                   # Utilitários de GC (baseline, notificação do GoCD)
├── docs/                      # Plano de Gerenciamento de Configuração
└── .github/                   # Templates de Solicitação de Mudança, PR e CODEOWNERS
```

## Executando localmente

Pré-requisitos: JDK 17+ e Docker.

```bash
./mvnw verify                                   # compila + testes de unidade e integração + cobertura
java -jar target/acervo-1.0.0-SNAPSHOT.jar      # http://localhost:8080 (H2 em memória)
./mvnw -f acceptance-tests/pom.xml test -Dacervo.baseUrl=http://localhost:8080   # aceitação
```

## Pipeline de implantação (GoCD)

```
 commit na main
      │
      ▼
┌──────────────────────────┐   ┌──────────────────────────┐   ┌──────────────────────────┐
│ acervo-commit            │   │ acervo-aceitacao         │   │ acervo-producao          │
│ 1. compilacao (JAR)      │──►│ 1. implantacao-          │──►│ 1. implantacao-producao  │
│ 2. testes                │   │    homologacao (:8081)   │   │    (aprovação manual)    │
│    ├ unitarios  ┐paralelo│   │ 2. testes-aceitacao      │   │ 2. publicacao-release    │
│    └ integracao ┘        │   │    (Cucumber + NFR)      │   │    (tag :producao, notas)│
│ 3. empacotamento (imagem)│   └──────────────────────────┘   └──────────────────────────┘
└──────────────────────────┘
```

### Subindo o GoCD

```bash
cd infra/gocd
cp .env.example .env            # opcional
docker compose up -d --build    # servidor + 2 agentes + registry (primeira vez ~3 min)
```

Abra **http://localhost:8153/go**. O servidor já nasce configurado com este repositório como
*Config Repository*: as três pipelines aparecem sozinhas e a primeira execução começa automaticamente.
Cada novo commit na `main` dispara o pipeline (verificação a cada minuto; para disparo imediato
instale o hook: `ln -sf ../../scripts/notificar-gocd.sh .git/hooks/post-commit`).

| Serviço | URL |
|---|---|
| GoCD | http://localhost:8153/go |
| Acervo — homologação | http://localhost:8081 |
| Acervo — produção | http://localhost:8080 |
| Registry (imagens) | http://localhost:5050/v2/acervo/tags/list |

### Implantação manual / rollback

```bash
deploy/deploy.sh producao 1.0.12     # implanta uma versão publicada no registry
deploy/rollback.sh producao 1.0.11   # retorna para a versão anterior
```

## Convenções de Gerência de Configuração

Branching (GitHub Flow), Conventional Commits, SemVer, linhas de base e controle de mudanças:
veja [CONTRIBUTING.md](CONTRIBUTING.md) e o Plano de Gerenciamento de Configuração em `docs/`.
