package br.pucminas.gcs.acervo.web.dto;

import br.pucminas.gcs.acervo.dominio.Leitor;
import br.pucminas.gcs.acervo.dominio.TipoLeitor;

public record LeitorResponse(Long id, String nome, String email, TipoLeitor tipo, boolean ativo,
                             int limiteEmprestimos, int prazoDias) {

    public static LeitorResponse de(Leitor leitor) {
        return new LeitorResponse(leitor.getId(), leitor.getNome(), leitor.getEmail(), leitor.getTipo(),
                leitor.isAtivo(), leitor.getTipo().getLimiteEmprestimos(), leitor.getTipo().getPrazoDias());
    }
}
