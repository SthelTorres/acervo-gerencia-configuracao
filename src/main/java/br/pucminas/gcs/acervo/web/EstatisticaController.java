package br.pucminas.gcs.acervo.web;

import br.pucminas.gcs.acervo.servico.EstatisticaService;
import br.pucminas.gcs.acervo.web.dto.EstatisticasResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estatisticas")
@Tag(name = "Estatísticas", description = "Indicadores do acervo")
public class EstatisticaController {

    private final EstatisticaService service;

    public EstatisticaController(EstatisticaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Indicadores gerais do acervo")
    public EstatisticasResponse gerar() {
        return service.gerar();
    }
}
