package com.cooperativa.voting.repository;

import com.cooperativa.voting.model.Voto;
import com.cooperativa.voting.enums.TipoVoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {
    
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Voto v WHERE v.sessao.id = :sessaoId AND v.associadoId = :associadoId")
    boolean existsBySessaoIdAndAssociadoId(@Param("sessaoId") Long sessaoId, @Param("associadoId") String associadoId);
    
    @Query("SELECT v FROM Voto v WHERE v.sessao.id = :sessaoId")
    List<Voto> findBySessaoId(@Param("sessaoId") Long sessaoId);
    
    @Query("SELECT v FROM Voto v WHERE v.associadoId = :associadoId ORDER BY v.votadoEm DESC")
    List<Voto> findByAssociadoIdOrderByVotadoEmDesc(@Param("associadoId") String associadoId);
    
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.sessao.id = :sessaoId AND v.tipo = :tipo")
    long countBySessaoIdAndTipo(@Param("sessaoId") Long sessaoId, @Param("tipo") TipoVoto tipo);
    
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.sessao.id = :sessaoId")
    long countBySessaoId(@Param("sessaoId") Long sessaoId);
    
    @Query("SELECT v FROM Voto v WHERE v.sessao.id = :sessaoId AND v.associadoId = :associadoId")
    Optional<Voto> findBySessaoIdAndAssociadoId(@Param("sessaoId") Long sessaoId, @Param("associadoId") String associadoId);
    
    @Query("SELECT v FROM Voto v WHERE v.sessao.pauta.id = :pautaId")
    List<Voto> findByPautaId(@Param("pautaId") Long pautaId);
    
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Voto v WHERE v.sessao.pauta.id = :pautaId AND v.associadoId = :associadoId")
    boolean existsByPautaIdAndAssociadoId(@Param("pautaId") Long pautaId, @Param("associadoId") String associadoId);
}