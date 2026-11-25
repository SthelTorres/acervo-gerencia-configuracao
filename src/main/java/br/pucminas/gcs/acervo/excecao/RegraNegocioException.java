package br.pucminas.gcs.acervo.excecao;

/**
 * Lançada quando uma operação viola uma regra de negócio da biblioteca. Mapeada para HTTP 422.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
