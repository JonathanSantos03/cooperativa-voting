package com.cooperativa.voting.repository;

import com.cooperativa.voting.model.Pauta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PautaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PautaRepository pautaRepository;

    @Test
    void findAllOrderByCreatedAtDesc_DeveRetornarPautasOrdenadasPorDataDecrescente() {
        Pauta pauta1 = new Pauta("Primeira Pauta", "Descrição 1");
        Pauta pauta2 = new Pauta("Segunda Pauta", "Descrição 2");
        Pauta pauta3 = new Pauta("Terceira Pauta", "Descrição 3");

        entityManager.persistAndFlush(pauta1);
        entityManager.persistAndFlush(pauta2);
        entityManager.persistAndFlush(pauta3);

        List<Pauta> pautas = pautaRepository.findAllOrderByCreatedAtDesc();

        assertThat(pautas).hasSize(3);
        assertThat(pautas.get(0).getTitulo()).isEqualTo("Terceira Pauta");
        assertThat(pautas.get(1).getTitulo()).isEqualTo("Segunda Pauta");
        assertThat(pautas.get(2).getTitulo()).isEqualTo("Primeira Pauta");
    }

    @Test
    void existsByTituloIgnoreCase_DeveRetornarTrue_QuandoTituloExiste() {
        Pauta pauta = new Pauta("PAUTA TESTE", "Descrição");
        entityManager.persistAndFlush(pauta);

        boolean exists = pautaRepository.existsByTituloIgnoreCase("pauta teste");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByTituloIgnoreCase_DeveRetornarTrue_QuandoTituloExisteComCaseDiferente() {
        Pauta pauta = new Pauta("Pauta Teste", "Descrição");
        entityManager.persistAndFlush(pauta);

        boolean exists = pautaRepository.existsByTituloIgnoreCase("PAUTA TESTE");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByTituloIgnoreCase_DeveRetornarFalse_QuandoTituloNaoExiste() {
        boolean exists = pautaRepository.existsByTituloIgnoreCase("Pauta Inexistente");

        assertThat(exists).isFalse();
    }

    @Test
    void save_DeveSalvarPautaComSucesso() {
        Pauta pauta = new Pauta("Nova Pauta", "Nova Descrição");

        Pauta savedPauta = pautaRepository.save(pauta);

        assertThat(savedPauta.getId()).isNotNull();
        assertThat(savedPauta.getTitulo()).isEqualTo("Nova Pauta");
        assertThat(savedPauta.getDescricao()).isEqualTo("Nova Descrição");
        assertThat(savedPauta.getCriadoEm()).isNotNull();
    }

    @Test
    void findById_DeveRetornarPauta_QuandoIdExiste() {
        Pauta pauta = new Pauta("Pauta Busca", "Descrição Busca");
        Pauta savedPauta = entityManager.persistAndFlush(pauta);

        var found = pautaRepository.findById(savedPauta.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitulo()).isEqualTo("Pauta Busca");
    }

    @Test
    void findById_DeveRetornarEmpty_QuandoIdNaoExiste() {
        var found = pautaRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void delete_DeveRemoverPauta() {
        Pauta pauta = new Pauta("Pauta Delete", "Descrição Delete");
        Pauta savedPauta = entityManager.persistAndFlush(pauta);

        pautaRepository.delete(savedPauta);
        entityManager.flush();

        var found = pautaRepository.findById(savedPauta.getId());
        assertThat(found).isEmpty();
    }
}