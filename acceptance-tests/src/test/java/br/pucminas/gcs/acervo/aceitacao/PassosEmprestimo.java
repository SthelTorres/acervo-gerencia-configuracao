package br.pucminas.gcs.acervo.aceitacao;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PassosEmprestimo {

    private final Contexto ctx;
    private final ApiCliente api;

    public PassosEmprestimo(Contexto ctx, ApiCliente api) {
        this.ctx = ctx;
        this.api = api;
    }

    @Quando("o leitor pega o livro emprestado")
    public void pegarEmprestado() {
        ctx.ultimaResposta = api.emprestar(ctx.livroId, ctx.leitorId);
        if (ctx.ultimaResposta.statusCode() == 201) {
            ctx.emprestimoId = ctx.ultimaResposta.jsonPath().getLong("id");
        }
    }

    @Dado("que o leitor pegou o livro emprestado")
    public void leitorPegouEmprestado() {
        pegarEmprestado();
        assertThat(ctx.ultimaResposta.statusCode()).as(ctx.ultimaResposta.asString()).isEqualTo(201);
    }

    @Entao("o empréstimo deve ser registrado com devolução prevista em {int} dias")
    public void emprestimoRegistrado(int dias) {
        assertThat(ctx.ultimaResposta.statusCode()).as(ctx.ultimaResposta.asString()).isEqualTo(201);
        LocalDate retirada = LocalDate.parse(ctx.ultimaResposta.jsonPath().getString("dataEmprestimo"));
        LocalDate prevista = LocalDate.parse(ctx.ultimaResposta.jsonPath().getString("dataPrevistaDevolucao"));
        assertThat(ChronoUnit.DAYS.between(retirada, prevista)).isEqualTo(dias);
        assertThat(ctx.ultimaResposta.jsonPath().getString("status")).isEqualTo("ATIVO");
    }

    @Quando("o leitor devolve o livro")
    public void devolver() {
        ctx.ultimaResposta = api.post("/api/emprestimos/{id}/devolucao", null, ctx.emprestimoId);
    }

    @Entao("a devolução deve ser registrada sem multa")
    public void devolucaoSemMulta() {
        assertThat(ctx.ultimaResposta.statusCode()).as(ctx.ultimaResposta.asString()).isEqualTo(200);
        assertThat(ctx.ultimaResposta.jsonPath().getString("status")).isEqualTo("DEVOLVIDO");
        assertThat(ctx.ultimaResposta.jsonPath().getDouble("multa")).isZero();
    }

    @Dado("que existe um leitor do tipo {string} com {int} empréstimos ativos")
    public void leitorComEmprestimos(String tipo, int quantidade) {
        Response leitor = api.cadastrarLeitor(tipo);
        assertThat(leitor.statusCode()).isEqualTo(201);
        ctx.leitorId = leitor.jsonPath().getLong("id");
        for (int i = 0; i < quantidade; i++) {
            Response livro = api.cadastrarLivro(GeradorDados.isbnValido(), "Livro " + i, "Autor", 1);
            assertThat(livro.statusCode()).isEqualTo(201);
            Response emprestimo = api.emprestar(livro.jsonPath().getLong("id"), ctx.leitorId);
            assertThat(emprestimo.statusCode()).as(emprestimo.asString()).isEqualTo(201);
        }
    }

    @Quando("o leitor tenta pegar mais um livro emprestado")
    public void pegarMaisUm() {
        Response livro = api.cadastrarLivro(GeradorDados.isbnValido(), "Livro extra", "Autor", 1);
        assertThat(livro.statusCode()).isEqualTo(201);
        ctx.ultimaResposta = api.emprestar(livro.jsonPath().getLong("id"), ctx.leitorId);
    }
}
