package com.cooperativa.voting.dto.response;

import com.cooperativa.voting.enums.TipoVoto;
import java.time.LocalDateTime;

public class VotoResponse {
    
    private Long id;
    private Long sessaoId;
    private String associadoId;
    private TipoVoto voto;
    private LocalDateTime votadoEm;
    
    public VotoResponse() {}
    
    public VotoResponse(Long id, Long sessaoId, String associadoId, 
                       TipoVoto voto, LocalDateTime votadoEm) {
        this.id = id;
        this.sessaoId = sessaoId;
        this.associadoId = associadoId;
        this.voto = voto;
        this.votadoEm = votadoEm;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getSessaoId() { return sessaoId; }
    public void setSessaoId(Long sessaoId) { this.sessaoId = sessaoId; }
    
    public String getAssociadoId() { return associadoId; }
    public void setAssociadoId(String associadoId) { this.associadoId = associadoId; }
    
    public TipoVoto getVoto() { return voto; }
    public void setVoto(TipoVoto voto) { this.voto = voto; }
    
    public LocalDateTime getVotadoEm() { return votadoEm; }
    public void setVotadoEm(LocalDateTime votadoEm) { this.votadoEm = votadoEm; }
}