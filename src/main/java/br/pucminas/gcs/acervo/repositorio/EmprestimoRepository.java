package br.pucminas.gcs.acervo.repositorio;

import br.pucminas.gcs.acervo.dominio.Emprestimo;
import br.pucminas.gcs.acervo.dominio.StatusEmprestimo;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    long countByLeitorIdAndStatus(Long leitorId, StatusEmprestimo status);

    long countByStatus(StatusEmprestimo status);

    boolean existsByLeitorIdAndStatusAndDataPrevistaDevolucaoBefore(Long leitorId, StatusEmprestimo status, LocalDate data);

    boolean existsByLivroIdAndStatus(Long livroId, StatusEmprestimo status);

    List<Emprestimo> findByStatusOrderByDataPrevistaDevolucaoAsc(StatusEmprestimo status);

    List<Emprestimo> findByStatusAndDataPrevistaDevolucaoBeforeOrderByDataPrevistaDevolucaoAsc(StatusEmprestimo status,
                                                                                               LocalDate data);

    List<Emprestimo> findByLeitorIdOrderByDataEmprestimoDesc(Long leitorId);
}
