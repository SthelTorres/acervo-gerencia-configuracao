package br.pucminas.gcs.acervo.servico;

import br.pucminas.gcs.acervo.dominio.StatusEmprestimo;
import br.pucminas.gcs.acervo.repositorio.EmprestimoRepository;
import br.pucminas.gcs.acervo.repositorio.LeitorRepository;
import br.pucminas.gcs.acervo.repositorio.LivroRepository;
import br.pucminas.gcs.acervo.web.dto.EstatisticasResponse;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Indicadores gerais do acervo exibidos no painel.
 */
@Service
@Transactional(readOnly = true)
public class EstatisticaService {

    private final LivroRepository livros;
    private final LeitorRepository leitores;
    private final EmprestimoRepository emprestimos;
    private final Clock clock;

    public EstatisticaService(LivroRepository livros, LeitorRepository leitores, EmprestimoRepository emprestimos,
                              Clock clock) {
        this.livros = livros;
        this.leitores = leitores;
        this.emprestimos = emprestimos;
        this.clock = clock;
    }

    public EstatisticasResponse gerar() {
        long totalExemplares = livros.somarExemplares();
        long disponiveis = livros.somarExemplaresDisponiveis();
        long atrasados = emprestimos.findByStatusAndDataPrevistaDevolucaoBeforeOrderByDataPrevistaDevolucaoAsc(
                StatusEmprestimo.ATIVO, LocalDate.now(clock)).size();
        return new EstatisticasResponse(
                livros.count(),
                totalExemplares,
                disponiveis,
                leitores.countByAtivoTrue(),
                emprestimos.countByStatus(StatusEmprestimo.ATIVO),
                atrasados,
                taxaOcupacao(totalExemplares, disponiveis));
    }

    /** Percentual de exemplares emprestados em relação ao total (0 a 100). */
    static double taxaOcupacao(long total, long disponiveis) {
        if (total == 0) {
            return 0.0;
        }
        double ocupacao = (total - disponiveis) * 100.0 / total;
        return Math.round(ocupacao * 10.0) / 10.0;
    }
}
