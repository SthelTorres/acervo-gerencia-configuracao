package br.pucminas.gcs.acervo.repositorio;

import br.pucminas.gcs.acervo.dominio.Livro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    boolean existsByIsbn(String isbn);

    Optional<Livro> findByIsbn(String isbn);

    List<Livro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(String titulo, String autor, Sort sort);

    List<Livro> findByCategoriaIgnoreCase(String categoria, Sort sort);

    @Query("select coalesce(sum(l.exemplaresTotais), 0) from Livro l")
    long somarExemplares();

    @Query("select coalesce(sum(l.exemplaresDisponiveis), 0) from Livro l")
    long somarExemplaresDisponiveis();
}
