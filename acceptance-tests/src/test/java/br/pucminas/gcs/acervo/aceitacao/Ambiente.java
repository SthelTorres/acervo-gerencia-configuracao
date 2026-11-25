package br.pucminas.gcs.acervo.aceitacao;

import java.util.Optional;

/**
 * Parâmetros do ambiente sob teste, lidos de propriedades do sistema ou variáveis de ambiente.
 */
public final class Ambiente {

    private Ambiente() {
    }

    public static String baseUrl() {
        return valor("acervo.baseUrl", "ACERVO_BASE_URL").orElse("http://localhost:8080");
    }

    public static Optional<String> versaoEsperada() {
        return valor("acervo.versaoEsperada", "ACERVO_VERSAO_ESPERADA");
    }

    public static Optional<String> commitEsperado() {
        return valor("acervo.commitEsperado", "ACERVO_COMMIT_ESPERADO");
    }

    private static Optional<String> valor(String propriedade, String variavel) {
        String v = System.getProperty(propriedade);
        if (v == null || v.isBlank()) {
            v = System.getenv(variavel);
        }
        return (v == null || v.isBlank()) ? Optional.empty() : Optional.of(v.trim());
    }
}
