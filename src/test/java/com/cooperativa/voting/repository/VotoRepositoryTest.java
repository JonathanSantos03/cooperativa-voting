package com.cooperativa.voting.repository;

import com.cooperativa.voting.enums.TipoVoto;
import com.cooperativa.voting.model.Pauta;
import com.cooperativa.voting.model.Sessao;
import com.cooperativa.voting.model.Voto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class VotoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VotoRepository votoRepository;

    private Pauta pauta;
    private Sessao sessao;

    @BeforeEach
    void setUp() {
        pauta = new Pauta("Pauta Teste", "Descrição da pauta");
        entityManager.persistAndFlush(pauta);

        sessao = new Sessao(pauta, 60);
        entityManager.persistAndFlush(sessao);
    }

    @Test
    void existsBySessaoIdAndAssociadoId_DeveRetornarTrue_QuandoVotoExiste() {
        Voto voto = new Voto(sessao, "12345678901", TipoVoto.SIM);
        entityManager.persistAndFlush(voto);

        boolean exists = votoRepository.existsBySessaoIdAndAssociadoId(sessao.getId(), "12345678901");

        assertThat(exists).isTrue();
    }

    @Test
    void existsBySessaoIdAndAssociadoId_DeveRetornarFalse_QuandoVotoNaoExiste() {
        boolean exists = votoRepository.existsBySessaoIdAndAssociadoId(sessao.getId(), "99999999999");

        assertThat(exists).isFalse();
    }

    @Test
    void findBySessaoId_DeveRetornarTodosVotosDaSessao() {
        Voto voto1 = new Voto(sessao, "12345678901", TipoVoto.SIM);
        Voto voto2 = new Voto(sessao, "98765432100", TipoVoto.NAO);

        entityManager.persistAndFlush(voto1);
        entityManager.persistAndFlush(voto2);

        List<Voto> votos = votoRepository.findBySessaoId(sessao.getId());

        assertThat(votos).hasSize(2);
        assertThat(votos).extracting(Voto::getAssociadoId)
            .containsExactlyInAnyOrder("12345678901", "98765432100");
    }

    @Test
    void findByAssociadoIdOrderByVotadoEmDesc_DeveRetornarVotosDoAssociadoOrdenadosPorData() {
        Pauta outraPauta = new Pauta("Outra Pauta", "Outra descrição");
        entityManager.persistAndFlush(outraPauta);

        Sessao outraSessao = new Sessao(outraPauta, 60);
        entityManager.persistAndFlush(outraSessao);

        Voto voto1 = new Voto(sessao, "12345678901", TipoVoto.SIM);
        Voto voto2 = new Voto(outraSessao, "12345678901", TipoVoto.NAO);

        entityManager.persistAndFlush(voto1);
        try {
            Thread.sleep(10); // Garantir diferença de tempo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        entityManager.persistAndFlush(voto2);

        List<Voto> votos = votoRepository.findByAssociadoIdOrderByVotadoEmDesc("12345678901");

        assertThat(votos).hasSize(2);
        assertThat(votos.get(0).getVotadoEm()).isAfter(votos.get(1).getVotadoEm());
    }

    @Test
    void countBySessaoIdAndTipo_DeveContarVotosPorTipo() {
        Voto voto1 = new Voto(sessao, "12345678901", TipoVoto.SIM);
        Voto voto2 = new Voto(sessao, "98765432100", TipoVoto.SIM);
        Voto voto3 = new Voto(sessao, "11111111111", TipoVoto.NAO);

        entityManager.persistAndFlush(voto1);
        entityManager.persistAndFlush(voto2);
        entityManager.persistAndFlush(voto3);

        long votosSim = votoRepository.countBySessaoIdAndTipo(sessao.getId(), TipoVoto.SIM);
        long votosNao = votoRepository.countBySessaoIdAndTipo(sessao.getId(), TipoVoto.NAO);

        assertThat(votosSim).isEqualTo(2);
        assertThat(votosNao).isEqualTo(1);
    }

    @Test
    void countBySessaoId_DeveContarTodosVotosDaSessao() {
        Voto voto1 = new Voto(sessao, "12345678901", TipoVoto.SIM);
        Voto voto2 = new Voto(sessao, "98765432100", TipoVoto.NAO);
        Voto voto3 = new Voto(sessao, "11111111111", TipoVoto.SIM);

        entityManager.persistAndFlush(voto1);
        entityManager.persistAndFlush(voto2);
        entityManager.persistAndFlush(voto3);

        long totalVotos = votoRepository.countBySessaoId(sessao.getId());

        assertThat(totalVotos).isEqualTo(3);
    }

    @Test
    void findBySessaoIdAndAssociadoId_DeveRetornarVoto_QuandoExiste() {
        Voto voto = new Voto(sessao, "12345678901", TipoVoto.SIM);
        entityManager.persistAndFlush(voto);

        Optional<Voto> found = votoRepository.findBySessaoIdAndAssociadoId(sessao.getId(), "12345678901");

        assertThat(found).isPresent();
        assertThat(found.get().getAssociadoId()).isEqualTo("12345678901");
        assertThat(found.get().getTipo()).isEqualTo(TipoVoto.SIM);
    }

    @Test
    void findBySessaoIdAndAssociadoId_DeveRetornarEmpty_QuandoNaoExiste() {
        Optional<Voto> found = votoRepository.findBySessaoIdAndAssociadoId(sessao.getId(), "99999999999");

        assertThat(found).isEmpty();
    }

    @Test
    void findByPautaId_DeveRetornarTodosVotosDaPauta() {
        Sessao outraSessao = new Sessao(pauta, 60);
        entityManager.persistAndFlush(outraSessao);

        Voto voto1 = new Voto(sessao, "12345678901", TipoVoto.SIM);
        Voto voto2 = new Voto(outraSessao, "98765432100", TipoVoto.NAO);

        entityManager.persistAndFlush(voto1);
        entityManager.persistAndFlush(voto2);

        List<Voto> votos = votoRepository.findByPautaId(pauta.getId());

        assertThat(votos).hasSize(2);
        assertThat(votos).extracting(Voto::getAssociadoId)
            .containsExactlyInAnyOrder("12345678901", "98765432100");
    }

    @Test
    void existsByPautaIdAndAssociadoId_DeveRetornarTrue_QuandoAssociadoJaVotouNaPauta() {
        Voto voto = new Voto(sessao, "12345678901", TipoVoto.SIM);
        entityManager.persistAndFlush(voto);

        boolean exists = votoRepository.existsByPautaIdAndAssociadoId(pauta.getId(), "12345678901");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByPautaIdAndAssociadoId_DeveRetornarFalse_QuandoAssociadoNaoVotouNaPauta() {
        boolean exists = votoRepository.existsByPautaIdAndAssociadoId(pauta.getId(), "99999999999");

        assertThat(exists).isFalse();
    }

    @Test
    void save_DeveSalvarVotoComSucesso() {
        Voto voto = new Voto(sessao, "12345678901", TipoVoto.SIM);

        Voto savedVoto = votoRepository.save(voto);

        assertThat(savedVoto.getId()).isNotNull();
        assertThat(savedVoto.getSessao()).isEqualTo(sessao);
        assertThat(savedVoto.getAssociadoId()).isEqualTo("12345678901");
        assertThat(savedVoto.getTipo()).isEqualTo(TipoVoto.SIM);
        assertThat(savedVoto.getVotadoEm()).isNotNull();
    }
}