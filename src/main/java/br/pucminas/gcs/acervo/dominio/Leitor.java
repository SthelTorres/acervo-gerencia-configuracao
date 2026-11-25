package br.pucminas.gcs.acervo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Pessoa cadastrada que pode tomar livros emprestados.
 */
@Entity
@Table(name = "leitor")
public class Leitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoLeitor tipo;

    @Column(nullable = false)
    private boolean ativo = true;

    protected Leitor() {
        // exigido pela JPA
    }

    public Leitor(String nome, String email, TipoLeitor tipo) {
        this.nome = nome;
        this.email = email;
        this.tipo = tipo;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public TipoLeitor getTipo() {
        return tipo;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
