package br.pucminas.gcs.acervo.servico;

import br.pucminas.gcs.acervo.dominio.Livro;
import br.pucminas.gcs.acervo.dominio.StatusEmprestimo;
import br.pucminas.gcs.acervo.dominio.regras.ValidadorIsbn;
import br.pucminas.gcs.acervo.excecao.ConflitoException;
import br.pucminas.gcs.acervo.excecao.RecursoNaoEncontradoException;
import br.pucminas.gcs.acervo.excecao.RegraNegocioException;
import br.pucminas.gcs.acervo.repositorio.EmprestimoRepository;
import br.pucminas.gcs.acervo.repositorio.LivroRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de manutenção do acervo de livros.
 */
@Service
@Transactional(readOnly = true)
public class LivroService {

    private static final Sort ORDEM_TITULO = Sort.by("titulo");

    private final LivroRepository livros;
    private final EmprestimoRepository emprestimos;

    public LivroService(LivroRepository livros, EmprestimoRepository emprestimos) {
        this.livros = livros;
        this.emprestimos = emprestimos;
    }

    @Transactional
    public Livro cadastrar(String isbn, String titulo, String autor, Integer ano, String categoria, int exemplares) {
        String isbnNormalizado = ValidadorIsbn.normalizar(isbn);
        if (!ValidadorIsbn.isValido(isbnNormalizado)) {
            throw new RegraNegocioException("ISBN inválido: " + isbn);
        }
        if (livros.existsByIsbn(isbnNormalizado)) {
            throw new ConflitoException("Já existe um livro cadastrado com o ISBN " + isbnNormalizado);
        }
        Livro livro = new Livro(isbnNormalizado, titulo.trim(), autor.trim(), ano, categoria, exemplares);
        return livros.save(livro);
    }

    public Livro buscarPorId(Long id) {
        return livros.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Livro", id));
    }

    public Livro buscarPorIsbn(String isbn) {
        String normalizado = ValidadorIsbn.normalizar(isbn);
        return livros.findByIsbn(normalizado).orElseThrow(() -> new RecursoNaoEncontradoException("Livro", isbn));
    }

    /** Lista o acervo, opcionalmente filtrando por termo (título/autor) ou categoria. */
    public List<Livro> pesquisar(String termo, String categoria) {
        if (termo != null && !termo.isBlank()) {
            return livros.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(termo.trim(), termo.trim(), ORDEM_TITULO);
        }
        if (categoria != null && !categoria.isBlank()) {
            return livros.findByCategoriaIgnoreCase(categoria.trim(), ORDEM_TITULO);
        }
        return livros.findAll(ORDEM_TITULO);
    }

    @Transactional
    public Livro atualizar(Long id, String titulo, String autor, Integer ano, String categoria) {
        Livro livro = buscarPorId(id);
        livro.atualizarDados(titulo.trim(), autor.trim(), ano, categoria);
        return livro;
    }

    @Transactional
    public Livro adicionarExemplares(Long id, int quantidade) {
        Livro livro = buscarPorId(id);
        livro.adicionarExemplares(quantidade);
        return livro;
    }

    @Transactional
    public void remover(Long id) {
        Livro livro = buscarPorId(id);
        if (emprestimos.existsByLivroIdAndStatus(id, StatusEmprestimo.ATIVO)) {
            throw new RegraNegocioException("O livro '" + livro.getTitulo() + "' possui empréstimos ativos e não pode ser removido");
        }
        livros.delete(livro);
    }
}
