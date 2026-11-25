package br.pucminas.gcs.acervo.servico;

import br.pucminas.gcs.acervo.dominio.Leitor;
import br.pucminas.gcs.acervo.dominio.TipoLeitor;
import br.pucminas.gcs.acervo.excecao.ConflitoException;
import br.pucminas.gcs.acervo.excecao.RecursoNaoEncontradoException;
import br.pucminas.gcs.acervo.repositorio.LeitorRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de cadastro de leitores.
 */
@Service
@Transactional(readOnly = true)
public class LeitorService {

    private final LeitorRepository leitores;

    public LeitorService(LeitorRepository leitores) {
        this.leitores = leitores;
    }

    @Transactional
    public Leitor cadastrar(String nome, String email, TipoLeitor tipo) {
        String emailNormalizado = email.trim().toLowerCase();
        if (leitores.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ConflitoException("Já existe um leitor cadastrado com o e-mail " + emailNormalizado);
        }
        return leitores.save(new Leitor(nome.trim(), emailNormalizado, tipo));
    }

    public Leitor buscarPorId(Long id) {
        return leitores.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Leitor", id));
    }

    public List<Leitor> listar() {
        return leitores.findAll(Sort.by("nome"));
    }

    @Transactional
    public Leitor desativar(Long id) {
        Leitor leitor = buscarPorId(id);
        leitor.desativar();
        return leitor;
    }

    @Transactional
    public Leitor reativar(Long id) {
        Leitor leitor = buscarPorId(id);
        leitor.reativar();
        return leitor;
    }
}
