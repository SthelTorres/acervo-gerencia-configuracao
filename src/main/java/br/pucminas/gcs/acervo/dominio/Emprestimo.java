package br.pucminas.gcs.acervo.dominio;

import br.pucminas.gcs.acervo.excecao.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Registro da retirada de um exemplar de um livro por um leitor.
 */
@Entity
@Table(name = "emprestimo")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livro_id")
    private Livro livro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "leitor_id")
    private Leitor leitor;

    @Column(name = "data_emprestimo", nullable = false)
    private LocalDate dataEmprestimo;

    @Column(name = "data_prevista_devolucao", nullable = false)
    private LocalDate dataPrevistaDevolucao;

    @Column(name = "data_devolucao")
    private LocalDate dataDevolucao;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private StatusEmprestimo status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal multa = BigDecimal.ZERO;

    @Column(nullable = false)
    private int renovacoes;

    protected Emprestimo() {
        // exigido pela JPA
    }

    private Emprestimo(Livro livro, Leitor leitor, LocalDate dataEmprestimo, LocalDate dataPrevistaDevolucao) {
        this.livro = livro;
        this.leitor = leitor;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
        this.status = StatusEmprestimo.ATIVO;
    }

    /**
     * Abre um novo empréstimo e retira um exemplar do acervo disponível.
     */
    public static Emprestimo abrir(Livro livro, Leitor leitor, LocalDate hoje, LocalDate dataPrevistaDevolucao) {
        livro.registrarSaida();
        return new Emprestimo(livro, leitor, hoje, dataPrevistaDevolucao);
    }

    public boolean isAtivo() {
        return status == StatusEmprestimo.ATIVO;
    }

    public boolean estaAtrasado(LocalDate referencia) {
        return isAtivo() && referencia.isAfter(dataPrevistaDevolucao);
    }

    /** Quantidade de dias de atraso em relação à data de referência (nunca negativa). */
    public long diasDeAtraso(LocalDate referencia) {
        long dias = ChronoUnit.DAYS.between(dataPrevistaDevolucao, referencia);
        return Math.max(0, dias);
    }

    /** Encerra o empréstimo, devolvendo o exemplar ao acervo e registrando a multa calculada. */
    public void devolver(LocalDate dataDevolucao, BigDecimal multa) {
        if (!isAtivo()) {
            throw new RegraNegocioException("O empréstimo " + id + " já foi devolvido");
        }
        if (dataDevolucao.isBefore(dataEmprestimo)) {
            throw new RegraNegocioException("A data de devolução não pode ser anterior à data do empréstimo");
        }
        this.dataDevolucao = dataDevolucao;
        this.multa = multa;
        this.status = StatusEmprestimo.DEVOLVIDO;
        livro.registrarRetorno();
    }

    /** Estende o prazo de devolução a partir da data atual. */
    public void renovar(LocalDate novaDataPrevista) {
        this.dataPrevistaDevolucao = novaDataPrevista;
        this.renovacoes++;
    }

    public Long getId() {
        return id;
    }

    public Livro getLivro() {
        return livro;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public StatusEmprestimo getStatus() {
        return status;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    public int getRenovacoes() {
        return renovacoes;
    }
}
