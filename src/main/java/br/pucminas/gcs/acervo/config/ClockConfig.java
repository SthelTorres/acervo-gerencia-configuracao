package br.pucminas.gcs.acervo.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fornece o relógio da aplicação. Injetar o Clock torna as regras baseadas em datas testáveis.
 */
@Configuration
public class ClockConfig {

    public static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");

    @Bean
    public Clock clock() {
        return Clock.system(FUSO_HORARIO);
    }
}
