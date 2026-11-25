package br.pucminas.gcs.acervo.web;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@DisplayName("API de Leitores (integração)")
class LeitorApiIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void cadastraListaDesativaEReativaLeitor() throws Exception {
        String corpo = mvc.perform(post("/api/leitores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Bruno Costa\", \"email\": \"Bruno@Exemplo.com\", \"tipo\": \"PROFESSOR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("bruno@exemplo.com"))
                .andExpect(jsonPath("$.limiteEmprestimos").value(5))
                .andExpect(jsonPath("$.prazoDias").value(30))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(corpo, "$.id")).longValue();

        mvc.perform(get("/api/leitores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(hasItem("Bruno Costa")));

        mvc.perform(post("/api/leitores/{id}/desativacao", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(post("/api/leitores/{id}/reativacao", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));
        mvc.perform(get("/api/leitores/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("PROFESSOR"));
    }

    @Test
    void rejeitaEmailDuplicado() throws Exception {
        mvc.perform(post("/api/leitores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Outra Ana\", \"email\": \"ana.souza@exemplo.edu.br\", \"tipo\": \"ALUNO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void validaEmailETipo() throws Exception {
        mvc.perform(post("/api/leitores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Sem Email\", \"email\": \"invalido\", \"tipo\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.email").value("E-mail inválido"))
                .andExpect(jsonPath("$.erros.tipo").exists());
    }

    @Test
    void leitorInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/leitores/{id}", 99999)).andExpect(status().isNotFound());
    }
}
