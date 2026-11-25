package br.pucminas.gcs.acervo.repositorio;

import static org.assertj.core.api.Assertions.assertThat;

import br.pucminas.gcs.acervo.dominio.Emprestimo;
import br.pucminas.gcs.acervo.dominio.Leitor;
import br.pucminas.gcs.acervo.dominio.Livro;
import br.pucminas.gcs.acervo.dominio.StatusEmprestimo;
import br.pucminas.gcs.acervo.dominio.TipoLeitor;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("Repositórios JPA (integração com banco)")
class EmprestimoRepositoryIT {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 25);

    @Autowired
    private LivroRepository livros;
    @Autowired
    private LeitorRepository leitores;
    @Autowired
    private EmprestimoRepository emprestimos;

    @Test
    void consultasDerivadasDeEmprestimos() {
        Livro livro = livros.save(new Livro("9788550804606", "Arquitetura Limpa", "Robert C. Martin", 2019, "ES", 3));
        Leitor leitor = leitores.save(new Leitor("Repo Teste", "repo@exemplo.com", TipoLeitor.ALUNO));

        Emprestimo atrasado = emprestimos.save(Emprestimo.abrir(livro, leitor, HOJE.minusDays(20), HOJE.minusDays(6)));
        Emprestimo noPrazo = emprestimos.save(Emprestimo.abrir(livro, leitor, HOJE, HOJE.plusDays(14)));
        Emprestimo devolvido = Emprestimo.abrir(livro, leitor, HOJE.minusDays(30), HOJE.minusDays(16));
        devolvido.devolver(HOJE.minusDays(16), BigDecimal.ZERO);
        emprestimos.save(devolvido);

        assertThat(emprestimos.countByLeitorIdAndStatus(leitor.getId(), StatusEmprestimo.ATIVO)).isEqualTo(2);
        assertThat(emprestimos.existsByLeitorIdAndStatusAndDataPrevistaDevolucaoBefore(
                leitor.getId(), StatusEmprestimo.ATIVO, HOJE)).isTrue();
        assertThat(emprestimos.existsByLivroIdAndStatus(livro.getId(), StatusEmprestimo.ATIVO)).isTrue();
        assertThat(emprestimos.findByStatusAndDataPrevistaDevolucaoBeforeOrderByDataPrevistaDevolucaoAsc(
                StatusEmprestimo.ATIVO, HOJE)).extracting(Emprestimo::getId).contains(atrasado.getId())
                .doesNotContain(noPrazo.getId());
        assertThat(emprestimos.findByLeitorIdOrderByDataEmprestimoDesc(leitor.getId()))
                .extracting(Emprestimo::getId).containsExactly(noPrazo.getId(), atrasado.getId(), devolvido.getId());
    }

    @Test
    void consultasDeLivrosELeitores() {
        assertThat(livros.existsByIsbn("9780321601919")).isTrue();
        assertThat(livros.findByIsbn("9780321601919")).get().extracting(Livro::getTitulo).isEqualTo("Continuous Delivery");
        assertThat(livros.somarExemplares()).isGreaterThanOrEqualTo(livros.somarExemplaresDisponiveis());
        assertThat(leitores.existsByEmailIgnoreCase("ANA.SOUZA@exemplo.edu.br")).isTrue();
        assertThat(leitores.findByEmailIgnoreCase("carlos.lima@exemplo.edu.br")).get()
                .extracting(Leitor::getTipo).isEqualTo(TipoLeitor.PROFESSOR);
        assertThat(leitores.countByAtivoTrue()).isGreaterThanOrEqualTo(4);
    }
}
