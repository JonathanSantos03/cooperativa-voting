package com.cooperativa.voting.exception;

public class VotacaoEncerradaException extends BusinessException {
    
    public VotacaoEncerradaException(String message) {
        super(message);
    }
    
    public VotacaoEncerradaException(Long sessaoId) {
        super(String.format("A sessão de votação %d está encerrada ou expirou", sessaoId));
    }
}
