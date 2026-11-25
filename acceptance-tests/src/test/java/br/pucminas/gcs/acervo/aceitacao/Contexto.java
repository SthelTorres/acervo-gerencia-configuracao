package br.pucminas.gcs.acervo.aceitacao;

import io.restassured.response.Response;

/**
 * Estado compartilhado entre os passos de um mesmo cenário (injetado pelo PicoContainer).
 */
public class Contexto {

    Long livroId;
    String isbn;
    Long leitorId;
    Long emprestimoId;
    Response ultimaResposta;
}
