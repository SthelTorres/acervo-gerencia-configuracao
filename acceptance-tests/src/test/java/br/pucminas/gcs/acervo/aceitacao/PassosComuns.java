package br.pucminas.gcs.acervo.aceitacao;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class PassosComuns {

    private final Contexto ctx;
    private final ApiCliente api;

    public PassosComuns(Contexto ctx, ApiCliente api) {
        this.ctx = ctx;
        this.api = api;
    }

    @Before
    public void configurar() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Dado("que o sistema Acervo está disponível")
    public void sistemaDisponivel() throws InterruptedException {
        int tentativas = 0;
        while (true) {
            try {
                if (api.get("/actuator/health").statusCode() == 200) {
                    return;
                }
            } catch (Exception ignorada) {
                // ambiente ainda subindo
            }
            if (++tentativas >= 30) {
                throw new AssertionError("O Acervo não respondeu em " + Ambiente.baseUrl());
            }
            Thread.sleep(1000);
        }
    }

    @Dado("que existe um livro com {int} exemplar(es)")
    public void existeLivro(int exemplares) {
        ctx.isbn = GeradorDados.isbnValido();
        Response r = api.cadastrarLivro(ctx.isbn, "Livro de aceitação " + ctx.isbn, "Autor de Teste", exemplares);
        assertThat(r.statusCode()).as(r.asString()).isEqualTo(201);
        ctx.livroId = r.jsonPath().getLong("id");
    }

    @Dado("que existe um leitor do tipo {string}")
    public void existeLeitor(String tipo) {
        Response r = api.cadastrarLeitor(tipo);
        assertThat(r.statusCode()).as(r.asString()).isEqualTo(201);
        ctx.leitorId = r.jsonPath().getLong("id");
    }

    @Entao("a operação deve ser recusada com o status {int} e a mensagem {string}")
    public void operacaoRecusada(int status, String mensagem) {
        assertThat(ctx.ultimaResposta.statusCode()).as(ctx.ultimaResposta.asString()).isEqualTo(status);
        assertThat(ctx.ultimaResposta.jsonPath().getString("detail")).contains(mensagem);
    }
}
