# Changelog

Todas as mudanças relevantes deste projeto são documentadas aqui.
Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e versionamento [SemVer](https://semver.org/lang/pt-BR/).

## [Não publicado]
### Adicionado
- Plano de Gerenciamento de Configuração v1.0 (PGC-ACV-001) em `docs/` (PDF e DOCX).
### Alterado
- Imagem dos agentes GoCD com `tzdata` (datas locais corretas nos logs e notas de release).
- Identidade e área de trabalho dos agentes persistidas em volume (`godata-agente-N`),
  evitando registros duplicados ao recriar os contêineres.

## [1.0.3] - 2026-11-25
### Adicionado
- Script `deploy/backup-banco.sh` com pg_dump compactado e retenção das 7 últimas cópias.
### Alterado
- Agentes e servidor GoCD no fuso America/Sao_Paulo (horários corretos nas notas de release).
- O artefato deixa de registrar `git.build.time` (sempre fixo por causa do build reproduzível).

## [1.0.2] - 2026-11-25
### Corrigido
- O artefato passa a registrar o commit completo (`git.commit.id`), permitindo a auditoria física
  automática no teste de aceitação de rastreabilidade (falha detectada na execução 1.0.1 do pipeline).

## [1.0.1] - 2026-11-25
### Adicionado
- Cadastro, pesquisa, atualização e remoção de livros com validação de ISBN-10/13.
- Cadastro de leitores (aluno, professor, visitante) com limites e prazos por categoria.
- Empréstimo, devolução com cálculo de multa por atraso e renovação.
- Painel com indicadores do acervo e interface web.
- Documentação OpenAPI (Swagger UI) e endpoints de saúde/versão (Actuator).
- Versionamento do banco de dados com Flyway (H2 em desenvolvimento, PostgreSQL em homologação/produção).
- Testes de unidade (JUnit 6 + Mockito), integração (Spring Boot Test) e aceitação (Cucumber BDD).
- Pipeline de implantação no GoCD (Etapa de Commit, Teste de Aceitação e Lançamento) como código.
- Implantação em contêineres Docker com smoke test e rollback automático.
