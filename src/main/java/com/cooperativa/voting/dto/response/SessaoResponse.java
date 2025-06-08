package com.cooperativa.voting.dto.response;

import com.cooperativa.voting.enums.StatusSessao;
import java.time.LocalDateTime;

public class SessaoResponse {
    
    private Long id;
    private Long pautaId;
    private String pautaTitulo;
    private LocalDateTime inicioEm;
    private LocalDateTime fimEm;
    private StatusSessao status;
    private boolean aberta;
    
    public SessaoResponse() {}
    
    public SessaoResponse(Long id, Long pautaId, String pautaTitulo, 
                         LocalDateTime inicioEm, LocalDateTime fimEm, 
                         StatusSessao status, boolean aberta) {
        this.id = id;
        this.pautaId = pautaId;
        this.pautaTitulo = pautaTitulo;
        this.inicioEm = inicioEm;
        this.fimEm = fimEm;
        this.status = status;
        this.aberta = aberta;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getPautaId() { return pautaId; }
    public void setPautaId(Long pautaId) { this.pautaId = pautaId; }
    
    public String getPautaTitulo() { return pautaTitulo; }
    public void setPautaTitulo(String pautaTitulo) { this.pautaTitulo = pautaTitulo; }
    
    public LocalDateTime getInicioEm() { return inicioEm; }
    public void setInicioEm(LocalDateTime inicioEm) { this.inicioEm = inicioEm; }
    
    public LocalDateTime getFimEm() { return fimEm; }
    public void setFimEm(LocalDateTime fimEm) { this.fimEm = fimEm; }
    
    public StatusSessao getStatus() { return status; }
    public void setStatus(StatusSessao status) { this.status = status; }
    
    public boolean isAberta() { return aberta; }
    public void setAberta(boolean aberta) { this.aberta = aberta; }
}