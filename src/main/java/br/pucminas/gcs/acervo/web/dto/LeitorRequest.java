package br.pucminas.gcs.acervo.web.dto;

import br.pucminas.gcs.acervo.dominio.TipoLeitor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeitorRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 120) String nome,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail inválido") @Size(max = 150) String email,
        @NotNull(message = "O tipo de leitor é obrigatório") TipoLeitor tipo) {
}
