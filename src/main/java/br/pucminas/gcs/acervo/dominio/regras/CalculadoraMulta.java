package br.pucminas.gcs.acervo.dominio.regras;

import br.pucminas.gcs.acervo.config.AcervoProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Calcula a multa por atraso: valor diário x dias de atraso, limitado a um teto.
 */
@Component
public class CalculadoraMulta {

    private final BigDecimal valorDiario;
    private final BigDecimal valorMaximo;

    @Autowired
    public CalculadoraMulta(AcervoProperties propriedades) {
        this(propriedades.multa().valorDiario(), propriedades.multa().valorMaximo());
    }

    public CalculadoraMulta(BigDecimal valorDiario, BigDecimal valorMaximo) {
        if (valorDiario.signum() < 0 || valorMaximo.signum() < 0) {
            throw new IllegalArgumentException("Os valores de multa não podem ser negativos");
        }
        this.valorDiario = valorDiario;
        this.valorMaximo = valorMaximo;
    }

    public BigDecimal calcular(LocalDate dataPrevista, LocalDate dataDevolucao) {
        long diasAtraso = ChronoUnit.DAYS.between(dataPrevista, dataDevolucao);
        if (diasAtraso <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
        BigDecimal multa = valorDiario.multiply(BigDecimal.valueOf(diasAtraso));
        return multa.min(valorMaximo).setScale(2, RoundingMode.HALF_EVEN);
    }

    public BigDecimal getValorDiario() {
        return valorDiario;
    }

    public BigDecimal getValorMaximo() {
        return valorMaximo;
    }
}
