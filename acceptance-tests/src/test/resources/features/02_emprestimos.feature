# language: pt
@funcional
Funcionalidade: Empréstimo e devolução de livros
  Como bibliotecário
  Quero registrar empréstimos e devoluções
  Para controlar a circulação dos exemplares do acervo

  Contexto:
    Dado que o sistema Acervo está disponível
    E que existe um livro com 1 exemplar
    E que existe um leitor do tipo "ALUNO"

  Cenário: Empréstimo retira o exemplar do acervo
    Quando o leitor pega o livro emprestado
    Então o empréstimo deve ser registrado com devolução prevista em 14 dias
    E o livro deve ter 0 exemplares disponíveis

  Cenário: Devolução no prazo não gera multa
    Dado que o leitor pegou o livro emprestado
    Quando o leitor devolve o livro
    Então a devolução deve ser registrada sem multa
    E o livro deve ter 1 exemplares disponíveis

  Cenário: Livro sem exemplares disponíveis não pode ser emprestado
    Dado que o leitor pegou o livro emprestado
    E que existe um leitor do tipo "PROFESSOR"
    Quando o leitor pega o livro emprestado
    Então a operação deve ser recusada com o status 422 e a mensagem "Não há exemplares"

  Esquema do Cenário: Limite de empréstimos simultâneos para <tipo>
    Dado que existe um leitor do tipo "<tipo>" com <limite> empréstimos ativos
    Quando o leitor tenta pegar mais um livro emprestado
    Então a operação deve ser recusada com o status 422 e a mensagem "Limite de <limite>"

    Exemplos:
      | tipo      | limite |
      | ALUNO     | 3      |
      | VISITANTE | 1      |
