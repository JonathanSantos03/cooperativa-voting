package com.cooperativa.voting.controller;

import com.cooperativa.voting.dto.request.VotoRequest;
import com.cooperativa.voting.dto.response.VotoResponse;
import com.cooperativa.voting.enums.TipoVoto;
import com.cooperativa.voting.exception.BusinessException;
import com.cooperativa.voting.exception.ResourceNotFoundException;
import com.cooperativa.voting.exception.VotacaoEncerradaException;
import com.cooperativa.voting.service.VotoService;
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

@WebMvcTest(VotoController.class)
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VotoService votoService;

    @Autowired
    private ObjectMapper objectMapper;

    private VotoRequest votoRequest;
    private VotoResponse votoResponse;

    @BeforeEach
    void setUp() {
        votoRequest = new VotoRequest();
        votoRequest.setAssociadoId("12345678901");
        votoRequest.setVoto(TipoVoto.SIM);

        votoResponse = new VotoResponse(1L, 1L, "12345678901", TipoVoto.SIM, LocalDateTime.now());
    }

    @Test
    void registrarVoto_DeveRetornar201_QuandoDadosValidos() throws Exception {
        when(votoService.registrarVoto(eq(1L), any(VotoRequest.class))).thenReturn(votoResponse);

        mockMvc.perform(post("/api/votos/sessao/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.sessaoId").value(1L))
                .andExpect(jsonPath("$.associadoId").value("12345678901"))
                .andExpect(jsonPath("$.voto").value("SIM"));

        verify(votoService).registrarVoto(eq(1L), any(VotoRequest.class));
    }

    @Test
    void registrarVoto_DeveRetornar400_QuandoAssociadoIdVazio() throws Exception {
        VotoRequest invalidRequest = new VotoRequest();
        invalidRequest.setAssociadoId("");
        invalidRequest.setVoto(TipoVoto.SIM);

        mockMvc.perform(post("/api/votos/sessao/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(votoService, never()).registrarVoto(any(), any());
    }

    @Test
    void registrarVoto_DeveRetornar400_QuandoAssociadoJaVotou() throws Exception {
        when(votoService.registrarVoto(eq(1L), any(VotoRequest.class)))
                .thenThrow(new BusinessException("Este associado já votou nesta pauta"));

        mockMvc.perform(post("/api/votos/sessao/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Este associado já votou nesta pauta"));

        verify(votoService).registrarVoto(eq(1L), any(VotoRequest.class));
    }

    @Test
    void registrarVoto_DeveRetornar403_QuandoVotacaoEncerrada() throws Exception {
        when(votoService.registrarVoto(eq(1L), any(VotoRequest.class)))
                .thenThrow(new VotacaoEncerradaException(1L));

        mockMvc.perform(post("/api/votos/sessao/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoRequest)))
                .andExpect(status().isForbidden());

        verify(votoService).registrarVoto(eq(1L), any(VotoRequest.class));
    }

    @Test
    void registrarVoto_DeveRetornar404_QuandoSessaoNaoExiste() throws Exception {
        when(votoService.registrarVoto(eq(999L), any(VotoRequest.class)))
                .thenThrow(new ResourceNotFoundException("Sessão", "id", 999L));

        mockMvc.perform(post("/api/votos/sessao/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoRequest)))
                .andExpect(status().isNotFound());

        verify(votoService).registrarVoto(eq(999L), any(VotoRequest.class));
    }

    @Test
    void listarVotosPorSessao_DeveRetornar200_ComListaVotos() throws Exception {
        List<VotoResponse> votos = Arrays.asList(votoResponse);
        when(votoService.listarVotosPorSessao(1L)).thenReturn(votos);

        mockMvc.perform(get("/api/votos/sessao/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].associadoId").value("12345678901"));

        verify(votoService).listarVotosPorSessao(1L);
    }

    @Test
    void listarVotosPorAssociado_DeveRetornar200_ComListaVotos() throws Exception {
        List<VotoResponse> votos = Arrays.asList(votoResponse);
        when(votoService.listarVotosPorAssociado("12345678901")).thenReturn(votos);

        mockMvc.perform(get("/api/votos/associado/12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].associadoId").value("12345678901"));

        verify(votoService).listarVotosPorAssociado("12345678901");
    }

    @Test
    void buscarVotoPorId_DeveRetornar200_QuandoIdExiste() throws Exception {
        when(votoService.buscarVotoPorId(1L)).thenReturn(votoResponse);

        mockMvc.perform(get("/api/votos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.associadoId").value("12345678901"));

        verify(votoService).buscarVotoPorId(1L);
    }

    @Test
    void buscarVotoPorId_DeveRetornar404_QuandoIdNaoExiste() throws Exception {
        when(votoService.buscarVotoPorId(999L))
                .thenThrow(new ResourceNotFoundException("Voto", "id", 999L));

        mockMvc.perform(get("/api/votos/999"))
                .andExpect(status().isNotFound());

        verify(votoService).buscarVotoPorId(999L);
    }

    @Test
    void verificarSePodeVotar_DeveRetornar200_ComTrue_QuandoPodeVotar() throws Exception {
        when(votoService.verificarSePodeVotar(1L, "12345678901")).thenReturn(true);

        mockMvc.perform(get("/api/votos/sessao/1/associado/12345678901/pode-votar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeVotar").value(true));

        verify(votoService).verificarSePodeVotar(1L, "12345678901");
    }

    @Test
    void verificarSePodeVotar_DeveRetornar200_ComFalse_QuandoNaoPodeVotar() throws Exception {
        when(votoService.verificarSePodeVotar(1L, "12345678901")).thenReturn(false);

        mockMvc.perform(get("/api/votos/sessao/1/associado/12345678901/pode-votar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeVotar").value(false));

        verify(votoService).verificarSePodeVotar(1L, "12345678901");
    }

    @Test
    void verificarSePodeVotar_DeveRetornar404_QuandoSessaoNaoExiste() throws Exception {
        when(votoService.verificarSePodeVotar(999L, "12345678901"))
                .thenThrow(new ResourceNotFoundException("Sessão", "id", 999L));

        mockMvc.perform(get("/api/votos/sessao/999/associado/12345678901/pode-votar"))
                .andExpect(status().isNotFound());

        verify(votoService).verificarSePodeVotar(999L, "12345678901");
    }
}