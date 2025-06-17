package com.cooperativa.voting.service;

import com.cooperativa.voting.dto.request.VotoRequest;
import com.cooperativa.voting.dto.response.VotoResponse;
import com.cooperativa.voting.enums.StatusSessao;
import com.cooperativa.voting.enums.TipoVoto;
import com.cooperativa.voting.exception.BusinessException;
import com.cooperativa.voting.exception.ResourceNotFoundException;
import com.cooperativa.voting.exception.VotacaoEncerradaException;
import com.cooperativa.voting.model.Pauta;
import com.cooperativa.voting.model.Sessao;
import com.cooperativa.voting.model.Voto;
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
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private SessaoService sessaoService;

    @InjectMocks
    private VotoService votoService;

    private VotoRequest votoRequest;
    private Pauta pauta;
    private Sessao sessao;
    private Voto voto;

    @BeforeEach
    void setUp() {
        votoRequest = new VotoRequest();
        votoRequest.setAssociadoId("12345678901");
        votoRequest.setVoto(TipoVoto.SIM);

        pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta Teste");

        sessao = new Sessao();
        sessao.setId(1L);
        sessao.setPauta(pauta);
        sessao.setStatus(StatusSessao.ABERTA);
        sessao.setInicioEm(LocalDateTime.now());
        sessao.setFimEm(LocalDateTime.now().plusMinutes(60));

        voto = new Voto();
        voto.setId(1L);
        voto.setSessao(sessao);
        voto.setAssociadoId("12345678901");
        voto.setTipo(TipoVoto.SIM);
        voto.setVotadoEm(LocalDateTime.now());
    }

    @Test
    void registrarVoto_DeveRetornarVotoResponse_QuandoDadosValidos() {
        when(sessaoService.buscarSessaoEntityPorId(1L)).thenReturn(sessao);
        when(votoRepository.existsBySessaoIdAndAssociadoId(1L, "12345678901")).thenReturn(false);
        when(votoRepository.save(any(Voto.class))).thenReturn(voto);

        VotoResponse response = votoService.registrarVoto(1L, votoRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSessaoId()).isEqualTo(1L);
        assertThat(response.getAssociadoId()).isEqualTo("12345678901");
        assertThat(response.getVoto()).isEqualTo(TipoVoto.SIM);
        
        verify(sessaoService).buscarSessaoEntityPorId(1L);
        verify(votoRepository).existsBySessaoIdAndAssociadoId(1L, "12345678901");
        verify(votoRepository).save(any(Voto.class));
    }

    @Test
    void registrarVoto_DeveLancarVotacaoEncerradaException_QuandoSessaoEncerrada() {
        sessao.setStatus(StatusSessao.ENCERRADA);
        when(sessaoService.buscarSessaoEntityPorId(1L)).thenReturn(sessao);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, votoRequest))
            .isInstanceOf(VotacaoEncerradaException.class);

        verify(sessaoService).buscarSessaoEntityPorId(1L);
        verify(votoRepository, never()).existsBySessaoIdAndAssociadoId(any(), any());
        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void registrarVoto_DeveLancarBusinessException_QuandoAssociadoJaVotou() {
        when(sessaoService.buscarSessaoEntityPorId(1L)).thenReturn(sessao);
        when(votoRepository.existsBySessaoIdAndAssociadoId(1L, "12345678901")).thenReturn(true);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, votoRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Este associado já votou nesta pauta");

        verify(sessaoService).buscarSessaoEntityPorId(1L);
        verify(votoRepository).existsBySessaoIdAndAssociadoId(1L, "12345678901");
        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void listarVotosPorSessao_DeveRetornarLista_QuandoSessaoExiste() {
        List<Voto> votos = Arrays.asList(voto);
        when(sessaoService.buscarSessaoEntityPorId(1L)).thenReturn(sessao);
        when(votoRepository.findBySessaoId(1L)).thenReturn(votos);

        List<VotoResponse> responses = votoService.listarVotosPorSessao(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getAssociadoId()).isEqualTo("12345678901");
        
        verify(sessaoService).buscarSessaoEntityPorId(1L);
        verify(votoRepository).findBySessaoId(1L);
    }

    @Test
    void listarVotosPorAssociado_DeveRetornarLista_ParaAssociadoValido() {
        List<Voto> votos = Arrays.asList(voto);
        when(votoRepository.findByAssociadoIdOrderByVotadoEmDesc("12345678901")).thenReturn(votos);

        List<VotoResponse> responses = votoService.listarVotosPorAssociado("12345678901");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAssociadoId()).isEqualTo("12345678901");
        
        verify(votoRepository).findByAssociadoIdOrderByVotadoEmDesc("12345678901");
    }

    @Test
    void buscarVotoPorId_DeveRetornarVotoResponse_QuandoIdExiste() {
        when(votoRepository.findById(1L)).thenReturn(Optional.of(voto));

        VotoResponse response = votoService.buscarVotoPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAssociadoId()).isEqualTo("12345678901");
        
        verify(votoRepository).findById(1L);
    }

    @Test
    void buscarVotoPorId_DeveLancarResourceNotFoundException_QuandoIdNaoExiste() {
        when(votoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> votoService.buscarVotoPorId(999L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(votoRepository).findById(999L);
    }

    @Test
    void verificarSePodeVotar_DeveRetornarTrue_QuandoSessaoAbertaEAssociadoNaoVotou() {
        when(sessaoService.buscarSessaoEntityPorId(1L)).thenReturn(sessao);
        when(votoRepository.existsBySessaoIdAndAssociadoId(1L, "12345678901")).thenReturn(false);

        boolean podeVotar = votoService.verificarSePodeVotar(1L, "12345678901");

        assertThat(podeVotar).isTrue();
        
        verify(sessaoService).buscarSessaoEntityPorId(1L);
        verify(votoRepository).existsBySessaoIdAndAssociadoId(1L, "12345678901");
    }

    @Test
    void verificarSePodeVotar_DeveRetornarFalse_QuandoSessaoEncerrada() {
        sessao.setStatus(StatusSessao.ENCERRADA);
        when(sessaoService.buscarSessaoEntityPorId(1L)).thenReturn(sessao);

        boolean podeVotar = votoService.verificarSePodeVotar(1L, "12345678901");

        assertThat(podeVotar).isFalse();
        
        verify(sessaoService).buscarSessaoEntityPorId(1L);
    }

    @Test
    void verificarSePodeVotar_DeveRetornarFalse_QuandoAssociadoJaVotou() {
        when(sessaoService.buscarSessaoEntityPorId(1L)).thenReturn(sessao);
        when(votoRepository.existsBySessaoIdAndAssociadoId(1L, "12345678901")).thenReturn(true);

        boolean podeVotar = votoService.verificarSePodeVotar(1L, "12345678901");

        assertThat(podeVotar).isFalse();
        
        verify(sessaoService).buscarSessaoEntityPorId(1L);
        verify(votoRepository).existsBySessaoIdAndAssociadoId(1L, "12345678901");
    }
}