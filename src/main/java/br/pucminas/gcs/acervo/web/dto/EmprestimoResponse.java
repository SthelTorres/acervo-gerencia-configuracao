package br.pucminas.gcs.acervo.web.dto;

import br.pucminas.gcs.acervo.dominio.Emprestimo;
import br.pucminas.gcs.acervo.dominio.StatusEmprestimo;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmprestimoResponse(Long id, Long livroId, String tituloLivro, Long leitorId, String nomeLeitor,
                                 LocalDate dataEmprestimo, LocalDate dataPrevistaDevolucao, LocalDate dataDevolucao,
                                 StatusEmprestimo status, BigDecimal multa, int renovacoes, boolean atrasado) {

    public static EmprestimoResponse de(Emprestimo e, LocalDate hoje) {
        return new EmprestimoResponse(e.getId(), e.getLivro().getId(), e.getLivro().getTitulo(),
                e.getLeitor().getId(), e.getLeitor().getNome(), e.getDataEmprestimo(), e.getDataPrevistaDevolucao(),
                e.getDataDevolucao(), e.getStatus(), e.getMulta(), e.getRenovacoes(), e.estaAtrasado(hoje));
    }
}
