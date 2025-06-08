package com.cooperativa.voting.dto.response;

import java.time.LocalDateTime;

public class PautaResponse {
    
    private Long id;
    private String titulo;
    private String descricao;
    private LocalDateTime criadoEm;
    
    public PautaResponse() {}
    
    public PautaResponse(Long id, String titulo, String descricao, LocalDateTime criadoEm) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.criadoEm = criadoEm;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}