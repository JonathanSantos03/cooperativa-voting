package com.cooperativa.voting.repository;

import com.cooperativa.voting.model.Sessao;
import com.cooperativa.voting.enums.StatusSessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessaoRepository extends JpaRepository<Sessao, Long> {
    
    @Query("SELECT s FROM Sessao s WHERE s.pauta.id = :pautaId ORDER BY s.inicioEm DESC")
    List<Sessao> findByPautaIdOrderByInicioEmDesc(@Param("pautaId") Long pautaId);
    
    @Query("SELECT s FROM Sessao s WHERE s.pauta.id = :pautaId AND s.status = :status")
    List<Sessao> findByPautaIdAndStatus(@Param("pautaId") Long pautaId, @Param("status") StatusSessao status);
    
    @Query("SELECT s FROM Sessao s WHERE s.status = 'ABERTA' AND s.fimEm < :now")
    List<Sessao> findExpiredSessions(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Sessao s WHERE s.pauta.id = :pautaId AND s.status = 'ABERTA' ORDER BY s.inicioEm DESC")
    Optional<Sessao> findActiveSessaoByPautaId(@Param("pautaId") Long pautaId);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Sessao s WHERE s.pauta.id = :pautaId AND s.status = 'ABERTA'")
    boolean existsActiveSessaoByPautaId(@Param("pautaId") Long pautaId);
}