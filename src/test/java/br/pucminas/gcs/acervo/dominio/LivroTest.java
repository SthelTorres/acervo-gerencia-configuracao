package br.pucminas.gcs.acervo.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.pucminas.gcs.acervo.excecao.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Livro")
class LivroTest {

    private Livro novoLivro(int exemplares) {
        return new Livro("9781484200773", "Pro Git", "Scott Chacon", 2014, "GCS", exemplares);
    }

    @Test
    void iniciaComTodosOsExemplaresDisponiveis() {
        Livro livro = novoLivro(3);
        assertThat(livro.getExemplaresTotais()).isEqualTo(3);
        assertThat(livro.getExemplaresDisponiveis()).isEqualTo(3);
        assertThat(livro.isDisponivel()).isTrue();
        assertThat(livro.getExemplaresEmprestados()).isZero();
    }

    @Test
    void exigePeloMenosUmExemplar() {
        assertThatThrownBy(() -> novoLivro(0)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void controlaSaidaERetornoDeExemplares() {
        Livro livro = novoLivro(1);
        livro.registrarSaida();
        assertThat(livro.isDisponivel()).isFalse();
        assertThat(livro.getExemplaresEmprestados()).isEqualTo(1);
        assertThatThrownBy(livro::registrarSaida).isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Não há exemplares");
        livro.registrarRetorno();
        assertThat(livro.getExemplaresDisponiveis()).isEqualTo(1);
    }

    @Test
    void naoPermiteRetornoAlemDoTotal() {
        assertThatThrownBy(() -> novoLivro(2).registrarRetorno()).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void adicionaExemplares() {
        Livro livro = novoLivro(1);
        livro.adicionarExemplares(2);
        assertThat(livro.getExemplaresTotais()).isEqualTo(3);
        assertThat(livro.getExemplaresDisponiveis()).isEqualTo(3);
        assertThatThrownBy(() -> livro.adicionarExemplares(0)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atualizaDadosBibliograficos() {
        Livro livro = novoLivro(1);
        livro.atualizarDados("Pro Git 2", "Chacon; Straub", 2015, "Git");
        assertThat(livro.getTitulo()).isEqualTo("Pro Git 2");
        assertThat(livro.getAutor()).isEqualTo("Chacon; Straub");
        assertThat(livro.getAnoPublicacao()).isEqualTo(2015);
        assertThat(livro.getCategoria()).isEqualTo("Git");
        assertThat(livro.getIsbn()).isEqualTo("9781484200773");
    }
}
