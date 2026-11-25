package br.pucminas.gcs.acervo.dominio;

/**
 * Categoria do leitor. Cada categoria define o limite de empréstimos simultâneos e o prazo de devolução.
 */
public enum TipoLeitor {

    ALUNO(3, 14),
    PROFESSOR(5, 30),
    VISITANTE(1, 7);

    private final int limiteEmprestimos;
    private final int prazoDias;

    TipoLeitor(int limiteEmprestimos, int prazoDias) {
        this.limiteEmprestimos = limiteEmprestimos;
        this.prazoDias = prazoDias;
    }

    public int getLimiteEmprestimos() {
        return limiteEmprestimos;
    }

    public int getPrazoDias() {
        return prazoDias;
    }
}
