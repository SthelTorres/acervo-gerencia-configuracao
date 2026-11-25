package br.pucminas.gcs.acervo.web.dto;

public record EstatisticasResponse(long titulos, long exemplares, long exemplaresDisponiveis, long leitoresAtivos,
                                   long emprestimosAtivos, long emprestimosAtrasados, double taxaOcupacao) {
}
