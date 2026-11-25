package br.pucminas.gcs.acervo.dominio.regras;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.pucminas.gcs.acervo.dominio.Emprestimo;
import br.pucminas.gcs.acervo.dominio.Leitor;
import br.pucminas.gcs.acervo.dominio.Livro;
import br.pucminas.gcs.acervo.dominio.TipoLeitor;
import br.pucminas.gcs.acervo.excecao.RegraNegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("PoliticaEmprestimo")
class PoliticaEmprestimoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 25);
    private final PoliticaEmprestimo politica = new PoliticaEmprestimo(1);

    private static Leitor leitor(TipoLeitor tipo) {
        return new Leitor("Fulano", "fulano@exemplo.com", tipo);
    }

    private static Emprestimo emprestimo(LocalDate prevista) {
        Livro livro = new Livro("9780321601919", "Continuous Delivery", "Humble", 2010, "DevOps", 2);
        return Emprestimo.abrir(livro, leitor(TipoLeitor.ALUNO), HOJE.minusDays(5), prevista);
    }

    @Test
    void permiteEmprestimoAbaixoDoLimite() {
        assertThatCode(() -> politica.validarElegibilidade(leitor(TipoLeitor.ALUNO), 2, false)).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0} atinge o limite com {1} empréstimos")
    @CsvSource({"ALUNO, 3", "PROFESSOR, 5", "VISITANTE, 1"})
    void bloqueiaQuandoLimiteDoTipoEhAtingido(TipoLeitor tipo, int ativos) {
        assertThatThrownBy(() -> politica.validarElegibilidade(leitor(tipo), ativos, false))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Limite de " + ativos);
    }

    @Test
    void bloqueiaLeitorInativo() {
        Leitor inativo = leitor(TipoLeitor.PROFESSOR);
        inativo.desativar();
        assertThatThrownBy(() -> politica.validarElegibilidade(inativo, 0, false))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("inativo");
    }

    @Test
    void bloqueiaLeitorComAtraso() {
        assertThatThrownBy(() -> politica.validarElegibilidade(leitor(TipoLeitor.ALUNO), 0, true))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("atraso");
    }

    @ParameterizedTest(name = "{0} devolve em {1} dias")
    @CsvSource({"ALUNO, 14", "PROFESSOR, 30", "VISITANTE, 7"})
    void calculaDataDeDevolucaoPeloTipo(TipoLeitor tipo, int dias) {
        assertThat(politica.calcularDataDevolucao(leitor(tipo), HOJE)).isEqualTo(HOJE.plusDays(dias));
    }

    @Test
    void permiteRenovarEmprestimoNoPrazo() {
        assertThatCode(() -> politica.validarRenovacao(emprestimo(HOJE.plusDays(2)), HOJE)).doesNotThrowAnyException();
    }

    @Test
    void naoPermiteRenovarEmprestimoAtrasado() {
        assertThatThrownBy(() -> politica.validarRenovacao(emprestimo(HOJE.minusDays(1)), HOJE))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("atraso");
    }

    @Test
    void naoPermiteUltrapassarLimiteDeRenovacoes() {
        Emprestimo emprestimo = emprestimo(HOJE.plusDays(2));
        emprestimo.renovar(HOJE.plusDays(14));
        assertThatThrownBy(() -> politica.validarRenovacao(emprestimo, HOJE))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("renovação");
        assertThat(politica.getMaxRenovacoes()).isEqualTo(1);
    }

    @Test
    void naoPermiteRenovarEmprestimoDevolvido() {
        Emprestimo emprestimo = emprestimo(HOJE.plusDays(2));
        emprestimo.devolver(HOJE, BigDecimal.ZERO);
        assertThatThrownBy(() -> politica.validarRenovacao(emprestimo, HOJE))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("ativos");
    }
}
