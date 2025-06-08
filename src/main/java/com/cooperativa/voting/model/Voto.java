package com.cooperativa.voting.model;

import com.cooperativa.voting.enums.TipoVoto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "votos", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"sessao_id", "associado_id"}))
public class Voto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false)
    private Sessao sessao;
    
    @NotNull(message = "ID do associado é obrigatório")
    @Column(name = "associado_id", nullable = false)
    private String associadoId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVoto tipo;
    
    @Column(name = "votado_em", nullable = false)
    private LocalDateTime votadoEm;
    
    public Voto() {
        this.votadoEm = LocalDateTime.now();
    }
    
    public Voto(Sessao sessao, String associadoId, TipoVoto tipo) {
        this();
        this.sessao = sessao;
        this.associadoId = associadoId;
        this.tipo = tipo;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Sessao getSessao() { return sessao; }
    public void setSessao(Sessao sessao) { this.sessao = sessao; }
    
    public String getAssociadoId() { return associadoId; }
    public void setAssociadoId(String associadoId) { this.associadoId = associadoId; }
    
    public TipoVoto getTipo() { return tipo; }
    public void setTipo(TipoVoto tipo) { this.tipo = tipo; }
    
    public LocalDateTime getVotadoEm() { return votadoEm; }
    public void setVotadoEm(LocalDateTime votadoEm) { this.votadoEm = votadoEm; }
}