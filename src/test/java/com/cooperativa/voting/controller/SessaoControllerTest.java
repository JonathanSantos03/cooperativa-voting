package com.cooperativa.voting.controller;

import com.cooperativa.voting.dto.request.SessaoRequest;
import com.cooperativa.voting.dto.response.ResultadoVotacaoResponse;
import com.cooperativa.voting.dto.response.SessaoResponse;
import com.cooperativa.voting.enums.StatusSessao;
import com.cooperativa.voting.exception.BusinessException;
import com.cooperativa.voting.exception.ResourceNotFoundException;
import com.cooperativa.voting.service.SessaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessaoController.class)
class SessaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessaoService sessaoService;

    @Autowired
    private ObjectMapper objectMapper;

    private SessaoRequest sessaoRequest;
    private SessaoResponse sessaoResponse;
    private ResultadoVotacaoResponse resultadoResponse;

    @BeforeEach
    void setUp() {
        sessaoRequest = new SessaoRequest();
        sessaoRequest.setDuracaoMinutos(60);

        LocalDateTime now = LocalDateTime.now();
        sessaoResponse = new SessaoResponse(1L, 1L, "Pauta Teste", now, now.plusMinutes(60), StatusSessao.ABERTA, true);

        resultadoResponse = new ResultadoVotacaoResponse(1L, 1L, "Pauta Teste", "Descrição", 
                StatusSessao.ABERTA, true, now, now.plusMinutes(60), 5, 3);
    }

    @Test
    void abrirSessao_DeveRetornar201_QuandoDadosValidos() throws Exception {
        when(sessaoService.abrirSessao(eq(1L), any(SessaoRequest.class))).thenReturn(sessaoResponse);

        mockMvc.perform(post("/api/sessoes/pauta/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.pautaId").value(1L))
                .andExpect(jsonPath("$.status").value("ABERTA"));

        verify(sessaoService).abrirSessao(eq(1L), any(SessaoRequest.class));
    }

    @Test
    void abrirSessao_DeveRetornar400_QuandoDuracaoInvalida() throws Exception {
        SessaoRequest invalidRequest = new SessaoRequest();
        invalidRequest.setDuracaoMinutos(0);

        mockMvc.perform(post("/api/sessoes/pauta/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(sessaoService, never()).abrirSessao(any(), any());
    }

    @Test
    void abrirSessao_DeveRetornar400_QuandoJaExisteSessaoAtiva() throws Exception {
        when(sessaoService.abrirSessao(eq(1L), any(SessaoRequest.class)))
                .thenThrow(new BusinessException("Já existe uma sessão de votação ativa para esta pauta"));

        mockMvc.perform(post("/api/sessoes/pauta/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessaoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe uma sessão de votação ativa para esta pauta"));

        verify(sessaoService).abrirSessao(eq(1L), any(SessaoRequest.class));
    }

    @Test
    void abrirSessao_DeveRetornar404_QuandoPautaNaoExiste() throws Exception {
        when(sessaoService.abrirSessao(eq(999L), any(SessaoRequest.class)))
                .thenThrow(new ResourceNotFoundException("Pauta", "id", 999L));

        mockMvc.perform(post("/api/sessoes/pauta/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessaoRequest)))
                .andExpect(status().isNotFound());

        verify(sessaoService).abrirSessao(eq(999L), any(SessaoRequest.class));
    }

    @Test
    void listarSessoesPorPauta_DeveRetornar200_ComListaSessoes() throws Exception {
        List<SessaoResponse> sessoes = Arrays.asList(sessaoResponse);
        when(sessaoService.listarSessoesPorPauta(1L)).thenReturn(sessoes);

        mockMvc.perform(get("/api/sessoes/pauta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].pautaId").value(1L));

        verify(sessaoService).listarSessoesPorPauta(1L);
    }

    @Test
    void buscarSessaoPorId_DeveRetornar200_QuandoIdExiste() throws Exception {
        when(sessaoService.buscarSessaoPorId(1L)).thenReturn(sessaoResponse);

        mockMvc.perform(get("/api/sessoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.pautaId").value(1L));

        verify(sessaoService).buscarSessaoPorId(1L);
    }

    @Test
    void buscarSessaoPorId_DeveRetornar404_QuandoIdNaoExiste() throws Exception {
        when(sessaoService.buscarSessaoPorId(999L))
                .thenThrow(new ResourceNotFoundException("Sessão", "id", 999L));

        mockMvc.perform(get("/api/sessoes/999"))
                .andExpect(status().isNotFound());

        verify(sessaoService).buscarSessaoPorId(999L);
    }

    @Test
    void obterResultadoVotacao_DeveRetornar200_ComResultado() throws Exception {
        when(sessaoService.obterResultadoVotacao(1L)).thenReturn(resultadoResponse);

        mockMvc.perform(get("/api/sessoes/1/resultado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessaoId").value(1L))
                .andExpect(jsonPath("$.votosSim").value(5))
                .andExpect(jsonPath("$.votosNao").value(3));

        verify(sessaoService).obterResultadoVotacao(1L);
    }

    @Test
    void encerrarSessao_DeveRetornar200_QuandoSessaoAberta() throws Exception {
        when(sessaoService.encerrarSessao(1L)).thenReturn(sessaoResponse);

        mockMvc.perform(put("/api/sessoes/1/encerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(sessaoService).encerrarSessao(1L);
    }

    @Test
    void encerrarSessao_DeveRetornar400_QuandoSessaoJaEncerrada() throws Exception {
        when(sessaoService.encerrarSessao(1L))
                .thenThrow(new BusinessException("Esta sessão já está encerrada"));

        mockMvc.perform(put("/api/sessoes/1/encerrar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Esta sessão já está encerrada"));

        verify(sessaoService).encerrarSessao(1L);
    }

    @Test
    void encerrarSessao_DeveRetornar404_QuandoSessaoNaoExiste() throws Exception {
        when(sessaoService.encerrarSessao(999L))
                .thenThrow(new ResourceNotFoundException("Sessão", "id", 999L));

        mockMvc.perform(put("/api/sessoes/999/encerrar"))
                .andExpect(status().isNotFound());

        verify(sessaoService).encerrarSessao(999L);
    }
}