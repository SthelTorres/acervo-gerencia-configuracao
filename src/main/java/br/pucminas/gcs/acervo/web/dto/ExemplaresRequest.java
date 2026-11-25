package br.pucminas.gcs.acervo.web.dto;

import jakarta.validation.constraints.Min;

public record ExemplaresRequest(@Min(value = 1, message = "A quantidade deve ser positiva") int quantidade) {
}
