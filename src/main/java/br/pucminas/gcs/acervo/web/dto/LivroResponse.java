package br.pucminas.gcs.acervo.web.dto;

import br.pucminas.gcs.acervo.dominio.Livro;

public record LivroResponse(Long id, String isbn, String titulo, String autor, Integer anoPublicacao, String categoria,
                            int exemplaresTotais, int exemplaresDisponiveis, boolean disponivel) {

    public static LivroResponse de(Livro livro) {
        return new LivroResponse(livro.getId(), livro.getIsbn(), livro.getTitulo(), livro.getAutor(),
                livro.getAnoPublicacao(), livro.getCategoria(), livro.getExemplaresTotais(),
                livro.getExemplaresDisponiveis(), livro.isDisponivel());
    }
}
