package br.pucminas.gcs.acervo.repositorio;

import br.pucminas.gcs.acervo.dominio.Leitor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeitorRepository extends JpaRepository<Leitor, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Leitor> findByEmailIgnoreCase(String email);

    long countByAtivoTrue();
}
