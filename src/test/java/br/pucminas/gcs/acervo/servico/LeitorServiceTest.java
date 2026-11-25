package br.pucminas.gcs.acervo.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.pucminas.gcs.acervo.dominio.Leitor;
import br.pucminas.gcs.acervo.dominio.TipoLeitor;
import br.pucminas.gcs.acervo.excecao.ConflitoException;
import br.pucminas.gcs.acervo.excecao.RecursoNaoEncontradoException;
import br.pucminas.gcs.acervo.repositorio.LeitorRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeitorService")
class LeitorServiceTest {

    @Mock
    private LeitorRepository leitores;
    @InjectMocks
    private LeitorService service;

    @Test
    void cadastraLeitorComEmailNormalizado() {
        when(leitores.existsByEmailIgnoreCase("ana@exemplo.com")).thenReturn(false);
        when(leitores.save(any(Leitor.class))).thenAnswer(inv -> inv.getArgument(0));

        Leitor leitor = service.cadastrar(" Ana ", " Ana@Exemplo.com ", TipoLeitor.ALUNO);

        assertThat(leitor.getNome()).isEqualTo("Ana");
        assertThat(leitor.getEmail()).isEqualTo("ana@exemplo.com");
        assertThat(leitor.isAtivo()).isTrue();
    }

    @Test
    void rejeitaEmailDuplicado() {
        when(leitores.existsByEmailIgnoreCase("ana@exemplo.com")).thenReturn(true);
        assertThatThrownBy(() -> service.cadastrar("Ana", "ana@exemplo.com", TipoLeitor.ALUNO))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void desativaEReativaLeitor() {
        Leitor leitor = new Leitor("Ana", "ana@exemplo.com", TipoLeitor.ALUNO);
        when(leitores.findById(1L)).thenReturn(Optional.of(leitor));

        assertThat(service.desativar(1L).isAtivo()).isFalse();
        assertThat(service.reativar(1L).isAtivo()).isTrue();
    }

    @Test
    void buscaInexistenteLancaExcecao() {
        when(leitores.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(7L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void listaLeitoresOrdenados() {
        when(leitores.findAll(any(Sort.class))).thenReturn(List.of(new Leitor("Ana", "a@e.com", TipoLeitor.ALUNO)));
        assertThat(service.listar()).hasSize(1);
    }
}
