package br.pucminas.gcs.acervo.excecao;

/**
 * Lançada quando um recurso (livro, leitor ou empréstimo) não existe. Mapeada para HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " com identificador " + id + " não foi encontrado(a)");
    }
}
