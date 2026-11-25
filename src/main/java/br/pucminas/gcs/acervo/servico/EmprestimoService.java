package br.pucminas.gcs.acervo.servico;

import br.pucminas.gcs.acervo.dominio.Emprestimo;
import br.pucminas.gcs.acervo.dominio.Leitor;
import br.pucminas.gcs.acervo.dominio.Livro;
import br.pucminas.gcs.acervo.dominio.StatusEmprestimo;
import br.pucminas.gcs.acervo.dominio.regras.CalculadoraMulta;
import br.pucminas.gcs.acervo.dominio.regras.PoliticaEmprestimo;
import br.pucminas.gcs.acervo.excecao.RecursoNaoEncontradoException;
import br.pucminas.gcs.acervo.repositorio.EmprestimoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de circulação: empréstimo, devolução e renovação.
 */
@Service
@Transactional(readOnly = true)
public class EmprestimoService {

    private final EmprestimoRepository emprestimos;
    private final LivroService livroService;
    private final LeitorService leitorService;
    private final PoliticaEmprestimo politica;
    private final CalculadoraMulta calculadoraMulta;
    private final Clock clock;

    public EmprestimoService(EmprestimoRepository emprestimos, LivroService livroService, LeitorService leitorService,
                             PoliticaEmprestimo politica, CalculadoraMulta calculadoraMulta, Clock clock) {
        this.emprestimos = emprestimos;
        this.livroService = livroService;
        this.leitorService = leitorService;
        this.politica = politica;
        this.calculadoraMulta = calculadoraMulta;
        this.clock = clock;
    }

    @Transactional
    public Emprestimo emprestar(Long livroId, Long leitorId) {
        LocalDate hoje = hoje();
        Leitor leitor = leitorService.buscarPorId(leitorId);
        Livro livro = livroService.buscarPorId(livroId);

        long ativos = emprestimos.countByLeitorIdAndStatus(leitorId, StatusEmprestimo.ATIVO);
        boolean possuiAtraso = emprestimos.existsByLeitorIdAndStatusAndDataPrevistaDevolucaoBefore(
                leitorId, StatusEmprestimo.ATIVO, hoje);
        politica.validarElegibilidade(leitor, ativos, possuiAtraso);

        LocalDate dataPrevista = politica.calcularDataDevolucao(leitor, hoje);
        return emprestimos.save(Emprestimo.abrir(livro, leitor, hoje, dataPrevista));
    }

    @Transactional
    public Emprestimo devolver(Long emprestimoId) {
        Emprestimo emprestimo = buscarPorId(emprestimoId);
        LocalDate hoje = hoje();
        BigDecimal multa = calculadoraMulta.calcular(emprestimo.getDataPrevistaDevolucao(), hoje);
        emprestimo.devolver(hoje, multa);
        return emprestimo;
    }

    @Transactional
    public Emprestimo renovar(Long emprestimoId) {
        Emprestimo emprestimo = buscarPorId(emprestimoId);
        LocalDate hoje = hoje();
        politica.validarRenovacao(emprestimo, hoje);
        emprestimo.renovar(politica.calcularDataDevolucao(emprestimo.getLeitor(), hoje));
        return emprestimo;
    }

    public Emprestimo buscarPorId(Long id) {
        return emprestimos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo", id));
    }

    public List<Emprestimo> listarAtivos() {
        return emprestimos.findByStatusOrderByDataPrevistaDevolucaoAsc(StatusEmprestimo.ATIVO);
    }

    public List<Emprestimo> listarAtrasados() {
        return emprestimos.findByStatusAndDataPrevistaDevolucaoBeforeOrderByDataPrevistaDevolucaoAsc(
                StatusEmprestimo.ATIVO, hoje());
    }

    public List<Emprestimo> historicoDoLeitor(Long leitorId) {
        leitorService.buscarPorId(leitorId);
        return emprestimos.findByLeitorIdOrderByDataEmprestimoDesc(leitorId);
    }

    public LocalDate hoje() {
        return LocalDate.now(clock);
    }
}
