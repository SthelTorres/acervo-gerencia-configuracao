package br.pucminas.gcs.acervo.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.pucminas.gcs.acervo.config.ClockConfig;
import br.pucminas.gcs.acervo.suporte.RelogioAjustavel;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("API de Empréstimos (integração)")
class EmprestimoApiIT {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 2);

    @TestConfiguration
    static class RelogioDeTeste {
        @Bean
        @Primary
        RelogioAjustavel relogioAjustavel() {
            return new RelogioAjustavel(HOJE, ClockConfig.FUSO_HORARIO);
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private RelogioAjustavel relogio;

    private long livroId;
    private long leitorId;

    @BeforeEach
    void preparar() throws Exception {
        relogio.definir(HOJE);
        livroId = criar("/api/livros", """
                {"isbn": "9780135957059", "titulo": "The Pragmatic Programmer", "autor": "Thomas; Hunt",
                 "anoPublicacao": 2019, "categoria": "Engenharia de Software", "exemplares": 1}""");
        leitorId = criar("/api/leitores", """
                {"nome": "Leitor Integração", "email": "integracao@exemplo.com", "tipo": "ALUNO"}""");
    }

    private long criar(String url, String json) throws Exception {
        String corpo = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    private ResultActions emprestar(long livro, long leitor) throws Exception {
        return mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"livroId\": " + livro + ", \"leitorId\": " + leitor + "}"));
    }

    @Test
    void fluxoCompletoDeEmprestimoEDevolucaoNoPrazo() throws Exception {
        String corpo = emprestar(livroId, leitorId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataEmprestimo").value("2026-11-02"))
                .andExpect(jsonPath("$.dataPrevistaDevolucao").value("2026-11-16"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andReturn().getResponse().getContentAsString();
        long emprestimoId = ((Number) JsonPath.read(corpo, "$.id")).longValue();

        mvc.perform(get("/api/livros/{id}", livroId)).andExpect(jsonPath("$.exemplaresDisponiveis").value(0));
        mvc.perform(get("/api/emprestimos/ativos")).andExpect(jsonPath("$[*].id").value(hasItem((int) emprestimoId)));

        relogio.avancarDias(10);
        mvc.perform(post("/api/emprestimos/{id}/devolucao", emprestimoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEVOLVIDO"))
                .andExpect(jsonPath("$.multa").value(0.0));

        mvc.perform(get("/api/livros/{id}", livroId)).andExpect(jsonPath("$.exemplaresDisponiveis").value(1));
        mvc.perform(get("/api/emprestimos/leitor/{id}", leitorId)).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void devolucaoComAtrasoGeraMultaEBloqueiaNovosEmprestimos() throws Exception {
        long emprestimoId = ((Number) JsonPath.read(emprestar(livroId, leitorId).andReturn().getResponse()
                .getContentAsString(), "$.id")).longValue();
        long outroLivro = criar("/api/livros", """
                {"isbn": "0321601912", "titulo": "Livro Extra", "autor": "Autor", "exemplares": 1}""");

        relogio.avancarDias(14 + 5);

        mvc.perform(get("/api/emprestimos/atrasados")).andExpect(jsonPath("$[*].id").value(hasItem((int) emprestimoId)));
        mvc.perform(get("/api/estatisticas")).andExpect(jsonPath("$.emprestimosAtrasados").value(1));
        emprestar(outroLivro, leitorId)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("atraso")));
        mvc.perform(post("/api/emprestimos/{id}/renovacao", emprestimoId)).andExpect(status().isUnprocessableContent());

        mvc.perform(post("/api/emprestimos/{id}/devolucao", emprestimoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.multa").value(10.0));
        mvc.perform(get("/api/emprestimos/atrasados")).andExpect(jsonPath("$[*].id").value(not(hasItem((int) emprestimoId))));
        emprestar(outroLivro, leitorId).andExpect(status().isCreated());
    }

    @Test
    void renovacaoPermitidaUmaVez() throws Exception {
        long emprestimoId = ((Number) JsonPath.read(emprestar(livroId, leitorId).andReturn().getResponse()
                .getContentAsString(), "$.id")).longValue();
        relogio.avancarDias(7);

        mvc.perform(post("/api/emprestimos/{id}/renovacao", emprestimoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPrevistaDevolucao").value("2026-11-23"))
                .andExpect(jsonPath("$.renovacoes").value(1));
        mvc.perform(post("/api/emprestimos/{id}/renovacao", emprestimoId))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void naoEmprestaLivroSemExemplarDisponivel() throws Exception {
        emprestar(livroId, leitorId).andExpect(status().isCreated());
        long outroLeitor = criar("/api/leitores", """
                {"nome": "Segundo Leitor", "email": "segundo@exemplo.com", "tipo": "PROFESSOR"}""");
        emprestar(livroId, outroLeitor)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Não há exemplares")));
    }

    @Test
    void naoRemoveLivroEmprestado() throws Exception {
        emprestar(livroId, leitorId).andExpect(status().isCreated());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/livros/{id}", livroId))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void validaRequisicaoEInexistentes() throws Exception {
        mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.livroId").exists());
        emprestar(livroId, 99999).andExpect(status().isNotFound());
        mvc.perform(get("/api/emprestimos/{id}", 99999)).andExpect(status().isNotFound());
    }
}
