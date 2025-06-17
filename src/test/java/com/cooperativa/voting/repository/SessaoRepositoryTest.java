package com.cooperativa.voting.repository;

import com.cooperativa.voting.enums.StatusSessao;
import com.cooperativa.voting.model.Pauta;
import com.cooperativa.voting.model.Sessao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SessaoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SessaoRepository sessaoRepository;

    private Pauta pauta;

    @BeforeEach
    void setUp() {
        pauta = new Pauta("Pauta Teste", "Descrição da pauta");
        entityManager.persistAndFlush(pauta);
    }

    @Test
    void findByPautaIdOrderByInicioEmDesc_DeveRetornarSessoesOrdenadasPorInicioDecrescente() {
        LocalDateTime agora = LocalDateTime.now();
        
        Sessao sessao1 = new Sessao(pauta, 60);
        sessao1.setInicioEm(agora.minusHours(2));
        
        Sessao sessao2 = new Sessao(pauta, 60);
        sessao2.setInicioEm(agora.minusHours(1));
        
        Sessao sessao3 = new Sessao(pauta, 60);
        sessao3.setInicioEm(agora);

        entityManager.persistAndFlush(sessao1);
        entityManager.persistAndFlush(sessao2);
        entityManager.persistAndFlush(sessao3);

        List<Sessao> sessoes = sessaoRepository.findByPautaIdOrderByInicioEmDesc(pauta.getId());

        assertThat(sessoes).hasSize(3);
        assertThat(sessoes.get(0).getInicioEm()).isEqualTo(agora);
        assertThat(sessoes.get(1).getInicioEm()).isEqualTo(agora.minusHours(1));
        assertThat(sessoes.get(2).getInicioEm()).isEqualTo(agora.minusHours(2));
    }

    @Test
    void findByPautaIdAndStatus_DeveRetornarSessoesPorStatusEspecifico() {
        Sessao sessaoAberta = new Sessao(pauta, 60);
        sessaoAberta.setStatus(StatusSessao.ABERTA);
        
        Sessao sessaoEncerrada = new Sessao(pauta, 60);
        sessaoEncerrada.setStatus(StatusSessao.ENCERRADA);

        entityManager.persistAndFlush(sessaoAberta);
        entityManager.persistAndFlush(sessaoEncerrada);

        List<Sessao> sessoesAbertas = sessaoRepository.findByPautaIdAndStatus(pauta.getId(), StatusSessao.ABERTA);
        List<Sessao> sessoesEncerradas = sessaoRepository.findByPautaIdAndStatus(pauta.getId(), StatusSessao.ENCERRADA);

        assertThat(sessoesAbertas).hasSize(1);
        assertThat(sessoesAbertas.get(0).getStatus()).isEqualTo(StatusSessao.ABERTA);
        
        assertThat(sessoesEncerradas).hasSize(1);
        assertThat(sessoesEncerradas.get(0).getStatus()).isEqualTo(StatusSessao.ENCERRADA);
    }

    @Test
    void findExpiredSessions_DeveRetornarSessoesExpiradas() {
        LocalDateTime agora = LocalDateTime.now();
        
        Sessao sessaoExpirada = new Sessao(pauta, 60);
        sessaoExpirada.setStatus(StatusSessao.ABERTA);
        sessaoExpirada.setFimEm(agora.minusMinutes(30)); // Expirada há 30 minutos
        
        Sessao sessaoAtiva = new Sessao(pauta, 60);
        sessaoAtiva.setStatus(StatusSessao.ABERTA);
        sessaoAtiva.setFimEm(agora.plusMinutes(30)); // Expira em 30 minutos

        entityManager.persistAndFlush(sessaoExpirada);
        entityManager.persistAndFlush(sessaoAtiva);

        List<Sessao> sessoesExpiradas = sessaoRepository.findExpiredSessions(agora);

        assertThat(sessoesExpiradas).hasSize(1);
        assertThat(sessoesExpiradas.get(0).getId()).isEqualTo(sessaoExpirada.getId());
    }

    @Test
    void findActiveSessaoByPautaId_DeveRetornarSessaoAtiva_QuandoExiste() {
        Sessao sessaoAtiva = new Sessao(pauta, 60);
        sessaoAtiva.setStatus(StatusSessao.ABERTA);
        
        Sessao sessaoEncerrada = new Sessao(pauta, 60);
        sessaoEncerrada.setStatus(StatusSessao.ENCERRADA);

        entityManager.persistAndFlush(sessaoAtiva);
        entityManager.persistAndFlush(sessaoEncerrada);

        Optional<Sessao> found = sessaoRepository.findActiveSessaoByPautaId(pauta.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(StatusSessao.ABERTA);
    }

    @Test
    void findActiveSessaoByPautaId_DeveRetornarEmpty_QuandoNaoExisteSessaoAtiva() {
        Sessao sessaoEncerrada = new Sessao(pauta, 60);
        sessaoEncerrada.setStatus(StatusSessao.ENCERRADA);
        entityManager.persistAndFlush(sessaoEncerrada);

        Optional<Sessao> found = sessaoRepository.findActiveSessaoByPautaId(pauta.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void existsActiveSessaoByPautaId_DeveRetornarTrue_QuandoExisteSessaoAtiva() {
        Sessao sessaoAtiva = new Sessao(pauta, 60);
        sessaoAtiva.setStatus(StatusSessao.ABERTA);
        entityManager.persistAndFlush(sessaoAtiva);

        boolean exists = sessaoRepository.existsActiveSessaoByPautaId(pauta.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsActiveSessaoByPautaId_DeveRetornarFalse_QuandoNaoExisteSessaoAtiva() {
        Sessao sessaoEncerrada = new Sessao(pauta, 60);
        sessaoEncerrada.setStatus(StatusSessao.ENCERRADA);
        entityManager.persistAndFlush(sessaoEncerrada);

        boolean exists = sessaoRepository.existsActiveSessaoByPautaId(pauta.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void existsActiveSessaoByPautaId_DeveRetornarFalse_QuandoNaoExisteSessao() {
        boolean exists = sessaoRepository.existsActiveSessaoByPautaId(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void save_DeveSalvarSessaoComSucesso() {
        Sessao sessao = new Sessao(pauta, 60);

        Sessao savedSessao = sessaoRepository.save(sessao);

        assertThat(savedSessao.getId()).isNotNull();
        assertThat(savedSessao.getPauta()).isEqualTo(pauta);
        assertThat(savedSessao.getStatus()).isEqualTo(StatusSessao.ABERTA);
        assertThat(savedSessao.getInicioEm()).isNotNull();
        assertThat(savedSessao.getFimEm()).isNotNull();
    }
}