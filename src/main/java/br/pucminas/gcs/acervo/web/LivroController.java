package br.pucminas.gcs.acervo.web;

import br.pucminas.gcs.acervo.servico.LivroService;
import br.pucminas.gcs.acervo.web.dto.ExemplaresRequest;
import br.pucminas.gcs.acervo.web.dto.LivroRequest;
import br.pucminas.gcs.acervo.web.dto.LivroResponse;
import br.pucminas.gcs.acervo.web.dto.LivroUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/livros")
@Tag(name = "Livros", description = "Manutenção do acervo")
public class LivroController {

    private final LivroService service;

    public LivroController(LivroService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista ou pesquisa livros por título/autor ou categoria")
    public List<LivroResponse> pesquisar(@RequestParam(required = false) String termo,
                                         @RequestParam(required = false) String categoria) {
        return service.pesquisar(termo, categoria).stream().map(LivroResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um livro pelo identificador")
    public LivroResponse buscar(@PathVariable Long id) {
        return LivroResponse.de(service.buscarPorId(id));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Busca um livro pelo ISBN")
    public LivroResponse buscarPorIsbn(@PathVariable String isbn) {
        return LivroResponse.de(service.buscarPorIsbn(isbn));
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo livro")
    public ResponseEntity<LivroResponse> cadastrar(@Valid @RequestBody LivroRequest req) {
        var livro = service.cadastrar(req.isbn(), req.titulo(), req.autor(), req.anoPublicacao(), req.categoria(),
                req.exemplares());
        return ResponseEntity.created(URI.create("/api/livros/" + livro.getId())).body(LivroResponse.de(livro));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados bibliográficos de um livro")
    public LivroResponse atualizar(@PathVariable Long id, @Valid @RequestBody LivroUpdateRequest req) {
        return LivroResponse.de(service.atualizar(id, req.titulo(), req.autor(), req.anoPublicacao(), req.categoria()));
    }

    @PostMapping("/{id}/exemplares")
    @Operation(summary = "Adiciona exemplares a um livro existente")
    public LivroResponse adicionarExemplares(@PathVariable Long id, @Valid @RequestBody ExemplaresRequest req) {
        return LivroResponse.de(service.adicionarExemplares(id, req.quantidade()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um livro sem empréstimos ativos")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
