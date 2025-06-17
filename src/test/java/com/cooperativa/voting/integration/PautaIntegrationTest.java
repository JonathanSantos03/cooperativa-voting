package com.cooperativa.voting.integration;

import com.cooperativa.voting.dto.request.PautaRequest;
import com.cooperativa.voting.model.Pauta;
import com.cooperativa.voting.repository.PautaRepository;
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
class PautaIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        pautaRepository.deleteAll();
    }

    @Test
    void devePermitirFluxoCompletoDeGerenciamentoDePautas() throws Exception {
        // 1. Criar uma pauta
        PautaRequest createRequest = new PautaRequest();
        createRequest.setTitulo("Pauta de Integração");
        createRequest.setDescricao("Descrição da pauta de integração");

        String response = mockMvc.perform(post("/api/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Pauta de Integração"))
                .andExpect(jsonPath("$.descricao").value("Descrição da pauta de integração"))
                .andReturn().getResponse().getContentAsString();

        // Extrair ID da resposta
        Long pautaId = objectMapper.readTree(response).get("id").asLong();

        // 2. Buscar a pauta criada
        mockMvc.perform(get("/api/pautas/" + pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pautaId))
                .andExpect(jsonPath("$.titulo").value("Pauta de Integração"));

        // 3. Listar pautas
        mockMvc.perform(get("/api/pautas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Pauta de Integração"));

        // 4. Atualizar a pauta
        PautaRequest updateRequest = new PautaRequest();
        updateRequest.setTitulo("Pauta Atualizada");
        updateRequest.setDescricao("Descrição atualizada");

        mockMvc.perform(put("/api/pautas/" + pautaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Pauta Atualizada"))
                .andExpect(jsonPath("$.descricao").value("Descrição atualizada"));

        // 5. Deletar a pauta
        mockMvc.perform(delete("/api/pautas/" + pautaId))
                .andExpect(status().isNoContent());

        // 6. Verificar que a pauta foi deletada
        mockMvc.perform(get("/api/pautas/" + pautaId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarCriacaoDePautaComTituloJaExistente() throws Exception {
        // Criar primeira pauta
        Pauta pauta = new Pauta("Título Duplicado", "Primeira descrição");
        pautaRepository.save(pauta);

        // Tentar criar segunda pauta com mesmo título
        PautaRequest request = new PautaRequest();
        request.setTitulo("Título Duplicado");
        request.setDescricao("Segunda descrição");

        mockMvc.perform(post("/api/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe uma pauta com este título"));
    }

    @Test
    void deveRejeitarCriacaoDePautaComDadosInvalidos() throws Exception {
        // Título vazio
        PautaRequest invalidRequest = new PautaRequest();
        invalidRequest.setTitulo("");
        invalidRequest.setDescricao("Descrição válida");

        mockMvc.perform(post("/api/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Descrição vazia
        invalidRequest.setTitulo("Título válido");
        invalidRequest.setDescricao("");

        mockMvc.perform(post("/api/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveRetornarNotFoundParaPautaInexistente() throws Exception {
        mockMvc.perform(get("/api/pautas/999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/pautas/999"))
                .andExpect(status().isNotFound());

        PautaRequest request = new PautaRequest();
        request.setTitulo("Título");
        request.setDescricao("Descrição");

        mockMvc.perform(put("/api/pautas/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}