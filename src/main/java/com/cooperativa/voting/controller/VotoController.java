package com.cooperativa.voting.controller;

import com.cooperativa.voting.dto.request.VotoRequest;
import com.cooperativa.voting.dto.response.VotoResponse;
import com.cooperativa.voting.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/votos")
@Tag(name = "Votos", description = "Gerenciamento de votos em sessões de votação")
public class VotoController {
    
    private static final Logger logger = LoggerFactory.getLogger(VotoController.class);
    
    @Autowired
    private VotoService votoService;
    
    @PostMapping("/sessao/{sessaoId}")
    @Operation(summary = "Registrar voto", description = "Registra um voto de um associado em uma sessão de votação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Voto registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou associado já votou"),
        @ApiResponse(responseCode = "403", description = "Sessão de votação encerrada"),
        @ApiResponse(responseCode = "404", description = "Sessão não encontrada"),
        @ApiResponse(responseCode = "409", description = "Associado já votou nesta pauta")
    })
    public ResponseEntity<VotoResponse> registrarVoto(
            @Parameter(description = "ID da sessão de votação") @PathVariable Long sessaoId,
            @Valid @RequestBody VotoRequest request) {
        
        logger.info("Recebida requisição para registrar voto - Sessão: {}, Associado: {}, Voto: {}", 
                   sessaoId, request.getAssociadoId(), request.getVoto());
        
        VotoResponse response = votoService.registrarVoto(sessaoId, request);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/sessao/{sessaoId}")
    @Operation(summary = "Listar votos por sessão", description = "Retorna todos os votos de uma sessão específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de votos retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    public ResponseEntity<List<VotoResponse>> listarVotosPorSessao(
            @Parameter(description = "ID da sessão") @PathVariable Long sessaoId) {
        
        logger.debug("Recebida requisição para listar votos da sessão ID: {}", sessaoId);
        
        List<VotoResponse> votos = votoService.listarVotosPorSessao(sessaoId);
        
        return ResponseEntity.ok(votos);
    }
    
    @GetMapping("/associado/{associadoId}")
    @Operation(summary = "Listar votos por associado", description = "Retorna todos os votos de um associado específico")
    @ApiResponse(responseCode = "200", description = "Lista de votos retornada com sucesso")
    public ResponseEntity<List<VotoResponse>> listarVotosPorAssociado(
            @Parameter(description = "ID do associado") @PathVariable String associadoId) {
        
        logger.debug("Recebida requisição para listar votos do associado ID: {}", associadoId);
        
        List<VotoResponse> votos = votoService.listarVotosPorAssociado(associadoId);
        
        return ResponseEntity.ok(votos);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar voto por ID", description = "Retorna um voto específico pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Voto encontrado"),
        @ApiResponse(responseCode = "404", description = "Voto não encontrado")
    })
    public ResponseEntity<VotoResponse> buscarVotoPorId(
            @Parameter(description = "ID do voto") @PathVariable Long id) {
        
        logger.debug("Recebida requisição para buscar voto ID: {}", id);
        
        VotoResponse voto = votoService.buscarVotoPorId(id);
        
        return ResponseEntity.ok(voto);
    }
    
    @GetMapping("/sessao/{sessaoId}/associado/{associadoId}/pode-votar")
    @Operation(summary = "Verificar se pode votar", description = "Verifica se um associado pode votar em uma sessão específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    public ResponseEntity<Map<String, Boolean>> verificarSePodeVotar(
            @Parameter(description = "ID da sessão") @PathVariable Long sessaoId,
            @Parameter(description = "ID do associado") @PathVariable String associadoId) {
        
        logger.debug("Recebida requisição para verificar se associado {} pode votar na sessão {}", 
                    associadoId, sessaoId);
        
        boolean podeVotar = votoService.verificarSePodeVotar(sessaoId, associadoId);
        
        return ResponseEntity.ok(Map.of("podeVotar", podeVotar));
    }
}