package br.pucminas.gcs.acervo.web;

import br.pucminas.gcs.acervo.dominio.Emprestimo;
import br.pucminas.gcs.acervo.servico.EmprestimoService;
import br.pucminas.gcs.acervo.web.dto.EmprestimoRequest;
import br.pucminas.gcs.acervo.web.dto.EmprestimoResponse;
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
@RequestMapping("/api/emprestimos")
@Tag(name = "Empréstimos", description = "Circulação de exemplares")
public class EmprestimoController {

    private final EmprestimoService service;

    public EmprestimoController(EmprestimoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Realiza um empréstimo")
    public ResponseEntity<EmprestimoResponse> emprestar(@Valid @RequestBody EmprestimoRequest req) {
        Emprestimo emprestimo = service.emprestar(req.livroId(), req.leitorId());
        return ResponseEntity.created(URI.create("/api/emprestimos/" + emprestimo.getId())).body(resposta(emprestimo));
    }

    @PostMapping("/{id}/devolucao")
    @Operation(summary = "Registra a devolução e calcula eventual multa")
    public EmprestimoResponse devolver(@PathVariable Long id) {
        return resposta(service.devolver(id));
    }

    @PostMapping("/{id}/renovacao")
    @Operation(summary = "Renova o prazo de um empréstimo")
    public EmprestimoResponse renovar(@PathVariable Long id) {
        return resposta(service.renovar(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um empréstimo")
    public EmprestimoResponse buscar(@PathVariable Long id) {
        return resposta(service.buscarPorId(id));
    }

    @GetMapping("/ativos")
    @Operation(summary = "Lista empréstimos em aberto")
    public List<EmprestimoResponse> ativos() {
        return service.listarAtivos().stream().map(this::resposta).toList();
    }

    @GetMapping("/atrasados")
    @Operation(summary = "Lista empréstimos vencidos")
    public List<EmprestimoResponse> atrasados() {
        return service.listarAtrasados().stream().map(this::resposta).toList();
    }

    @GetMapping("/leitor/{leitorId}")
    @Operation(summary = "Histórico de empréstimos de um leitor")
    public List<EmprestimoResponse> historico(@PathVariable Long leitorId) {
        return service.historicoDoLeitor(leitorId).stream().map(this::resposta).toList();
    }

    private EmprestimoResponse resposta(Emprestimo emprestimo) {
        return EmprestimoResponse.de(emprestimo, service.hoje());
    }
}
