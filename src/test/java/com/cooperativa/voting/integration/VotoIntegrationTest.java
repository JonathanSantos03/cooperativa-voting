package com.cooperativa.voting.integration;

import com.cooperativa.voting.dto.request.VotoRequest;
import com.cooperativa.voting.enums.TipoVoto;
import com.cooperativa.voting.model.Pauta;
import com.cooperativa.voting.model.Sessao;
import com.cooperativa.voting.repository.PautaRepository;
import com.cooperativa.voting.repository.SessaoRepository;
import com.cooperativa.voting.repository.VotoRepository;
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
class VotoIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private SessaoRepository sessaoRepository;

    @Autowired
    private VotoRepository votoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Pauta pauta;
    private Sessao sessao;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        votoRepository.deleteAll();
        sessaoRepository.deleteAll();
        pautaRepository.deleteAll();

        pauta = new Pauta("Pauta para Votação", "Descrição da pauta");
        pauta = pautaRepository.save(pauta);

        sessao = new Sessao(pauta, 60);
        sessao = sessaoRepository.save(sessao);
    }

    @Test
    void devePermitirFluxoCompletoDeVotacao() throws Exception {
        // 1. Verificar se pode votar
        mockMvc.perform(get("/api/votos/sessao/" + sessao.getId() + "/associado/12345678901/pode-votar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeVotar").value(true));

        // 2. Registrar um voto SIM
        VotoRequest votoSim = new VotoRequest();
        votoSim.setAssociadoId("12345678901");
        votoSim.setVoto(TipoVoto.SIM);

        String response = mockMvc.perform(post("/api/votos/sessao/" + sessao.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoSim)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessaoId").value(sessao.getId()))
                .andExpect(jsonPath("$.associadoId").value("12345678901"))
                .andExpect(jsonPath("$.voto").value("SIM"))
                .andReturn().getResponse().getContentAsString();

        Long votoId = objectMapper.readTree(response).get("id").asLong();

        // 3. Registrar um voto NÃO
        VotoRequest votoNao = new VotoRequest();
        votoNao.setAssociadoId("98765432100");
        votoNao.setVoto(TipoVoto.NAO);

        mockMvc.perform(post("/api/votos/sessao/" + sessao.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoNao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.associadoId").value("98765432100"))
                .andExpect(jsonPath("$.voto").value("NAO"));

        // 4. Verificar que o primeiro associado não pode mais votar
        mockMvc.perform(get("/api/votos/sessao/" + sessao.getId() + "/associado/12345678901/pode-votar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeVotar").value(false));

        // 5. Buscar voto por ID
        mockMvc.perform(get("/api/votos/" + votoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(votoId))
                .andExpect(jsonPath("$.associadoId").value("12345678901"));

        // 6. Listar votos da sessão
        mockMvc.perform(get("/api/votos/sessao/" + sessao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 7. Listar votos do associado
        mockMvc.perform(get("/api/votos/associado/12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].associadoId").value("12345678901"));

        // 8. Verificar resultado da votação
        mockMvc.perform(get("/api/sessoes/" + sessao.getId() + "/resultado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.votosSim").value(1))
                .andExpect(jsonPath("$.votosNao").value(1));
    }

    @Test
    void deveRejeitarVotoDuplicado() throws Exception {
        // Registrar primeiro voto
        VotoRequest voto = new VotoRequest();
        voto.setAssociadoId("12345678901");
        voto.setVoto(TipoVoto.SIM);

        mockMvc.perform(post("/api/votos/sessao/" + sessao.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voto)))
                .andExpect(status().isCreated());

        // Tentar registrar segundo voto do mesmo associado
        mockMvc.perform(post("/api/votos/sessao/" + sessao.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Este associado já votou nesta pauta"));
    }

    @Test
    void deveRejeitarVotoEmSessaoEncerrada() throws Exception {
        // Encerrar a sessão
        sessao.encerrar();
        sessaoRepository.save(sessao);

        VotoRequest voto = new VotoRequest();
        voto.setAssociadoId("12345678901");
        voto.setVoto(TipoVoto.SIM);

        mockMvc.perform(post("/api/votos/sessao/" + sessao.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarVotoComDadosInvalidos() throws Exception {
        // Associado ID vazio
        VotoRequest invalidVoto = new VotoRequest();
        invalidVoto.setAssociadoId("");
        invalidVoto.setVoto(TipoVoto.SIM);

        mockMvc.perform(post("/api/votos/sessao/" + sessao.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidVoto)))
                .andExpect(status().isBadRequest());

        // Tipo de voto nulo
        invalidVoto.setAssociadoId("12345678901");
        invalidVoto.setVoto(null);

        mockMvc.perform(post("/api/votos/sessao/" + sessao.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidVoto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarNotFoundParaSessaoInexistente() throws Exception {
        VotoRequest voto = new VotoRequest();
        voto.setAssociadoId("12345678901");
        voto.setVoto(TipoVoto.SIM);

        mockMvc.perform(post("/api/votos/sessao/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voto)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/votos/sessao/999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/votos/sessao/999/associado/12345678901/pode-votar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundParaVotoInexistente() throws Exception {
        mockMvc.perform(get("/api/votos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarListaVaziaParaAssociadoSemVotos() throws Exception {
        mockMvc.perform(get("/api/votos/associado/99999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void devePermitirMultiplosVotosNaMesmaSessao() throws Exception {
        // Registrar múltiplos votos de diferentes associados
        String[] associados = {"11111111111", "22222222222", "33333333333"};
        TipoVoto[] votos = {TipoVoto.SIM, TipoVoto.NAO, TipoVoto.SIM};

        for (int i = 0; i < associados.length; i++) {
            VotoRequest voto = new VotoRequest();
            voto.setAssociadoId(associados[i]);
            voto.setVoto(votos[i]);

            mockMvc.perform(post("/api/votos/sessao/" + sessao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(voto)))
                    .andExpect(status().isCreated());
        }

        // Verificar resultado
        mockMvc.perform(get("/api/sessoes/" + sessao.getId() + "/resultado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.votosSim").value(2))
                .andExpect(jsonPath("$.votosNao").value(1));

        // Verificar lista de votos
        mockMvc.perform(get("/api/votos/sessao/" + sessao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}