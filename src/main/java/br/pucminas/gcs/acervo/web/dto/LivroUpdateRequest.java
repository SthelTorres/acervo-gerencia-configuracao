package br.pucminas.gcs.acervo.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LivroUpdateRequest(
        @NotBlank(message = "O título é obrigatório") @Size(max = 200) String titulo,
        @NotBlank(message = "O autor é obrigatório") @Size(max = 150) String autor,
        @Min(1450) @Max(2100) Integer anoPublicacao,
        @Size(max = 60) String categoria) {
}
