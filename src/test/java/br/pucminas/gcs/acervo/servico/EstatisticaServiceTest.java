package br.pucminas.gcs.acervo.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.pucminas.gcs.acervo.dominio.StatusEmprestimo;
import br.pucminas.gcs.acervo.repositorio.EmprestimoRepository;
import br.pucminas.gcs.acervo.repositorio.LeitorRepository;
import br.pucminas.gcs.acervo.repositorio.LivroRepository;
import br.pucminas.gcs.acervo.web.dto.EstatisticasResponse;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstatisticaService")
class EstatisticaServiceTest {

    @Mock
    private LivroRepository livros;
    @Mock
    private LeitorRepository leitores;
    @Mock
    private EmprestimoRepository emprestimos;

    @ParameterizedTest(name = "total={0}, disponíveis={1} => {2}%")
    @CsvSource({"0, 0, 0.0", "10, 10, 0.0", "10, 7, 30.0", "3, 2, 33.3", "4, 0, 100.0"})
    void calculaTaxaDeOcupacao(long total, long disponiveis, double esperado) {
        assertThat(EstatisticaService.taxaOcupacao(total, disponiveis)).isEqualTo(esperado);
    }

    @Test
    void consolidaIndicadores() {
        when(livros.count()).thenReturn(4L);
        when(livros.somarExemplares()).thenReturn(10L);
        when(livros.somarExemplaresDisponiveis()).thenReturn(8L);
        when(leitores.countByAtivoTrue()).thenReturn(3L);
        when(emprestimos.countByStatus(StatusEmprestimo.ATIVO)).thenReturn(2L);
        when(emprestimos.findByStatusAndDataPrevistaDevolucaoBeforeOrderByDataPrevistaDevolucaoAsc(any(), any()))
                .thenReturn(List.of());

        EstatisticasResponse r = new EstatisticaService(livros, leitores, emprestimos, Clock.systemUTC()).gerar();

        assertThat(r).isEqualTo(new EstatisticasResponse(4, 10, 8, 3, 2, 0, 20.0));
    }
}
