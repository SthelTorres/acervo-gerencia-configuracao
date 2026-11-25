package br.pucminas.gcs.acervo.dominio.regras;

import br.pucminas.gcs.acervo.config.AcervoProperties;
import br.pucminas.gcs.acervo.dominio.Emprestimo;
import br.pucminas.gcs.acervo.dominio.Leitor;
import br.pucminas.gcs.acervo.excecao.RegraNegocioException;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Regras que determinam se um leitor pode retirar ou renovar empréstimos.
 */
@Component
public class PoliticaEmprestimo {

    private final int maxRenovacoes;

    @Autowired
    public PoliticaEmprestimo(AcervoProperties propriedades) {
        this(propriedades.emprestimo().maxRenovacoes());
    }

    public PoliticaEmprestimo(int maxRenovacoes) {
        this.maxRenovacoes = maxRenovacoes;
    }

    /**
     * Verifica se o leitor pode realizar um novo empréstimo.
     *
     * @param emprestimosAtivos quantidade de empréstimos em aberto do leitor
     * @param possuiAtraso      se o leitor tem algum empréstimo vencido
     */
    public void validarElegibilidade(Leitor leitor, long emprestimosAtivos, boolean possuiAtraso) {
        if (!leitor.isAtivo()) {
            throw new RegraNegocioException("O leitor " + leitor.getNome() + " está inativo");
        }
        if (possuiAtraso) {
            throw new RegraNegocioException("O leitor " + leitor.getNome() + " possui empréstimos em atraso");
        }
        int limite = leitor.getTipo().getLimiteEmprestimos();
        if (emprestimosAtivos >= limite) {
            throw new RegraNegocioException("Limite de " + limite + " empréstimo(s) simultâneo(s) atingido para "
                    + leitor.getTipo().name().toLowerCase());
        }
    }

    public LocalDate calcularDataDevolucao(Leitor leitor, LocalDate dataEmprestimo) {
        return dataEmprestimo.plusDays(leitor.getTipo().getPrazoDias());
    }

    public void validarRenovacao(Emprestimo emprestimo, LocalDate hoje) {
        if (!emprestimo.isAtivo()) {
            throw new RegraNegocioException("Somente empréstimos ativos podem ser renovados");
        }
        if (emprestimo.estaAtrasado(hoje)) {
            throw new RegraNegocioException("Empréstimos em atraso não podem ser renovados");
        }
        if (emprestimo.getRenovacoes() >= maxRenovacoes) {
            throw new RegraNegocioException("Limite de " + maxRenovacoes + " renovação(ões) atingido");
        }
    }

    public int getMaxRenovacoes() {
        return maxRenovacoes;
    }
}
