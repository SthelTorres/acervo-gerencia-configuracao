package br.pucminas.gcs.acervo.excecao;

/**
 * Lançada quando um recurso duplicado é cadastrado (ex.: ISBN ou e-mail já existentes). Mapeada para HTTP 409.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
