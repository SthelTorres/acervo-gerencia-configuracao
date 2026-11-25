package br.pucminas.gcs.acervo.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parâmetros de negócio externalizados (itens de configuração).
 * Podem ser alterados por ambiente via application.yml ou variáveis de ambiente,
 * por exemplo ACERVO_MULTA_VALORDIARIO=3.00.
 */
@ConfigurationProperties(prefix = "acervo")
public record AcervoProperties(Multa multa, Emprestimo emprestimo) {

    public AcervoProperties {
        multa = multa == null ? new Multa(new BigDecimal("2.00"), new BigDecimal("50.00")) : multa;
        emprestimo = emprestimo == null ? new Emprestimo(1) : emprestimo;
    }

    /** Valor cobrado por dia de atraso e teto da multa por empréstimo. */
    public record Multa(@DefaultValue("2.00") BigDecimal valorDiario, @DefaultValue("50.00") BigDecimal valorMaximo) {
    }

    /** Regras de empréstimo configuráveis. */
    public record Emprestimo(@DefaultValue("1") int maxRenovacoes) {
    }
}
