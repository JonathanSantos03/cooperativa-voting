package com.cooperativa.voting.service;

import com.cooperativa.voting.dto.request.SessaoRequest;
import com.cooperativa.voting.dto.response.ResultadoVotacaoResponse;
import com.cooperativa.voting.dto.response.SessaoResponse;
import com.cooperativa.voting.enums.StatusSessao;
import com.cooperativa.voting.enums.TipoVoto;
import com.cooperativa.voting.exception.BusinessException;
import com.cooperativa.voting.exception.ResourceNotFoundException;
import com.cooperativa.voting.model.Pauta;
import com.cooperativa.voting.model.Sessao;
import com.cooperativa.voting.repository.SessaoRepository;
import com.cooperativa.voting.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaService pautaService;

    @InjectMocks
    private SessaoService sessaoService;

    private SessaoRequest sessaoRequest;
    private Pauta pauta;
    private Sessao sessao;

    @BeforeEach
    void setUp() {
        sessaoRequest = new SessaoRequest();
        sessaoRequest.setDuracaoMinutos(60);

        pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta Teste");
        pauta.setDescricao("Descrição da pauta");

        sessao = new Sessao();
        sessao.setId(1L);
        sessao.setPauta(pauta);
        sessao.setInicioEm(LocalDateTime.now());
        sessao.setFimEm(LocalDateTime.now().plusMinutes(60));
        sessao.setStatus(StatusSessao.ABERTA);
    }

    @Test
    void abrirSessao_DeveRetornarSessaoResponse_QuandoDadosValidos() {
        when(pautaService.buscarPautaEntityPorId(1L)).thenReturn(pauta);
        when(sessaoRepository.existsActiveSessaoByPautaId(1L)).thenReturn(false);
        when(sessaoRepository.save(any(Sessao.class))).thenReturn(sessao);

        SessaoResponse response = sessaoService.abrirSessao(1L, sessaoRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPautaId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(StatusSessao.ABERTA);
        
        verify(pautaService).buscarPautaEntityPorId(1L);
        verify(sessaoRepository).existsActiveSessaoByPautaId(1L);
        verify(sessaoRepository).save(any(Sessao.class));
    }

    @Test
    void abrirSessao_DeveLancarBusinessException_QuandoJaExisteSessaoAtiva() {
        when(pautaService.buscarPautaEntityPorId(1L)).thenReturn(pauta);
        when(sessaoRepository.existsActiveSessaoByPautaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> sessaoService.abrirSessao(1L, sessaoRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Já existe uma sessão de votação ativa para esta pauta");

        verify(pautaService).buscarPautaEntityPorId(1L);
        verify(sessaoRepository).existsActiveSessaoByPautaId(1L);
        verify(sessaoRepository, never()).save(any(Sessao.class));
    }

    @Test
    void listarSessoesPorPauta_DeveRetornarLista_QuandoPautaExiste() {
        List<Sessao> sessoes = Arrays.asList(sessao);
        when(pautaService.buscarPautaEntityPorId(1L)).thenReturn(pauta);
        when(sessaoRepository.findByPautaIdOrderByInicioEmDesc(1L)).thenReturn(sessoes);

        List<SessaoResponse> responses = sessaoService.listarSessoesPorPauta(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        
        verify(pautaService).buscarPautaEntityPorId(1L);
        verify(sessaoRepository).findByPautaIdOrderByInicioEmDesc(1L);
    }

    @Test
    void buscarSessaoPorId_DeveRetornarSessaoResponse_QuandoIdExiste() {
        when(sessaoRepository.findById(1L)).thenReturn(Optional.of(sessao));

        SessaoResponse response = sessaoService.buscarSessaoPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPautaId()).isEqualTo(1L);
        
        verify(sessaoRepository).findById(1L);
    }

    @Test
    void buscarSessaoPorId_DeveLancarResourceNotFoundException_QuandoIdNaoExiste() {
        when(sessaoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessaoService.buscarSessaoPorId(999L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(sessaoRepository).findById(999L);
    }

    @Test
    void obterResultadoVotacao_DeveRetornarResultado_QuandoSessaoExiste() {
        when(sessaoRepository.findById(1L)).thenReturn(Optional.of(sessao));
        when(votoRepository.countBySessaoIdAndTipo(1L, TipoVoto.SIM)).thenReturn(5L);
        when(votoRepository.countBySessaoIdAndTipo(1L, TipoVoto.NAO)).thenReturn(3L);

        ResultadoVotacaoResponse response = sessaoService.obterResultadoVotacao(1L);

        assertThat(response).isNotNull();
        assertThat(response.getSessaoId()).isEqualTo(1L);
        assertThat(response.getVotosSim()).isEqualTo(5);
        assertThat(response.getVotosNao()).isEqualTo(3);
        
        verify(sessaoRepository).findById(1L);
        verify(votoRepository).countBySessaoIdAndTipo(1L, TipoVoto.SIM);
        verify(votoRepository).countBySessaoIdAndTipo(1L, TipoVoto.NAO);
    }

    @Test
    void encerrarSessao_DeveRetornarSessaoEncerrada_QuandoSessaoAberta() {
        when(sessaoRepository.findById(1L)).thenReturn(Optional.of(sessao));
        when(sessaoRepository.save(any(Sessao.class))).thenReturn(sessao);

        SessaoResponse response = sessaoService.encerrarSessao(1L);

        assertThat(response).isNotNull();
        
        verify(sessaoRepository).findById(1L);
        verify(sessaoRepository).save(any(Sessao.class));
    }

    @Test
    void encerrarSessao_DeveLancarBusinessException_QuandoSessaoJaEncerrada() {
        sessao.setStatus(StatusSessao.ENCERRADA);
        when(sessaoRepository.findById(1L)).thenReturn(Optional.of(sessao));

        assertThatThrownBy(() -> sessaoService.encerrarSessao(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Esta sessão já está encerrada");

        verify(sessaoRepository).findById(1L);
        verify(sessaoRepository, never()).save(any(Sessao.class));
    }

    @Test
    void encerrarSessoesExpiradas_DeveEncerrarSessoes_QuandoExistemSessoesExpiradas() {
        List<Sessao> sessoesExpiradas = Arrays.asList(sessao);
        when(sessaoRepository.findExpiredSessions(any(LocalDateTime.class))).thenReturn(sessoesExpiradas);

        sessaoService.encerrarSessoesExpiradas();

        verify(sessaoRepository).findExpiredSessions(any(LocalDateTime.class));
        verify(sessaoRepository).saveAll(sessoesExpiradas);
    }

    @Test
    void encerrarSessoesExpiradas_NaoDeveExecutarSave_QuandoNaoExistemSessoesExpiradas() {
        when(sessaoRepository.findExpiredSessions(any(LocalDateTime.class))).thenReturn(Arrays.asList());

        sessaoService.encerrarSessoesExpiradas();

        verify(sessaoRepository).findExpiredSessions(any(LocalDateTime.class));
        verify(sessaoRepository, never()).saveAll(any());
    }
}