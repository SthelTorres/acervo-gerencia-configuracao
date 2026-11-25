package br.pucminas.gcs.acervo.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.pucminas.gcs.acervo.excecao.RegraNegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Emprestimo")
class EmprestimoTest {

    private static final LocalDate RETIRADA = LocalDate.of(2026, 11, 1);
    private static final LocalDate PREVISTA = RETIRADA.plusDays(14);

    private Livro livro;
    private Leitor leitor;

    @BeforeEach
    void preparar() {
        livro = new Livro("9780321601919", "Continuous Delivery", "Humble; Farley", 2010, "DevOps", 1);
        leitor = new Leitor("Ana", "ana@exemplo.com", TipoLeitor.ALUNO);
    }

    @Test
    void abrirEmprestimoRetiraExemplarDoAcervo() {
        Emprestimo e = Emprestimo.abrir(livro, leitor, RETIRADA, PREVISTA);
        assertThat(e.isAtivo()).isTrue();
        assertThat(e.getStatus()).isEqualTo(StatusEmprestimo.ATIVO);
        assertThat(e.getLivro()).isSameAs(livro);
        assertThat(e.getLeitor()).isSameAs(leitor);
        assertThat(e.getDataEmprestimo()).isEqualTo(RETIRADA);
        assertThat(e.getDataPrevistaDevolucao()).isEqualTo(PREVISTA);
        assertThat(e.getMulta()).isEqualByComparingTo("0");
        assertThat(livro.getExemplaresDisponiveis()).isZero();
    }

    @Test
    void identificaAtrasoEDiasDeAtraso() {
        Emprestimo e = Emprestimo.abrir(livro, leitor, RETIRADA, PREVISTA);
        assertThat(e.estaAtrasado(PREVISTA)).isFalse();
        assertThat(e.diasDeAtraso(PREVISTA.minusDays(2))).isZero();
        assertThat(e.estaAtrasado(PREVISTA.plusDays(1))).isTrue();
        assertThat(e.diasDeAtraso(PREVISTA.plusDays(4))).isEqualTo(4);
    }

    @Test
    void devolucaoEncerraEmprestimoEDevolveExemplar() {
        Emprestimo e = Emprestimo.abrir(livro, leitor, RETIRADA, PREVISTA);
        e.devolver(PREVISTA.plusDays(2), new BigDecimal("4.00"));
        assertThat(e.isAtivo()).isFalse();
        assertThat(e.getStatus()).isEqualTo(StatusEmprestimo.DEVOLVIDO);
        assertThat(e.getDataDevolucao()).isEqualTo(PREVISTA.plusDays(2));
        assertThat(e.getMulta()).isEqualByComparingTo("4.00");
        assertThat(e.estaAtrasado(PREVISTA.plusDays(30))).isFalse();
        assertThat(livro.getExemplaresDisponiveis()).isEqualTo(1);
    }

    @Test
    void naoPermiteDevolverDuasVezes() {
        Emprestimo e = Emprestimo.abrir(livro, leitor, RETIRADA, PREVISTA);
        e.devolver(PREVISTA, BigDecimal.ZERO);
        assertThatThrownBy(() -> e.devolver(PREVISTA, BigDecimal.ZERO)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoPermiteDevolucaoAntesDaRetirada() {
        Emprestimo e = Emprestimo.abrir(livro, leitor, RETIRADA, PREVISTA);
        assertThatThrownBy(() -> e.devolver(RETIRADA.minusDays(1), BigDecimal.ZERO))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void renovacaoEstendePrazoEContaRenovacoes() {
        Emprestimo e = Emprestimo.abrir(livro, leitor, RETIRADA, PREVISTA);
        e.renovar(PREVISTA.plusDays(14));
        assertThat(e.getDataPrevistaDevolucao()).isEqualTo(PREVISTA.plusDays(14));
        assertThat(e.getRenovacoes()).isEqualTo(1);
    }

    @Test
    void naoAbreEmprestimoSemExemplarDisponivel() {
        Emprestimo.abrir(livro, leitor, RETIRADA, PREVISTA);
        assertThatThrownBy(() -> Emprestimo.abrir(livro, leitor, RETIRADA, PREVISTA))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void tipoLeitorDefineLimitesEPrazos() {
        assertThat(TipoLeitor.ALUNO.getLimiteEmprestimos()).isEqualTo(3);
        assertThat(TipoLeitor.PROFESSOR.getPrazoDias()).isEqualTo(30);
        assertThat(TipoLeitor.VISITANTE.getLimiteEmprestimos()).isEqualTo(1);
    }

    @Test
    void leitorPodeSerDesativadoEReativado() {
        leitor.desativar();
        assertThat(leitor.isAtivo()).isFalse();
        leitor.reativar();
        assertThat(leitor.isAtivo()).isTrue();
    }
}
