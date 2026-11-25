package br.pucminas.gcs.acervo.aceitacao;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;

public class PassosNaoFuncionais {

    private final ApiCliente api;
    private Response resposta;
    private final List<Long> temposMs = new ArrayList<>();
    private int falhas;

    public PassosNaoFuncionais(ApiCliente api) {
        this.api = api;
    }

    @Quando("eu consulto o health check da aplicação")
    public void consultarHealth() {
        resposta = api.get("/actuator/health");
    }

    @Entao("o status da aplicação deve ser {string}")
    public void statusAplicacao(String status) {
        assertThat(resposta.statusCode()).isEqualTo(200);
        assertThat(resposta.jsonPath().getString("status")).isEqualTo(status);
    }

    @Entao("o status do banco de dados deve ser {string}")
    public void statusBanco(String status) {
        assertThat(resposta.jsonPath().getString("components.db.status")).isEqualTo(status);
    }

    @Quando("eu consulto as informações de build")
    public void consultarInfo() {
        resposta = api.get("/actuator/info");
        assertThat(resposta.statusCode()).isEqualTo(200);
    }

    @Entao("a versão implantada deve ser a versão esperada pelo pipeline")
    public void versaoEsperada() {
        String versao = resposta.jsonPath().getString("build.version");
        assertThat(versao).as("build.version").isNotBlank();
        Ambiente.versaoEsperada().ifPresentOrElse(
                esperada -> assertThat(versao).isEqualTo(esperada),
                () -> System.out.println("[aceitação] ACERVO_VERSAO_ESPERADA não informada; versão implantada: " + versao));
    }

    @Entao("o commit implantado deve ser o commit esperado pelo pipeline")
    public void commitEsperado() {
        JsonPath json = resposta.jsonPath();
        String commit = json.getString("git.commit.id.full");
        if (commit == null) {
            commit = json.getString("git.commit.id");
        }
        String implantado = commit;
        Ambiente.commitEsperado().ifPresentOrElse(
                esperado -> assertThat(implantado).as("commit implantado").isEqualTo(esperado),
                () -> System.out.println("[aceitação] ACERVO_COMMIT_ESPERADO não informado; commit implantado: " + implantado));
    }

    @Quando("eu realizo {int} pesquisas consecutivas no acervo")
    public void realizarPesquisas(int quantidade) {
        for (int i = 0; i < 10; i++) {
            api.get("/api/livros"); // aquecimento da JVM antes de medir
        }
        for (int i = 0; i < quantidade; i++) {
            long inicio = System.nanoTime();
            Response r = api.get("/api/livros?termo={t}", i % 2 == 0 ? "a" : "software");
            temposMs.add((System.nanoTime() - inicio) / 1_000_000);
            if (r.statusCode() != 200) {
                falhas++;
            }
        }
        System.out.printf("[aceitação] %d requisições: média=%.1f ms, p95=%d ms, máx=%d ms%n", quantidade, media(),
                percentil(95), temposMs.stream().mapToLong(Long::longValue).max().orElse(0));
    }

    @Entao("nenhuma requisição deve falhar")
    public void nenhumaFalha() {
        assertThat(falhas).isZero();
    }

    @Entao("o tempo médio de resposta deve ser inferior a {int} milissegundos")
    public void tempoMedio(int limite) {
        assertThat(media()).isLessThan(limite);
    }

    @Entao("o percentil {int} do tempo de resposta deve ser inferior a {int} milissegundos")
    public void tempoPercentil(int percentil, int limite) {
        assertThat(percentil(percentil)).isLessThan(limite);
    }

    @Quando("eu acesso a página inicial")
    public void acessarPaginaInicial() {
        resposta = given().baseUri(Ambiente.baseUrl()).get("/");
    }

    @Entao("a página deve ser exibida com o título {string}")
    public void paginaComTitulo(String titulo) {
        assertThat(resposta.statusCode()).isEqualTo(200);
        assertThat(resposta.contentType()).contains("text/html");
        assertThat(resposta.asString()).contains("<title>" + titulo);
    }

    @Entao("a documentação da API deve estar publicada")
    public void documentacaoPublicada() {
        Response docs = api.get("/v3/api-docs");
        assertThat(docs.statusCode()).isEqualTo(200);
        assertThat(docs.jsonPath().getString("openapi")).startsWith("3.");
        assertThat(given().baseUri(Ambiente.baseUrl()).get("/swagger-ui/index.html").statusCode()).isEqualTo(200);
    }

    private double media() {
        return temposMs.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private long percentil(int p) {
        List<Long> ordenados = temposMs.stream().sorted().toList();
        if (ordenados.isEmpty()) {
            return 0;
        }
        int indice = (int) Math.ceil(p / 100.0 * ordenados.size()) - 1;
        return ordenados.get(Math.max(0, indice));
    }
}
