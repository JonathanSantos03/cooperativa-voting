package com.cooperativa.voting.service;

import com.cooperativa.voting.dto.request.SessaoRequest;
import com.cooperativa.voting.dto.response.ResultadoVotacaoResponse;
import com.cooperativa.voting.dto.response.SessaoResponse;
import com.cooperativa.voting.enums.StatusSessao;
import com.cooperativa.voting.enums.TipoVoto;
import com.cooperativa.voting.exception.BusinessException;
import com.cooperativa.voting.exception.ResourceNotFoundException;
import com.cooperativa.voting.model.Pauta;
import com.cooperativa.voting.model.Sessao;
import com.cooperativa.voting.repository.SessaoRepository;
import com.cooperativa.voting.repository.VotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessaoService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessaoService.class);
    
    @Autowired
    private SessaoRepository sessaoRepository;
    
    @Autowired
    private VotoRepository votoRepository;
    
    @Autowired
    private PautaService pautaService;
    
    public SessaoResponse abrirSessao(Long pautaId, SessaoRequest request) {
        logger.info("Abrindo sessão para pauta ID: {} com duração: {} minutos", pautaId, request.getDuracaoMinutos());
        
        Pauta pauta = pautaService.buscarPautaEntityPorId(pautaId);
        
        // Verificar se já existe sessão ativa para esta pauta
        if (sessaoRepository.existsActiveSessaoByPautaId(pautaId)) {
            throw new BusinessException("Já existe uma sessão de votação ativa para esta pauta");
        }
        
        Sessao sessao = new Sessao(pauta, request.getDuracaoMinutos());
        Sessao savedSessao = sessaoRepository.save(sessao);
        
        logger.info("Sessão aberta com sucesso - ID: {}, Fim em: {}", 
                   savedSessao.getId(), savedSessao.getFimEm());
        
        return convertToResponse(savedSessao);
    }
    
    @Transactional(readOnly = true)
    public List<SessaoResponse> listarSessoesPorPauta(Long pautaId) {
        logger.debug("Listando sessões para pauta ID: {}", pautaId);
        
        // Verificar se a pauta existe
        pautaService.buscarPautaEntityPorId(pautaId);
        
        return sessaoRepository.findByPautaIdOrderByInicioEmDesc(pautaId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public SessaoResponse buscarSessaoPorId(Long id) {
        logger.debug("Buscando sessão por ID: {}", id);
        
        Sessao sessao = sessaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão", "id", id));
        
        return convertToResponse(sessao);
    }
    
    @Transactional(readOnly = true)
    public Sessao buscarSessaoEntityPorId(Long id) {
        return sessaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão", "id", id));
    }
    
    @Transactional(readOnly = true)
    public ResultadoVotacaoResponse obterResultadoVotacao(Long sessaoId) {
        logger.debug("Obtendo resultado da votação para sessão ID: {}", sessaoId);
        
        Sessao sessao = buscarSessaoEntityPorId(sessaoId);
        
        int votosSim = (int) votoRepository.countBySessaoIdAndTipo(sessaoId, TipoVoto.SIM);
        int votosNao = (int) votoRepository.countBySessaoIdAndTipo(sessaoId, TipoVoto.NAO);
        
        boolean votacaoAberta = sessao.isAberta();
        
        return new ResultadoVotacaoResponse(
            sessao.getId(),
            sessao.getPauta().getId(),
            sessao.getPauta().getTitulo(),
            sessao.getPauta().getDescricao(),
            sessao.getStatus(),
            votacaoAberta,
            sessao.getInicioEm(),
            sessao.getFimEm(),
            votosSim,
            votosNao
        );
    }
    
    public SessaoResponse encerrarSessao(Long id) {
        logger.info("Encerrando sessão ID: {}", id);
        
        Sessao sessao = buscarSessaoEntityPorId(id);
        
        if (sessao.getStatus() == StatusSessao.ENCERRADA) {
            throw new BusinessException("Esta sessão já está encerrada");
        }
        
        sessao.encerrar();
        Sessao updatedSessao = sessaoRepository.save(sessao);
        
        logger.info("Sessão encerrada com sucesso - ID: {}", updatedSessao.getId());
        
        return convertToResponse(updatedSessao);
    }
    
    // Tarefa agendada para encerrar sessões expiradas
    @Scheduled(fixedRate = 60000) // Executa a cada minuto
    public void encerrarSessoesExpiradas() {
        List<Sessao> sessoesExpiradas = sessaoRepository.findExpiredSessions(LocalDateTime.now());
        
        if (!sessoesExpiradas.isEmpty()) {
            logger.info("Encerrando {} sessões expiradas", sessoesExpiradas.size());
            
            sessoesExpiradas.forEach(sessao -> {
                sessao.encerrar();
                logger.debug("Sessão ID {} encerrada automaticamente", sessao.getId());
            });
            
            sessaoRepository.saveAll(sessoesExpiradas);
        }
    }
    
    private SessaoResponse convertToResponse(Sessao sessao) {
        return new SessaoResponse(
            sessao.getId(),
            sessao.getPauta().getId(),
            sessao.getPauta().getTitulo(),
            sessao.getInicioEm(),
            sessao.getFimEm(),
            sessao.getStatus(),
            sessao.isAberta()
        );
    }
}
