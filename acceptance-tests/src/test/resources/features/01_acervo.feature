# language: pt
@funcional
Funcionalidade: Gestão do acervo de livros
  Como bibliotecário
  Quero cadastrar e consultar livros
  Para manter o acervo da biblioteca atualizado

  Contexto:
    Dado que o sistema Acervo está disponível

  Cenário: Cadastrar um novo livro com ISBN válido
    Quando eu cadastro o livro "Release It!" de "Michael T. Nygard" com 2 exemplares
    Então o livro deve ser criado com sucesso
    E o livro deve aparecer na pesquisa por "Release It"
    E o livro deve ter 2 exemplares disponíveis

  Cenário: Rejeitar livro com ISBN inválido
    Quando eu tento cadastrar um livro com o ISBN "1234567890"
    Então a operação deve ser recusada com o status 422 e a mensagem "ISBN inválido"

  Cenário: Impedir o cadastro de ISBN duplicado
    Dado que existe um livro com 1 exemplar
    Quando eu tento cadastrar outro livro com o mesmo ISBN
    Então a operação deve ser recusada com o status 409 e a mensagem "Já existe um livro"
