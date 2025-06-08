package com.cooperativa.voting.model;

import com.cooperativa.voting.enums.StatusSessao;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sessoes")
public class Sessao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;
    
    @Column(name = "inicio_em", nullable = false)
    private LocalDateTime inicioEm;
    
    @Column(name = "fim_em", nullable = false)
    private LocalDateTime fimEm;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSessao status;
    
    @OneToMany(mappedBy = "sessao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Voto> votos;
    
    public Sessao() {
        this.status = StatusSessao.ABERTA;
        this.inicioEm = LocalDateTime.now();
    }
    
    public Sessao(Pauta pauta, int duracaoMinutos) {
        this();
        this.pauta = pauta;
        this.fimEm = this.inicioEm.plusMinutes(duracaoMinutos);
    }
    
    public boolean isAberta() {
        return status == StatusSessao.ABERTA && LocalDateTime.now().isBefore(fimEm);
    }
    
    public void encerrar() {
        this.status = StatusSessao.ENCERRADA;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Pauta getPauta() { return pauta; }
    public void setPauta(Pauta pauta) { this.pauta = pauta; }
    
    public LocalDateTime getInicioEm() { return inicioEm; }
    public void setInicioEm(LocalDateTime inicioEm) { this.inicioEm = inicioEm; }
    
    public LocalDateTime getFimEm() { return fimEm; }
    public void setFimEm(LocalDateTime fimEm) { this.fimEm = fimEm; }
    
    public StatusSessao getStatus() { return status; }
    public void setStatus(StatusSessao status) { this.status = status; }
    
    public List<Voto> getVotos() { return votos; }
    public void setVotos(List<Voto> votos) { this.votos = votos; }
}