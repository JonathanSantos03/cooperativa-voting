package com.cooperativa.voting.service;

import com.cooperativa.voting.dto.request.PautaRequest;
import com.cooperativa.voting.dto.response.PautaResponse;
import com.cooperativa.voting.exception.BusinessException;
import com.cooperativa.voting.exception.ResourceNotFoundException;
import com.cooperativa.voting.model.Pauta;
import com.cooperativa.voting.repository.PautaRepository;
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
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private PautaService pautaService;

    private PautaRequest pautaRequest;
    private Pauta pauta;

    @BeforeEach
    void setUp() {
        pautaRequest = new PautaRequest();
        pautaRequest.setTitulo("Teste Pauta");
        pautaRequest.setDescricao("Descrição da pauta de teste");

        pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Teste Pauta");
        pauta.setDescricao("Descrição da pauta de teste");
        pauta.setCriadoEm(LocalDateTime.now());
    }

    @Test
    void criarPauta_DeveRetornarPautaResponse_QuandoDadosValidos() {
        when(pautaRepository.existsByTituloIgnoreCase(pautaRequest.getTitulo())).thenReturn(false);
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);

        PautaResponse response = pautaService.criarPauta(pautaRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitulo()).isEqualTo("Teste Pauta");
        assertThat(response.getDescricao()).isEqualTo("Descrição da pauta de teste");
        
        verify(pautaRepository).existsByTituloIgnoreCase(pautaRequest.getTitulo());
        verify(pautaRepository).save(any(Pauta.class));
    }

    @Test
    void criarPauta_DeveLancarBusinessException_QuandoTituloJaExiste() {
        when(pautaRepository.existsByTituloIgnoreCase(pautaRequest.getTitulo())).thenReturn(true);

        assertThatThrownBy(() -> pautaService.criarPauta(pautaRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Já existe uma pauta com este título");

        verify(pautaRepository).existsByTituloIgnoreCase(pautaRequest.getTitulo());
        verify(pautaRepository, never()).save(any(Pauta.class));
    }

    @Test
    void listarPautas_DeveRetornarListaPautaResponse() {
        List<Pauta> pautas = Arrays.asList(pauta);
        when(pautaRepository.findAllOrderByCreatedAtDesc()).thenReturn(pautas);

        List<PautaResponse> responses = pautaService.listarPautas();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitulo()).isEqualTo("Teste Pauta");
        
        verify(pautaRepository).findAllOrderByCreatedAtDesc();
    }

    @Test
    void buscarPautaPorId_DeveRetornarPautaResponse_QuandoIdExiste() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        PautaResponse response = pautaService.buscarPautaPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitulo()).isEqualTo("Teste Pauta");
        
        verify(pautaRepository).findById(1L);
    }

    @Test
    void buscarPautaPorId_DeveLancarResourceNotFoundException_QuandoIdNaoExiste() {
        when(pautaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pautaService.buscarPautaPorId(999L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(pautaRepository).findById(999L);
    }

    @Test
    void buscarPautaEntityPorId_DeveRetornarPauta_QuandoIdExiste() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        Pauta result = pautaService.buscarPautaEntityPorId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitulo()).isEqualTo("Teste Pauta");
        
        verify(pautaRepository).findById(1L);
    }

    @Test
    void atualizarPauta_DeveRetornarPautaAtualizada_QuandoDadosValidos() {
        PautaRequest updateRequest = new PautaRequest();
        updateRequest.setTitulo("Título Atualizado");
        updateRequest.setDescricao("Descrição Atualizada");

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(pautaRepository.existsByTituloIgnoreCase("Título Atualizado")).thenReturn(false);
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);

        PautaResponse response = pautaService.atualizarPauta(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(pautaRepository).findById(1L);
        verify(pautaRepository).existsByTituloIgnoreCase("Título Atualizado");
        verify(pautaRepository).save(any(Pauta.class));
    }

    @Test
    void atualizarPauta_DeveLancarBusinessException_QuandoNovoTituloJaExiste() {
        PautaRequest updateRequest = new PautaRequest();
        updateRequest.setTitulo("Outro Título");
        updateRequest.setDescricao("Descrição Atualizada");

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(pautaRepository.existsByTituloIgnoreCase("Outro Título")).thenReturn(true);

        assertThatThrownBy(() -> pautaService.atualizarPauta(1L, updateRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Já existe uma pauta com este título");

        verify(pautaRepository).findById(1L);
        verify(pautaRepository).existsByTituloIgnoreCase("Outro Título");
        verify(pautaRepository, never()).save(any(Pauta.class));
    }

    @Test
    void deletarPauta_DeveExecutarSemErro_QuandoIdExiste() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        pautaService.deletarPauta(1L);

        verify(pautaRepository).findById(1L);
        verify(pautaRepository).delete(pauta);
    }

    @Test
    void deletarPauta_DeveLancarResourceNotFoundException_QuandoIdNaoExiste() {
        when(pautaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pautaService.deletarPauta(999L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(pautaRepository).findById(999L);
        verify(pautaRepository, never()).delete(any(Pauta.class));
    }
}