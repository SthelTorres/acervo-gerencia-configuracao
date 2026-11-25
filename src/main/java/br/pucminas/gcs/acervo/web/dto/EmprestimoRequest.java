package br.pucminas.gcs.acervo.web.dto;

import jakarta.validation.constraints.NotNull;

public record EmprestimoRequest(
        @NotNull(message = "O livro é obrigatório") Long livroId,
        @NotNull(message = "O leitor é obrigatório") Long leitorId) {
}
