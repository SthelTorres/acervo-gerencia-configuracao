-- V2: dados de exemplo para demonstração (desabilitável removendo classpath:db/exemplo de spring.flyway.locations)
INSERT INTO livro (isbn, titulo, autor, ano_publicacao, categoria, exemplares_totais, exemplares_disponiveis) VALUES
('9780321601919', 'Continuous Delivery', 'Jez Humble; David Farley', 2010, 'DevOps', 3, 3),
('9781942788003', 'The DevOps Handbook', 'Gene Kim; Jez Humble; Patrick Debois; John Willis', 2016, 'DevOps', 2, 2),
('9781942788331', 'Accelerate', 'Nicole Forsgren; Jez Humble; Gene Kim', 2018, 'DevOps', 2, 2),
('9780201741179', 'Software Configuration Management Patterns', 'Stephen P. Berczuk; Brad Appleton', 2002, 'Gerência de Configuração', 2, 2),
('9781484200773', 'Pro Git', 'Scott Chacon; Ben Straub', 2014, 'Gerência de Configuração', 4, 4),
('9780132350884', 'Clean Code', 'Robert C. Martin', 2008, 'Engenharia de Software', 3, 3),
('9780201633610', 'Design Patterns', 'Erich Gamma; Richard Helm; Ralph Johnson; John Vlissides', 1994, 'Engenharia de Software', 2, 2),
('9780321125217', 'Domain-Driven Design', 'Eric Evans', 2003, 'Engenharia de Software', 1, 1),
('9788543024974', 'Engenharia de Software', 'Ian Sommerville', 2018, 'Engenharia de Software', 5, 5),
('9780134757599', 'Refactoring', 'Martin Fowler', 2018, 'Engenharia de Software', 2, 2);

INSERT INTO leitor (nome, email, tipo, ativo) VALUES
('Ana Beatriz Souza', 'ana.souza@exemplo.edu.br', 'ALUNO', TRUE),
('Carlos Henrique Lima', 'carlos.lima@exemplo.edu.br', 'PROFESSOR', TRUE),
('Mariana Alves', 'mariana.alves@exemplo.edu.br', 'ALUNO', TRUE),
('Visitante Teste', 'visitante@exemplo.com', 'VISITANTE', TRUE);
