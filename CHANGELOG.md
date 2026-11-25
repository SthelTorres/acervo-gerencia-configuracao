# Changelog

Todas as mudanças relevantes deste projeto são documentadas aqui.
Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e versionamento [SemVer](https://semver.org/lang/pt-BR/).

## [Não publicado]

## [1.0.0] - 2026-11-25
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
