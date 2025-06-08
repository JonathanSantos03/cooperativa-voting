package com.cooperativa.voting.repository;

import com.cooperativa.voting.model.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PautaRepository extends JpaRepository<Pauta, Long> {
    
    @Query("SELECT p FROM Pauta p ORDER BY p.criadoEm DESC")
    List<Pauta> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Pauta p WHERE UPPER(p.titulo) = UPPER(?1)")
    boolean existsByTituloIgnoreCase(String titulo);
}