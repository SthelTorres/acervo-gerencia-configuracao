package br.pucminas.gcs.acervo.dominio.regras;

/**
 * Valida códigos ISBN-10 e ISBN-13 pelo dígito verificador.
 */
public final class ValidadorIsbn {

    private ValidadorIsbn() {
    }

    /** Remove hífens e espaços e converte o 'x' final para maiúsculo. */
    public static String normalizar(String isbn) {
        if (isbn == null) {
            return "";
        }
        return isbn.replaceAll("[\\s-]", "").toUpperCase();
    }

    public static boolean isValido(String isbn) {
        String codigo = normalizar(isbn);
        return switch (codigo.length()) {
            case 10 -> isIsbn10Valido(codigo);
            case 13 -> isIsbn13Valido(codigo);
            default -> false;
        };
    }

    static boolean isIsbn10Valido(String codigo) {
        if (!codigo.matches("\\d{9}[\\dX]")) {
            return false;
        }
        int soma = 0;
        for (int i = 0; i < 10; i++) {
            char c = codigo.charAt(i);
            int valor = (c == 'X') ? 10 : Character.getNumericValue(c);
            soma += valor * (10 - i);
        }
        return soma % 11 == 0;
    }

    static boolean isIsbn13Valido(String codigo) {
        if (!codigo.matches("\\d{13}")) {
            return false;
        }
        int soma = 0;
        for (int i = 0; i < 13; i++) {
            int digito = Character.getNumericValue(codigo.charAt(i));
            soma += (i % 2 == 0) ? digito : digito * 3;
        }
        return soma % 10 == 0;
    }
}
