package br.pucminas.gcs.acervo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Aplicação (integração)")
class AcervoApplicationIT {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private Flyway flyway;

    @Test
    void aplicaTodasAsMigracoesDoBanco() {
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
    }

    @Test
    void healthCheckIndicaAplicacaoNoAr() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"));
    }

    @Test
    void infoExpoeVersaoEAmbiente() throws Exception {
        mvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aplicacao.nome").value("Acervo"))
                .andExpect(jsonPath("$.ambiente").value("desenvolvimento"))
                .andExpect(jsonPath("$.build.version").exists());
    }

    @Test
    void publicaDocumentacaoOpenApiEPaginaInicial() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Acervo - API de Gestão de Biblioteca"));
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Acervo")));
    }
}
