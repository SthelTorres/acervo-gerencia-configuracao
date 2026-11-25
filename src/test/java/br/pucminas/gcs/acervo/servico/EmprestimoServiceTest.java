package br.pucminas.gcs.acervo.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucminas.gcs.acervo.config.ClockConfig;
import br.pucminas.gcs.acervo.dominio.Emprestimo;
import br.pucminas.gcs.acervo.dominio.Leitor;
import br.pucminas.gcs.acervo.dominio.Livro;
import br.pucminas.gcs.acervo.dominio.StatusEmprestimo;
import br.pucminas.gcs.acervo.dominio.TipoLeitor;
import br.pucminas.gcs.acervo.dominio.regras.CalculadoraMulta;
import br.pucminas.gcs.acervo.dominio.regras.PoliticaEmprestimo;
import br.pucminas.gcs.acervo.excecao.RecursoNaoEncontradoException;
import br.pucminas.gcs.acervo.excecao.RegraNegocioException;
import br.pucminas.gcs.acervo.repositorio.EmprestimoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmprestimoService")
class EmprestimoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 25);

    @Mock
    private EmprestimoRepository emprestimos;
    @Mock
    private LivroService livroService;
    @Mock
    private LeitorService leitorService;

    private EmprestimoService service;
    private Livro livro;
    private Leitor leitor;

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(HOJE.atStartOfDay(ClockConfig.FUSO_HORARIO).toInstant(), ClockConfig.FUSO_HORARIO);
        service = new EmprestimoService(emprestimos, livroService, leitorService, new PoliticaEmprestimo(1),
                new CalculadoraMulta(new BigDecimal("2.00"), new BigDecimal("50.00")), relogio);
        livro = new Livro("9780321601919", "Continuous Delivery", "Humble", 2010, "DevOps", 2);
        leitor = new Leitor("Ana", "ana@exemplo.com", TipoLeitor.ALUNO);
    }

    @Test
    void realizaEmprestimoComPrazoDoTipoDeLeitor() {
        when(leitorService.buscarPorId(1L)).thenReturn(leitor);
        when(livroService.buscarPorId(2L)).thenReturn(livro);
        when(emprestimos.countByLeitorIdAndStatus(1L, StatusEmprestimo.ATIVO)).thenReturn(0L);
        when(emprestimos.existsByLeitorIdAndStatusAndDataPrevistaDevolucaoBefore(1L, StatusEmprestimo.ATIVO, HOJE))
                .thenReturn(false);
        when(emprestimos.save(any(Emprestimo.class))).thenAnswer(inv -> inv.getArgument(0));

        Emprestimo emprestimo = service.emprestar(2L, 1L);

        assertThat(emprestimo.getDataEmprestimo()).isEqualTo(HOJE);
        assertThat(emprestimo.getDataPrevistaDevolucao()).isEqualTo(HOJE.plusDays(14));
        assertThat(livro.getExemplaresDisponiveis()).isEqualTo(1);
    }

    @Test
    void naoEmprestaParaLeitorComAtraso() {
        when(leitorService.buscarPorId(1L)).thenReturn(leitor);
        when(livroService.buscarPorId(2L)).thenReturn(livro);
        when(emprestimos.countByLeitorIdAndStatus(1L, StatusEmprestimo.ATIVO)).thenReturn(1L);
        when(emprestimos.existsByLeitorIdAndStatusAndDataPrevistaDevolucaoBefore(1L, StatusEmprestimo.ATIVO, HOJE))
                .thenReturn(true);

        assertThatThrownBy(() -> service.emprestar(2L, 1L)).isInstanceOf(RegraNegocioException.class);
        verify(emprestimos, never()).save(any());
        assertThat(livro.getExemplaresDisponiveis()).isEqualTo(2);
    }

    @Test
    void devolucaoComAtrasoCalculaMulta() {
        Emprestimo emprestimo = Emprestimo.abrir(livro, leitor, HOJE.minusDays(20), HOJE.minusDays(6));
        when(emprestimos.findById(5L)).thenReturn(Optional.of(emprestimo));

        Emprestimo devolvido = service.devolver(5L);

        assertThat(devolvido.getStatus()).isEqualTo(StatusEmprestimo.DEVOLVIDO);
        assertThat(devolvido.getMulta()).isEqualByComparingTo("12.00");
        assertThat(devolvido.getDataDevolucao()).isEqualTo(HOJE);
    }

    @Test
    void renovacaoUsaDataAtualComoBase() {
        Emprestimo emprestimo = Emprestimo.abrir(livro, leitor, HOJE.minusDays(10), HOJE.plusDays(4));
        when(emprestimos.findById(5L)).thenReturn(Optional.of(emprestimo));

        Emprestimo renovado = service.renovar(5L);

        assertThat(renovado.getDataPrevistaDevolucao()).isEqualTo(HOJE.plusDays(14));
        assertThat(renovado.getRenovacoes()).isEqualTo(1);
    }

    @Test
    void listagensDelegamAoRepositorio() {
        Emprestimo emprestimo = Emprestimo.abrir(livro, leitor, HOJE, HOJE.plusDays(14));
        when(emprestimos.findByStatusOrderByDataPrevistaDevolucaoAsc(StatusEmprestimo.ATIVO)).thenReturn(List.of(emprestimo));
        when(emprestimos.findByStatusAndDataPrevistaDevolucaoBeforeOrderByDataPrevistaDevolucaoAsc(StatusEmprestimo.ATIVO, HOJE))
                .thenReturn(List.of());
        when(leitorService.buscarPorId(1L)).thenReturn(leitor);
        when(emprestimos.findByLeitorIdOrderByDataEmprestimoDesc(1L)).thenReturn(List.of(emprestimo));

        assertThat(service.listarAtivos()).hasSize(1);
        assertThat(service.listarAtrasados()).isEmpty();
        assertThat(service.historicoDoLeitor(1L)).containsExactly(emprestimo);
        assertThat(service.hoje()).isEqualTo(HOJE);
    }

    @Test
    void emprestimoInexistenteLancaExcecao() {
        when(emprestimos.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.devolver(9L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
