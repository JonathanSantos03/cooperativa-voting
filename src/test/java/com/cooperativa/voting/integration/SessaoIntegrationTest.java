package com.cooperativa.voting.integration;

import com.cooperativa.voting.dto.request.SessaoRequest;
import com.cooperativa.voting.model.Pauta;
import com.cooperativa.voting.model.Sessao;
import com.cooperativa.voting.repository.PautaRepository;
import com.cooperativa.voting.repository.SessaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SessaoIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private SessaoRepository sessaoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Pauta pauta;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        sessaoRepository.deleteAll();
        pautaRepository.deleteAll();

        pauta = new Pauta("Pauta para Sessão", "Descrição da pauta");
        pauta = pautaRepository.save(pauta);
    }

    @Test
    void devePermitirFluxoCompletoDeGerenciamentoDeSessoes() throws Exception {
        // 1. Abrir uma sessão
        SessaoRequest createRequest = new SessaoRequest();
        createRequest.setDuracaoMinutos(60);

        String response = mockMvc.perform(post("/api/sessoes/pauta/" + pauta.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pautaId").value(pauta.getId()))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andReturn().getResponse().getContentAsString();

        // Extrair ID da resposta
        Long sessaoId = objectMapper.readTree(response).get("id").asLong();

        // 2. Buscar a sessão criada
        mockMvc.perform(get("/api/sessoes/" + sessaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessaoId))
                .andExpect(jsonPath("$.pautaId").value(pauta.getId()));

        // 3. Listar sessões da pauta
        mockMvc.perform(get("/api/sessoes/pauta/" + pauta.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(sessaoId));

        // 4. Obter resultado da votação
        mockMvc.perform(get("/api/sessoes/" + sessaoId + "/resultado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessaoId").value(sessaoId))
                .andExpect(jsonPath("$.votosSim").value(0))
                .andExpect(jsonPath("$.votosNao").value(0));

        // 5. Encerrar a sessão
        mockMvc.perform(put("/api/sessoes/" + sessaoId + "/encerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessaoId));

        // 6. Tentar encerrar novamente (deve falhar)
        mockMvc.perform(put("/api/sessoes/" + sessaoId + "/encerrar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Esta sessão já está encerrada"));
    }

    @Test
    void deveRejeitarAberturaDeSessaoComSessaoJaAtiva() throws Exception {
        // Criar primeira sessão
        Sessao sessaoAtiva = new Sessao(pauta, 60);
        sessaoRepository.save(sessaoAtiva);

        // Tentar criar segunda sessão para a mesma pauta
        SessaoRequest request = new SessaoRequest();
        request.setDuracaoMinutos(30);

        mockMvc.perform(post("/api/sessoes/pauta/" + pauta.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe uma sessão de votação ativa para esta pauta"));
    }

    @Test
    void deveRejeitarAberturaDeSessaoComDadosInvalidos() throws Exception {
        // Duração inválida (zero)
        SessaoRequest invalidRequest = new SessaoRequest();
        invalidRequest.setDuracaoMinutos(0);

        mockMvc.perform(post("/api/sessoes/pauta/" + pauta.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Duração inválida (negativa)
        invalidRequest.setDuracaoMinutos(-10);

        mockMvc.perform(post("/api/sessoes/pauta/" + pauta.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarNotFoundParaPautaInexistente() throws Exception {
        SessaoRequest request = new SessaoRequest();
        request.setDuracaoMinutos(60);

        mockMvc.perform(post("/api/sessoes/pauta/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/sessoes/pauta/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundParaSessaoInexistente() throws Exception {
        mockMvc.perform(get("/api/sessoes/999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/sessoes/999/resultado"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/sessoes/999/encerrar"))
                .andExpect(status().isNotFound());
    }
}