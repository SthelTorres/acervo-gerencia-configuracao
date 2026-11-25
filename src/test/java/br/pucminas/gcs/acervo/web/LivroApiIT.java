package br.pucminas.gcs.acervo.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("API de Livros (integração)")
class LivroApiIT {

    private static final String NOVO_LIVRO = """
            {"isbn": "978-1-68050-239-8", "titulo": "Release It!", "autor": "Michael T. Nygard",
             "anoPublicacao": 2018, "categoria": "DevOps", "exemplares": 2}
            """;

    @Autowired
    private MockMvc mvc;

    private long cadastrar(String json) throws Exception {
        String corpo = mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    @Test
    void listaOAcervoInicialCarregadoPeloFlyway() throws Exception {
        mvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$[*].titulo").value(hasItem("Continuous Delivery")));
    }

    @Test
    void cadastraConsultaEAtualizaLivro() throws Exception {
        long id = cadastrar(NOVO_LIVRO);

        mvc.perform(get("/api/livros/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("9781680502398"))
                .andExpect(jsonPath("$.exemplaresDisponiveis").value(2))
                .andExpect(jsonPath("$.disponivel").value(true));

        mvc.perform(get("/api/livros/isbn/{isbn}", "9781680502398"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));

        mvc.perform(put("/api/livros/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"Release It! 2nd Edition\", \"autor\": \"Nygard\", \"anoPublicacao\": 2018}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Release It! 2nd Edition"));

        mvc.perform(post("/api/livros/{id}/exemplares", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exemplaresTotais").value(5));
    }

    @Test
    void pesquisaPorTermoECategoria() throws Exception {
        mvc.perform(get("/api/livros").param("termo", "humble"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo").value(hasItem("Continuous Delivery")));
        mvc.perform(get("/api/livros").param("categoria", "gerência de configuração"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo").value(hasItem("Pro Git")));
    }

    @Test
    void retornaLocationAoCadastrar() throws Exception {
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(NOVO_LIVRO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/livros/")));
    }

    @Test
    void rejeitaIsbnDuplicadoComConflito() throws Exception {
        cadastrar(NOVO_LIVRO);
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(NOVO_LIVRO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Recurso duplicado"));
    }

    @Test
    void rejeitaIsbnInvalidoComRegraDeNegocio() throws Exception {
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\": \"1234567890\", \"titulo\": \"X\", \"autor\": \"Y\", \"exemplares\": 1}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("ISBN inválido: 1234567890"));
    }

    @Test
    void validaCamposObrigatorios() throws Exception {
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\": \"\", \"titulo\": \"\", \"autor\": \"Y\", \"exemplares\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.isbn").exists())
                .andExpect(jsonPath("$.erros.titulo").exists())
                .andExpect(jsonPath("$.erros.exemplares").exists());
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content("{json inválido"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeLivroERetorna404Depois() throws Exception {
        long id = cadastrar(NOVO_LIVRO);
        mvc.perform(delete("/api/livros/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/livros/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
