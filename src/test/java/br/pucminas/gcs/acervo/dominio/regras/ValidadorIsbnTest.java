package br.pucminas.gcs.acervo.dominio.regras;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ValidadorIsbn")
class ValidadorIsbnTest {

    @ParameterizedTest(name = "ISBN válido: {0}")
    @ValueSource(strings = {"9780321601919", "978-0-321-60191-9", "0321601912", "080442957X", "080442957x", " 9781484200773 "})
    void deveAceitarIsbnsValidos(String isbn) {
        assertThat(ValidadorIsbn.isValido(isbn)).isTrue();
    }

    @ParameterizedTest(name = "ISBN inválido: {0}")
    @ValueSource(strings = {"9780321601918", "0321601913", "12345", "97803216019190", "ABCDEFGHIJ", "978032160191X", "X321601912"})
    void deveRejeitarIsbnsInvalidos(String isbn) {
        assertThat(ValidadorIsbn.isValido(isbn)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveRejeitarIsbnVazioOuNulo(String isbn) {
        assertThat(ValidadorIsbn.isValido(isbn)).isFalse();
    }

    @Test
    void deveNormalizarRemovendoHifensEspacosEConvertendoX() {
        assertThat(ValidadorIsbn.normalizar("0-8044-2957-x")).isEqualTo("080442957X");
        assertThat(ValidadorIsbn.normalizar("978 0 321 60191 9")).isEqualTo("9780321601919");
        assertThat(ValidadorIsbn.normalizar(null)).isEmpty();
    }
}
