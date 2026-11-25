package br.pucminas.gcs.acervo.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucminas.gcs.acervo.dominio.Livro;
import br.pucminas.gcs.acervo.dominio.StatusEmprestimo;
import br.pucminas.gcs.acervo.excecao.ConflitoException;
import br.pucminas.gcs.acervo.excecao.RecursoNaoEncontradoException;
import br.pucminas.gcs.acervo.excecao.RegraNegocioException;
import br.pucminas.gcs.acervo.repositorio.EmprestimoRepository;
import br.pucminas.gcs.acervo.repositorio.LivroRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
@DisplayName("LivroService")
class LivroServiceTest {

    @Mock
    private LivroRepository livros;
    @Mock
    private EmprestimoRepository emprestimos;
    @InjectMocks
    private LivroService service;

    private static Livro livro() {
        return new Livro("9780321601919", "Continuous Delivery", "Humble", 2010, "DevOps", 2);
    }

    @Test
    void cadastraLivroComIsbnNormalizado() {
        when(livros.existsByIsbn("9780321601919")).thenReturn(false);
        when(livros.save(any(Livro.class))).thenAnswer(inv -> inv.getArgument(0));

        Livro salvo = service.cadastrar("978-0-321-60191-9", "  Continuous Delivery ", " Humble ", 2010, "DevOps", 2);

        assertThat(salvo.getIsbn()).isEqualTo("9780321601919");
        assertThat(salvo.getTitulo()).isEqualTo("Continuous Delivery");
        assertThat(salvo.getAutor()).isEqualTo("Humble");
    }

    @Test
    void rejeitaIsbnInvalido() {
        assertThatThrownBy(() -> service.cadastrar("123", "T", "A", null, null, 1))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("ISBN inválido");
        verify(livros, never()).save(any());
    }

    @Test
    void rejeitaIsbnDuplicado() {
        when(livros.existsByIsbn("9780321601919")).thenReturn(true);
        assertThatThrownBy(() -> service.cadastrar("9780321601919", "T", "A", null, null, 1))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void buscaPorIdInexistenteLancaExcecao() {
        when(livros.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L)).isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void buscaPorIsbnNormalizado() {
        when(livros.findByIsbn("9780321601919")).thenReturn(Optional.of(livro()));
        assertThat(service.buscarPorIsbn("978-0321601919").getTitulo()).isEqualTo("Continuous Delivery");
    }

    @Test
    void pesquisaPorTermoCategoriaOuTodos() {
        when(livros.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(eq("git"), eq("git"), any(Sort.class)))
                .thenReturn(List.of(livro()));
        when(livros.findByCategoriaIgnoreCase(eq("DevOps"), any(Sort.class))).thenReturn(List.of(livro(), livro()));
        when(livros.findAll(any(Sort.class))).thenReturn(List.of());

        assertThat(service.pesquisar(" git ", null)).hasSize(1);
        assertThat(service.pesquisar(null, "DevOps")).hasSize(2);
        assertThat(service.pesquisar("  ", " ")).isEmpty();
    }

    @Test
    void atualizaEAdicionaExemplares() {
        Livro livro = livro();
        when(livros.findById(1L)).thenReturn(Optional.of(livro));

        service.atualizar(1L, " Novo Título ", " Novo Autor ", 2020, "Outra");
        service.adicionarExemplares(1L, 3);

        assertThat(livro.getTitulo()).isEqualTo("Novo Título");
        assertThat(livro.getExemplaresTotais()).isEqualTo(5);
    }

    @Test
    void naoRemoveLivroComEmprestimoAtivo() {
        when(livros.findById(1L)).thenReturn(Optional.of(livro()));
        when(emprestimos.existsByLivroIdAndStatus(1L, StatusEmprestimo.ATIVO)).thenReturn(true);
        assertThatThrownBy(() -> service.remover(1L)).isInstanceOf(RegraNegocioException.class);
        verify(livros, never()).delete(any());
    }

    @Test
    void removeLivroSemEmprestimoAtivo() {
        Livro livro = livro();
        when(livros.findById(1L)).thenReturn(Optional.of(livro));
        when(emprestimos.existsByLivroIdAndStatus(1L, StatusEmprestimo.ATIVO)).thenReturn(false);
        service.remover(1L);
        verify(livros).delete(livro);
        verify(livros, never()).existsByIsbn(anyString());
    }
}
