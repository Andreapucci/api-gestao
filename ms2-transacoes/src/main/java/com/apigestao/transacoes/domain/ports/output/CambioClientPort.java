package com.apigestao.transacoes.domain.ports.output;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface CambioClientPort {

    /**
     * Busca a cotação de uma moeda estrangeira em relação ao Real.
     * Retorna Optional.empty() caso a BrasilAPI esteja indisponível,
     * para que a consulta da transação nunca quebre por causa de uma
     * falha externa.
     */
    Optional<Cotacao> buscarCotacao(String moeda);

    record Cotacao(
            String moeda,
            BigDecimal valor,
            LocalDate data
    ) {
    }
}
