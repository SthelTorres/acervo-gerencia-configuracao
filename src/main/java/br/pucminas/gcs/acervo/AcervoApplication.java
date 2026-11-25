package br.pucminas.gcs.acervo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Ponto de entrada da aplicação Acervo - Sistema de Gestão de Biblioteca.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AcervoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcervoApplication.class, args);
    }
}
