package br.pucminas.gcs.acervo.suporte;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Relógio controlável para testes de integração: permite "avançar o tempo" e simular atrasos.
 */
public class RelogioAjustavel extends Clock {

    private Instant agora;
    private final ZoneId zona;

    public RelogioAjustavel(LocalDate dataInicial, ZoneId zona) {
        this.agora = dataInicial.atStartOfDay(zona).plusHours(10).toInstant();
        this.zona = zona;
    }

    public void avancarDias(long dias) {
        agora = agora.plus(Duration.ofDays(dias));
    }

    public void definir(LocalDate data) {
        agora = data.atStartOfDay(zona).plusHours(10).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return zona;
    }

    @Override
    public Clock withZone(ZoneId novaZona) {
        return Clock.fixed(agora, novaZona);
    }

    @Override
    public Instant instant() {
        return agora;
    }
}
