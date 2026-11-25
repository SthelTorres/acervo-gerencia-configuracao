package br.pucminas.gcs.acervo.web;

import br.pucminas.gcs.acervo.servico.LeitorService;
import br.pucminas.gcs.acervo.web.dto.LeitorRequest;
import br.pucminas.gcs.acervo.web.dto.LeitorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leitores")
@Tag(name = "Leitores", description = "Cadastro de leitores")
public class LeitorController {

    private final LeitorService service;

    public LeitorController(LeitorService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os leitores")
    public List<LeitorResponse> listar() {
        return service.listar().stream().map(LeitorResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um leitor pelo identificador")
    public LeitorResponse buscar(@PathVariable Long id) {
        return LeitorResponse.de(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um leitor")
    public ResponseEntity<LeitorResponse> cadastrar(@Valid @RequestBody LeitorRequest req) {
        var leitor = service.cadastrar(req.nome(), req.email(), req.tipo());
        return ResponseEntity.created(URI.create("/api/leitores/" + leitor.getId())).body(LeitorResponse.de(leitor));
    }

    @PostMapping("/{id}/desativacao")
    @Operation(summary = "Desativa um leitor (bloqueia novos empréstimos)")
    public LeitorResponse desativar(@PathVariable Long id) {
        return LeitorResponse.de(service.desativar(id));
    }

    @PostMapping("/{id}/reativacao")
    @Operation(summary = "Reativa um leitor")
    public LeitorResponse reativar(@PathVariable Long id) {
        return LeitorResponse.de(service.reativar(id));
    }
}
