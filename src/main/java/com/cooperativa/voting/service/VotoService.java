package com.cooperativa.voting.service;

import com.cooperativa.voting.dto.request.VotoRequest;
import com.cooperativa.voting.dto.response.VotoResponse;
import com.cooperativa.voting.exception.BusinessException;
import com.cooperativa.voting.exception.ResourceNotFoundException;
import com.cooperativa.voting.exception.VotacaoEncerradaException;
import com.cooperativa.voting.model.Sessao;
import com.cooperativa.voting.model.Voto;
import com.cooperativa.voting.repository.VotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VotoService {
    
    private static final Logger logger = LoggerFactory.getLogger(VotoService.class);
    
    @Autowired
    private VotoRepository votoRepository;
    
    @Autowired
    private SessaoService sessaoService;
    
    public VotoResponse registrarVoto(Long sessaoId, VotoRequest request) {
        logger.info("Registrando voto - Sessão: {}, Associado: {}, Voto: {}", 
                   sessaoId, request.getAssociadoId(), request.getVoto());
        
        Sessao sessao = sessaoService.buscarSessaoEntityPorId(sessaoId);
        
        // Verificar se a sessão está aberta
        if (!sessao.isAberta()) {
            throw new VotacaoEncerradaException(sessaoId);
        }
        
        // Verificar se o associado já votou nesta sessão
        if (votoRepository.existsBySessaoIdAndAssociadoId(sessaoId, request.getAssociadoId())) {
            throw new BusinessException("Este associado já votou nesta pauta");
        }
        
        Voto voto = new Voto(sessao, request.getAssociadoId(), request.getVoto());
        Voto savedVoto = votoRepository.save(voto);
        
        logger.info("Voto registrado com sucesso - ID: {}", savedVoto.getId());
        
        return convertToResponse(savedVoto);
    }
    
    @Transactional(readOnly = true)
    public List<VotoResponse> listarVotosPorSessao(Long sessaoId) {
        logger.debug("Listando votos para sessão ID: {}", sessaoId);
        
        // Verificar se a sessão existe
        sessaoService.buscarSessaoEntityPorId(sessaoId);
        
        return votoRepository.findBySessaoId(sessaoId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<VotoResponse> listarVotosPorAssociado(String associadoId) {
        logger.debug("Listando votos para associado ID: {}", associadoId);
        
        return votoRepository.findByAssociadoIdOrderByVotadoEmDesc(associadoId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public VotoResponse buscarVotoPorId(Long id) {
        logger.debug("Buscando voto por ID: {}", id);
        
        Voto voto = votoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voto", "id", id));
        
        return convertToResponse(voto);
    }
    
    @Transactional(readOnly = true)
    public boolean verificarSePodeVotar(Long sessaoId, String associadoId) {
        logger.debug("Verificando se associado {} pode votar na sessão {}", associadoId, sessaoId);
        
        Sessao sessao = sessaoService.buscarSessaoEntityPorId(sessaoId);
        
        return sessao.isAberta() && 
               !votoRepository.existsBySessaoIdAndAssociadoId(sessaoId, associadoId);
    }
    
    private VotoResponse convertToResponse(Voto voto) {
        return new VotoResponse(
            voto.getId(),
            voto.getSessao().getId(),
            voto.getAssociadoId(),
            voto.getTipo(),
            voto.getVotadoEm()
        );
    }
}