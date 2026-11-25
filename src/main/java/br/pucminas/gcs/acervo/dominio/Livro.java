package br.pucminas.gcs.acervo.dominio;

import br.pucminas.gcs.acervo.excecao.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Título do acervo. Controla a quantidade total de exemplares e quantos estão disponíveis para empréstimo.
 */
@Entity
@Table(name = "livro")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(name = "ano_publicacao")
    private Integer anoPublicacao;

    @Column(length = 60)
    private String categoria;

    @Column(name = "exemplares_totais", nullable = false)
    private int exemplaresTotais;

    @Column(name = "exemplares_disponiveis", nullable = false)
    private int exemplaresDisponiveis;

    protected Livro() {
        // exigido pela JPA
    }

    public Livro(String isbn, String titulo, String autor, Integer anoPublicacao, String categoria, int exemplares) {
        if (exemplares < 1) {
            throw new RegraNegocioException("Um livro deve ter pelo menos 1 exemplar");
        }
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.categoria = categoria;
        this.exemplaresTotais = exemplares;
        this.exemplaresDisponiveis = exemplares;
    }

    public boolean isDisponivel() {
        return exemplaresDisponiveis > 0;
    }

    public int getExemplaresEmprestados() {
        return exemplaresTotais - exemplaresDisponiveis;
    }

    /** Retira um exemplar do acervo disponível (saída para empréstimo). */
    public void registrarSaida() {
        if (!isDisponivel()) {
            throw new RegraNegocioException("Não há exemplares disponíveis do livro '" + titulo + "'");
        }
        exemplaresDisponiveis--;
    }

    /** Devolve um exemplar ao acervo disponível. */
    public void registrarRetorno() {
        if (exemplaresDisponiveis >= exemplaresTotais) {
            throw new RegraNegocioException("Todos os exemplares do livro '" + titulo + "' já estão no acervo");
        }
        exemplaresDisponiveis++;
    }

    public void adicionarExemplares(int quantidade) {
        if (quantidade < 1) {
            throw new RegraNegocioException("A quantidade de exemplares adicionados deve ser positiva");
        }
        exemplaresTotais += quantidade;
        exemplaresDisponiveis += quantidade;
    }

    public void atualizarDados(String titulo, String autor, Integer anoPublicacao, String categoria) {
        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public Integer getAnoPublicacao() {
        return anoPublicacao;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getExemplaresTotais() {
        return exemplaresTotais;
    }

    public int getExemplaresDisponiveis() {
        return exemplaresDisponiveis;
    }
}
