package com.cooperativa.voting.controller;

import com.cooperativa.voting.dto.request.SessaoRequest;
import com.cooperativa.voting.dto.response.ResultadoVotacaoResponse;
import com.cooperativa.voting.dto.response.SessaoResponse;
import com.cooperativa.voting.service.SessaoService;
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

@RestController
@RequestMapping("/api/sessoes")
@Tag(name = "Sessões", description = "Gerenciamento de sessões de votação")
public class SessaoController {
    
    private static final Logger logger = LoggerFactory.getLogger(SessaoController.class);
    
    @Autowired
    private SessaoService sessaoService;
    
    @PostMapping("/pauta/{pautaId}")
    @Operation(summary = "Abrir sessão de votação", description = "Abre uma nova sessão de votação para uma pauta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sessão aberta com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou sessão já existe para a pauta"),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    })
    public ResponseEntity<SessaoResponse> abrirSessao(
            @Parameter(description = "ID da pauta") @PathVariable Long pautaId,
            @Valid @RequestBody SessaoRequest request) {
        
        logger.info("Recebida requisição para abrir sessão - Pauta: {}, Duração: {} min", 
                   pautaId, request.getDuracaoMinutos());
        
        SessaoResponse response = sessaoService.abrirSessao(pautaId, request);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/pauta/{pautaId}")
    @Operation(summary = "Listar sessões por pauta", description = "Retorna todas as sessões de uma pauta específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de sessões retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    })
    public ResponseEntity<List<SessaoResponse>> listarSessoesPorPauta(
            @Parameter(description = "ID da pauta") @PathVariable Long pautaId) {
        
        logger.debug("Recebida requisição para listar sessões da pauta ID: {}", pautaId);
        
        List<SessaoResponse> sessoes = sessaoService.listarSessoesPorPauta(pautaId);
        
        return ResponseEntity.ok(sessoes);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar sessão por ID", description = "Retorna uma sessão específica pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sessão encontrada"),
        @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    public ResponseEntity<SessaoResponse> buscarSessaoPorId(
            @Parameter(description = "ID da sessão") @PathVariable Long id) {
        
        logger.debug("Recebida requisição para buscar sessão ID: {}", id);
        
        SessaoResponse sessao = sessaoService.buscarSessaoPorId(id);
        
        return ResponseEntity.ok(sessao);
    }
    
    @GetMapping("/{id}/resultado")
    @Operation(summary = "Obter resultado da votação", description = "Retorna o resultado completo da votação de uma sessão")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resultado da votação obtido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    public ResponseEntity<ResultadoVotacaoResponse> obterResultadoVotacao(
            @Parameter(description = "ID da sessão") @PathVariable Long id) {
        
        logger.debug("Recebida requisição para obter resultado da sessão ID: {}", id);
        
        ResultadoVotacaoResponse resultado = sessaoService.obterResultadoVotacao(id);
        
        return ResponseEntity.ok(resultado);
    }
    
    @PutMapping("/{id}/encerrar")
    @Operation(summary = "Encerrar sessão", description = "Encerra manualmente uma sessão de votação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sessão encerrada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Sessão já está encerrada"),
        @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    public ResponseEntity<SessaoResponse> encerrarSessao(
            @Parameter(description = "ID da sessão") @PathVariable Long id) {
        
        logger.info("Recebida requisição para encerrar sessão ID: {}", id);
        
        SessaoResponse response = sessaoService.encerrarSessao(id);
        
        return ResponseEntity.ok(response);
    }
}
