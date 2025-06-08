package com.cooperativa.voting.dto.request;

import jakarta.validation.constraints.Min;

public class SessaoRequest {
    
    @Min(value = 1, message = "Duração deve ser pelo menos 1 minuto")
    private Integer duracaoMinutos = 1;
    
    public SessaoRequest() {}
    
    public SessaoRequest(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos != null ? duracaoMinutos : 1;
    }
    
    // Getters and Setters
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { 
        this.duracaoMinutos = duracaoMinutos != null ? duracaoMinutos : 1; 
    }
}
