package com.cooperativa.voting.controller;

import com.cooperativa.voting.dto.request.PautaRequest;
import com.cooperativa.voting.dto.response.PautaResponse;
import com.cooperativa.voting.service.PautaService;
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
@RequestMapping("/api/pautas")
@Tag(name = "Pautas", description = "Gerenciamento de pautas para votação")
public class PautaController {
    
    private static final Logger logger = LoggerFactory.getLogger(PautaController.class);
    
    @Autowired
    private PautaService pautaService;
    
    @PostMapping
    @Operation(summary = "Criar nova pauta", description = "Cria uma nova pauta para votação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pauta criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou pauta já existe"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PautaResponse> criarPauta(
            @Valid @RequestBody PautaRequest request) {
        
        logger.info("Recebida requisição para criar pauta: {}", request.getTitulo());
        
        PautaResponse response = pautaService.criarPauta(request);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping
    @Operation(summary = "Listar todas as pautas", description = "Retorna todas as pautas ordenadas por data de criação")
    @ApiResponse(responseCode = "200", description = "Lista de pautas retornada com sucesso")
    public ResponseEntity<List<PautaResponse>> listarPautas() {
        
        logger.debug("Recebida requisição para listar pautas");
        
        List<PautaResponse> pautas = pautaService.listarPautas();
        
        return ResponseEntity.ok(pautas);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar pauta por ID", description = "Retorna uma pauta específica pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pauta encontrada"),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    })
    public ResponseEntity<PautaResponse> buscarPautaPorId(
            @Parameter(description = "ID da pauta") @PathVariable Long id) {
        
        logger.debug("Recebida requisição para buscar pauta ID: {}", id);
        
        PautaResponse pauta = pautaService.buscarPautaPorId(id);
        
        return ResponseEntity.ok(pauta);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pauta", description = "Atualiza uma pauta existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pauta atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    })
    public ResponseEntity<PautaResponse> atualizarPauta(
            @Parameter(description = "ID da pauta") @PathVariable Long id,
            @Valid @RequestBody PautaRequest request) {
        
        logger.info("Recebida requisição para atualizar pauta ID: {}", id);
        
        PautaResponse response = pautaService.atualizarPauta(id, request);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pauta", description = "Remove uma pauta do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pauta deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    })
    public ResponseEntity<Void> deletarPauta(
            @Parameter(description = "ID da pauta") @PathVariable Long id) {
        
        logger.info("Recebida requisição para deletar pauta ID: {}", id);
        
        pautaService.deletarPauta(id);
        
        return ResponseEntity.noContent().build();
    }
}