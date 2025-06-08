package com.cooperativa.voting.service;

import com.cooperativa.voting.dto.request.PautaRequest;
import com.cooperativa.voting.dto.response.PautaResponse;
import com.cooperativa.voting.exception.BusinessException;
import com.cooperativa.voting.exception.ResourceNotFoundException;
import com.cooperativa.voting.model.Pauta;
import com.cooperativa.voting.repository.PautaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PautaService {
    
    private static final Logger logger = LoggerFactory.getLogger(PautaService.class);
    
    @Autowired
    private PautaRepository pautaRepository;
    
    public PautaResponse criarPauta(PautaRequest request) {
        logger.info("Criando nova pauta: {}", request.getTitulo());
        
        // Validar se já existe pauta com o mesmo título
        if (pautaRepository.existsByTituloIgnoreCase(request.getTitulo())) {
            throw new BusinessException("Já existe uma pauta com este título");
        }
        
        Pauta pauta = new Pauta(request.getTitulo(), request.getDescricao());
        Pauta savedPauta = pautaRepository.save(pauta);
        
        logger.info("Pauta criada com sucesso - ID: {}", savedPauta.getId());
        
        return convertToResponse(savedPauta);
    }
    
    @Transactional(readOnly = true)
    public List<PautaResponse> listarPautas() {
        logger.debug("Listando todas as pautas");
        
        return pautaRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PautaResponse buscarPautaPorId(Long id) {
        logger.debug("Buscando pauta por ID: {}", id);
        
        Pauta pauta = pautaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pauta", "id", id));
        
        return convertToResponse(pauta);
    }
    
    @Transactional(readOnly = true)
    public Pauta buscarPautaEntityPorId(Long id) {
        return pautaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pauta", "id", id));
    }
    
    public PautaResponse atualizarPauta(Long id, PautaRequest request) {
        logger.info("Atualizando pauta ID: {}", id);
        
        Pauta pauta = buscarPautaEntityPorId(id);
        
        // Validar se já existe outra pauta com o mesmo título
        if (!pauta.getTitulo().equalsIgnoreCase(request.getTitulo()) && 
            pautaRepository.existsByTituloIgnoreCase(request.getTitulo())) {
            throw new BusinessException("Já existe uma pauta com este título");
        }
        
        pauta.setTitulo(request.getTitulo());
        pauta.setDescricao(request.getDescricao());
        
        Pauta updatedPauta = pautaRepository.save(pauta);
        
        logger.info("Pauta atualizada com sucesso - ID: {}", updatedPauta.getId());
        
        return convertToResponse(updatedPauta);
    }
    
    public void deletarPauta(Long id) {
        logger.info("Deletando pauta ID: {}", id);
        
        Pauta pauta = buscarPautaEntityPorId(id);
        pautaRepository.delete(pauta);
        
        logger.info("Pauta deletada com sucesso - ID: {}", id);
    }
    
    private PautaResponse convertToResponse(Pauta pauta) {
        return new PautaResponse(
            pauta.getId(),
            pauta.getTitulo(),
            pauta.getDescricao(),
            pauta.getCriadoEm()
        );
    }
}
