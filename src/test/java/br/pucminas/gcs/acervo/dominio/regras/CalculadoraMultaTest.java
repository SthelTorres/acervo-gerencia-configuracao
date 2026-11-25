package br.pucminas.gcs.acervo.dominio.regras;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("CalculadoraMulta")
class CalculadoraMultaTest {

    private static final LocalDate PREVISTA = LocalDate.of(2026, 11, 10);
    private final CalculadoraMulta calculadora = new CalculadoraMulta(new BigDecimal("2.00"), new BigDecimal("50.00"));

    @Test
    void naoCobraMultaQuandoDevolvidoNoPrazo() {
        assertThat(calculadora.calcular(PREVISTA, PREVISTA)).isEqualByComparingTo("0.00");
        assertThat(calculadora.calcular(PREVISTA, PREVISTA.minusDays(3))).isEqualByComparingTo("0.00");
    }

    @ParameterizedTest(name = "{0} dia(s) de atraso => R$ {1}")
    @CsvSource({"1, 2.00", "3, 6.00", "10, 20.00", "25, 50.00", "40, 50.00"})
    void cobraValorDiarioLimitadoAoTeto(int diasAtraso, String esperado) {
        assertThat(calculadora.calcular(PREVISTA, PREVISTA.plusDays(diasAtraso))).isEqualByComparingTo(esperado);
    }

    @Test
    void retornaValorComDuasCasasDecimais() {
        assertThat(calculadora.calcular(PREVISTA, PREVISTA.plusDays(2)).scale()).isEqualTo(2);
    }

    @Test
    void naoAceitaParametrosNegativos() {
        assertThatThrownBy(() -> new CalculadoraMulta(new BigDecimal("-1"), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void expoeParametrosConfigurados() {
        assertThat(calculadora.getValorDiario()).isEqualByComparingTo("2.00");
        assertThat(calculadora.getValorMaximo()).isEqualByComparingTo("50.00");
    }
}
