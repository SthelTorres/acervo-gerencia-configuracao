package br.pucminas.gcs.acervo.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LivroRequest(
        @NotBlank(message = "O ISBN é obrigatório") String isbn,
        @NotBlank(message = "O título é obrigatório") @Size(max = 200) String titulo,
        @NotBlank(message = "O autor é obrigatório") @Size(max = 150) String autor,
        @Min(value = 1450, message = "Ano de publicação inválido") @Max(value = 2100, message = "Ano de publicação inválido") Integer anoPublicacao,
        @Size(max = 60) String categoria,
        @Min(value = 1, message = "Informe pelo menos 1 exemplar") int exemplares) {
}
