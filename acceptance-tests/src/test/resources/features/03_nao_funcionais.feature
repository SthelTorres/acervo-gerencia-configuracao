# language: pt
@nao-funcional
Funcionalidade: Requisitos não funcionais do serviço implantado
  Para garantir a qualidade do que é entregue aos usuários
  O ambiente implantado deve atender requisitos de disponibilidade, desempenho e rastreabilidade

  Cenário: Disponibilidade da aplicação e do banco de dados
    Quando eu consulto o health check da aplicação
    Então o status da aplicação deve ser "UP"
    E o status do banco de dados deve ser "UP"

  Cenário: A versão implantada é exatamente o artefato produzido pelo pipeline
    Quando eu consulto as informações de build
    Então a versão implantada deve ser a versão esperada pelo pipeline
    E o commit implantado deve ser o commit esperado pelo pipeline

  Cenário: Tempo de resposta da pesquisa no acervo
    Quando eu realizo 50 pesquisas consecutivas no acervo
    Então nenhuma requisição deve falhar
    E o tempo médio de resposta deve ser inferior a 300 milissegundos
    E o percentil 95 do tempo de resposta deve ser inferior a 800 milissegundos

  Cenário: Interface web e documentação da API disponíveis aos usuários
    Quando eu acesso a página inicial
    Então a página deve ser exibida com o título "Acervo"
    E a documentação da API deve estar publicada
