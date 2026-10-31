package com.apigestao.transacoes.infrastructure.adapters.output.persistence.repository;

import com.apigestao.transacoes.domain.model.TipoTransacao;
import com.apigestao.transacoes.infrastructure.adapters.output.persistence.entity.TransacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataTransacaoRepository
        extends JpaRepository<TransacaoEntity, Long> {

    // Busca todas as transações de um usuário
    List<TransacaoEntity> findByUsuarioId(Long usuarioId);

    // Busca transações com filtros
    @Query("""
        SELECT t FROM TransacaoEntity t
        WHERE t.usuarioId = :usuarioId
        AND (:inicio IS NULL OR t.dataHora >= :inicio)
        AND (:fim IS NULL OR t.dataHora <= :fim)
        AND (:tipo IS NULL OR t.tipo = :tipo)
    """)
    List<TransacaoEntity> buscarComFiltros(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("tipo") TipoTransacao tipo
    );
}