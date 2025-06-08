package com.cooperativa.voting.dto.request;

import com.cooperativa.voting.enums.TipoVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VotoRequest {
    
    @NotBlank(message = "ID do associado é obrigatório")
    private String associadoId;
    
    @NotNull(message = "Tipo do voto é obrigatório")
    private TipoVoto voto;
    
    public VotoRequest() {}
    
    public VotoRequest(String associadoId, TipoVoto voto) {
        this.associadoId = associadoId;
        this.voto = voto;
    }

    public String getAssociadoId() { return associadoId; }
    public void setAssociadoId(String associadoId) { this.associadoId = associadoId; }
    
    public TipoVoto getVoto() { return voto; }
    public void setVoto(TipoVoto voto) { this.voto = voto; }
}