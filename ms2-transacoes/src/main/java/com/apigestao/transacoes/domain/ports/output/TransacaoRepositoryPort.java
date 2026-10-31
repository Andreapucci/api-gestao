package com.apigestao.transacoes.domain.ports.output;

import com.apigestao.transacoes.domain.model.Transacao;
import com.apigestao.transacoes.domain.model.TipoTransacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepositoryPort {

    Transacao salvar(Transacao transacao);

    Optional<Transacao> buscarPorId(Long id);

    List<Transacao> buscarPorUsuarioId(Long usuarioId);

    List<Transacao> buscarComFiltros(
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fim,
            TipoTransacao tipo
    );
}