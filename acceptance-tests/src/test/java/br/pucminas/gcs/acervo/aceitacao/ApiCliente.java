package br.pucminas.gcs.acervo.aceitacao;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

/**
 * Cliente HTTP mínimo para a API do Acervo usado pelos passos.
 */
public class ApiCliente {

    private RequestSpecification req() {
        return given().baseUri(Ambiente.baseUrl()).contentType(ContentType.JSON).accept(ContentType.JSON);
    }

    public Response get(String caminho, Object... parametros) {
        return req().get(caminho, parametros);
    }

    public Response post(String caminho, Object corpo, Object... parametros) {
        RequestSpecification r = req();
        if (corpo != null) {
            r = r.body(corpo);
        }
        return r.post(caminho, parametros);
    }

    public Response cadastrarLivro(String isbn, String titulo, String autor, int exemplares) {
        return post("/api/livros", Map.of("isbn", isbn, "titulo", titulo, "autor", autor,
                "categoria", "Aceitação", "exemplares", exemplares));
    }

    public Response cadastrarLeitor(String tipo) {
        return post("/api/leitores", Map.of("nome", "Leitor " + tipo.toLowerCase(), "email", GeradorDados.emailUnico(),
                "tipo", tipo));
    }

    public Response emprestar(long livroId, long leitorId) {
        return post("/api/emprestimos", Map.of("livroId", livroId, "leitorId", leitorId));
    }
}
