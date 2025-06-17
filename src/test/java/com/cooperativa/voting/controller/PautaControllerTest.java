package com.cooperativa.voting.controller;

import com.cooperativa.voting.dto.request.PautaRequest;
import com.cooperativa.voting.dto.response.PautaResponse;
import com.cooperativa.voting.exception.BusinessException;
import com.cooperativa.voting.exception.ResourceNotFoundException;
import com.cooperativa.voting.service.PautaService;
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

@WebMvcTest(PautaController.class)
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PautaService pautaService;

    @Autowired
    private ObjectMapper objectMapper;

    private PautaRequest pautaRequest;
    private PautaResponse pautaResponse;

    @BeforeEach
    void setUp() {
        pautaRequest = new PautaRequest();
        pautaRequest.setTitulo("Pauta Teste");
        pautaRequest.setDescricao("Descrição da pauta teste");

        pautaResponse = new PautaResponse(1L, "Pauta Teste", "Descrição da pauta teste", LocalDateTime.now());
    }

    @Test
    void criarPauta_DeveRetornar201_QuandoDadosValidos() throws Exception {
        when(pautaService.criarPauta(any(PautaRequest.class))).thenReturn(pautaResponse);

        mockMvc.perform(post("/api/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pautaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Pauta Teste"))
                .andExpect(jsonPath("$.descricao").value("Descrição da pauta teste"));

        verify(pautaService).criarPauta(any(PautaRequest.class));
    }

    @Test
    void criarPauta_DeveRetornar400_QuandoTituloVazio() throws Exception {
        PautaRequest invalidRequest = new PautaRequest();
        invalidRequest.setTitulo("");
        invalidRequest.setDescricao("Descrição");

        mockMvc.perform(post("/api/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(pautaService, never()).criarPauta(any(PautaRequest.class));
    }

    @Test
    void criarPauta_DeveRetornar400_QuandoTituloJaExiste() throws Exception {
        when(pautaService.criarPauta(any(PautaRequest.class)))
                .thenThrow(new BusinessException("Já existe uma pauta com este título"));

        mockMvc.perform(post("/api/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pautaRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe uma pauta com este título"));

        verify(pautaService).criarPauta(any(PautaRequest.class));
    }

    @Test
    void listarPautas_DeveRetornar200_ComListaPautas() throws Exception {
        List<PautaResponse> pautas = Arrays.asList(pautaResponse);
        when(pautaService.listarPautas()).thenReturn(pautas);

        mockMvc.perform(get("/api/pautas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].titulo").value("Pauta Teste"));

        verify(pautaService).listarPautas();
    }

    @Test
    void buscarPautaPorId_DeveRetornar200_QuandoIdExiste() throws Exception {
        when(pautaService.buscarPautaPorId(1L)).thenReturn(pautaResponse);

        mockMvc.perform(get("/api/pautas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Pauta Teste"));

        verify(pautaService).buscarPautaPorId(1L);
    }

    @Test
    void buscarPautaPorId_DeveRetornar404_QuandoIdNaoExiste() throws Exception {
        when(pautaService.buscarPautaPorId(999L))
                .thenThrow(new ResourceNotFoundException("Pauta", "id", 999L));

        mockMvc.perform(get("/api/pautas/999"))
                .andExpect(status().isNotFound());

        verify(pautaService).buscarPautaPorId(999L);
    }

    @Test
    void atualizarPauta_DeveRetornar200_QuandoDadosValidos() throws Exception {
        when(pautaService.atualizarPauta(eq(1L), any(PautaRequest.class))).thenReturn(pautaResponse);

        mockMvc.perform(put("/api/pautas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pautaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Pauta Teste"));

        verify(pautaService).atualizarPauta(eq(1L), any(PautaRequest.class));
    }

    @Test
    void atualizarPauta_DeveRetornar404_QuandoIdNaoExiste() throws Exception {
        when(pautaService.atualizarPauta(eq(999L), any(PautaRequest.class)))
                .thenThrow(new ResourceNotFoundException("Pauta", "id", 999L));

        mockMvc.perform(put("/api/pautas/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pautaRequest)))
                .andExpect(status().isNotFound());

        verify(pautaService).atualizarPauta(eq(999L), any(PautaRequest.class));
    }

    @Test
    void deletarPauta_DeveRetornar204_QuandoIdExiste() throws Exception {
        doNothing().when(pautaService).deletarPauta(1L);

        mockMvc.perform(delete("/api/pautas/1"))
                .andExpect(status().isNoContent());

        verify(pautaService).deletarPauta(1L);
    }

    @Test
    void deletarPauta_DeveRetornar404_QuandoIdNaoExiste() throws Exception {
        doThrow(new ResourceNotFoundException("Pauta", "id", 999L))
                .when(pautaService).deletarPauta(999L);

        mockMvc.perform(delete("/api/pautas/999"))
                .andExpect(status().isNotFound());

        verify(pautaService).deletarPauta(999L);
    }
}