package com.cooperativa.voting.dto.response;

import com.cooperativa.voting.enums.StatusSessao;
import java.time.LocalDateTime;

public class ResultadoVotacaoResponse {
    
    private Long sessaoId;
    private Long pautaId;
    private String pautaTitulo;
    private String pautaDescricao;
    private StatusSessao statusSessao;
    private boolean votacaoAberta;
    private LocalDateTime inicioVotacao;
    private LocalDateTime fimVotacao;
    private int totalVotos;
    private int votosSim;
    private int votosNao;
    private Double percentualSim;
    private Double percentualNao;
    
    public ResultadoVotacaoResponse() {}
    
    public ResultadoVotacaoResponse(Long sessaoId, Long pautaId, String pautaTitulo, 
                                  String pautaDescricao, StatusSessao statusSessao, 
                                  boolean votacaoAberta, LocalDateTime inicioVotacao, 
                                  LocalDateTime fimVotacao, int votosSim, int votosNao) {
        this.sessaoId = sessaoId;
        this.pautaId = pautaId;
        this.pautaTitulo = pautaTitulo;
        this.pautaDescricao = pautaDescricao;
        this.statusSessao = statusSessao;
        this.votacaoAberta = votacaoAberta;
        this.inicioVotacao = inicioVotacao;
        this.fimVotacao = fimVotacao;
        this.votosSim = votosSim;
        this.votosNao = votosNao;
        this.totalVotos = votosSim + votosNao;
        
        if (totalVotos > 0) {
            this.percentualSim = (double) votosSim / totalVotos * 100;
            this.percentualNao = (double) votosNao / totalVotos * 100;
        } else {
            this.percentualSim = 0.0;
            this.percentualNao = 0.0;
        }
    }
    
    public Long getSessaoId() { return sessaoId; }
    public void setSessaoId(Long sessaoId) { this.sessaoId = sessaoId; }
    
    public Long getPautaId() { return pautaId; }
    public void setPautaId(Long pautaId) { this.pautaId = pautaId; }
    
    public String getPautaTitulo() { return pautaTitulo; }
    public void setPautaTitulo(String pautaTitulo) { this.pautaTitulo = pautaTitulo; }
    
    public String getPautaDescricao() { return pautaDescricao; }
    public void setPautaDescricao(String pautaDescricao) { this.pautaDescricao = pautaDescricao; }
    
    public StatusSessao getStatusSessao() { return statusSessao; }
    public void setStatusSessao(StatusSessao statusSessao) { this.statusSessao = statusSessao; }
    
    public boolean isVotacaoAberta() { return votacaoAberta; }
    public void setVotacaoAberta(boolean votacaoAberta) { this.votacaoAberta = votacaoAberta; }
    
    public LocalDateTime getInicioVotacao() { return inicioVotacao; }
    public void setInicioVotacao(LocalDateTime inicioVotacao) { this.inicioVotacao = inicioVotacao; }
    
    public LocalDateTime getFimVotacao() { return fimVotacao; }
    public void setFimVotacao(LocalDateTime fimVotacao) { this.fimVotacao = fimVotacao; }
    
    public int getTotalVotos() { return totalVotos; }
    public void setTotalVotos(int totalVotos) { this.totalVotos = totalVotos; }
    
    public int getVotosSim() { return votosSim; }
    public void setVotosSim(int votosSim) { this.votosSim = votosSim; }
    
    public int getVotosNao() { return votosNao; }
    public void setVotosNao(int votosNao) { this.votosNao = votosNao; }
    
    public Double getPercentualSim() { return percentualSim; }
    public void setPercentualSim(Double percentualSim) { this.percentualSim = percentualSim; }
    
    public Double getPercentualNao() { return percentualNao; }
    public void setPercentualNao(Double percentualNao) { this.percentualNao = percentualNao; }
}
