package br.pucminas.gcs.acervo.aceitacao;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Gera dados únicos para que os cenários sejam independentes e possam ser repetidos no mesmo ambiente.
 */
public final class GeradorDados {

    private GeradorDados() {
    }

    /** Gera um ISBN-13 aleatório com dígito verificador válido (prefixo 979, raro em acervos reais). */
    public static String isbnValido() {
        StringBuilder isbn = new StringBuilder("979");
        for (int i = 0; i < 9; i++) {
            isbn.append(ThreadLocalRandom.current().nextInt(10));
        }
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            int d = isbn.charAt(i) - '0';
            soma += (i % 2 == 0) ? d : d * 3;
        }
        isbn.append((10 - soma % 10) % 10);
        return isbn.toString();
    }

    public static String emailUnico() {
        return "aceitacao." + UUID.randomUUID().toString().substring(0, 8) + "@exemplo.com";
    }
}
