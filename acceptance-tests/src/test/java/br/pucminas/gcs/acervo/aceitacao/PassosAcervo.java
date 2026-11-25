package br.pucminas.gcs.acervo.aceitacao;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import java.util.List;

public class PassosAcervo {

    private final Contexto ctx;
    private final ApiCliente api;

    public PassosAcervo(Contexto ctx, ApiCliente api) {
        this.ctx = ctx;
        this.api = api;
    }

    @Quando("eu cadastro o livro {string} de {string} com {int} exemplares")
    public void cadastrarLivro(String titulo, String autor, int exemplares) {
        ctx.isbn = GeradorDados.isbnValido();
        ctx.ultimaResposta = api.cadastrarLivro(ctx.isbn, titulo + " [" + ctx.isbn + "]", autor, exemplares);
    }

    @Entao("o livro deve ser criado com sucesso")
    public void livroCriado() {
        assertThat(ctx.ultimaResposta.statusCode()).as(ctx.ultimaResposta.asString()).isEqualTo(201);
        assertThat(ctx.ultimaResposta.header("Location")).startsWith("/api/livros/");
        ctx.livroId = ctx.ultimaResposta.jsonPath().getLong("id");
    }

    @Entao("o livro deve aparecer na pesquisa por {string}")
    public void livroNaPesquisa(String termo) {
        Response r = api.get("/api/livros?termo={termo}", termo);
        assertThat(r.statusCode()).isEqualTo(200);
        List<String> isbns = r.jsonPath().getList("isbn");
        assertThat(isbns).contains(ctx.isbn);
    }

    @Entao("o livro deve ter {int} exemplares disponíveis")
    public void exemplaresDisponiveis(int quantidade) {
        Response r = api.get("/api/livros/{id}", ctx.livroId);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getInt("exemplaresDisponiveis")).isEqualTo(quantidade);
    }

    @Quando("eu tento cadastrar um livro com o ISBN {string}")
    public void cadastrarComIsbn(String isbn) {
        ctx.ultimaResposta = api.cadastrarLivro(isbn, "Livro inválido", "Autor", 1);
    }

    @Quando("eu tento cadastrar outro livro com o mesmo ISBN")
    public void cadastrarIsbnDuplicado() {
        ctx.ultimaResposta = api.cadastrarLivro(ctx.isbn, "Outro título", "Outro autor", 1);
    }
}
