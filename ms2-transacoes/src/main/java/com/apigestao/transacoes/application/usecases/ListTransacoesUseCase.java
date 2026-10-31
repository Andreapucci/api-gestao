package com.apigestao.transacoes.application.usecases;

import com.apigestao.transacoes.domain.model.TipoTransacao;
import com.apigestao.transacoes.domain.model.Transacao;
import com.apigestao.transacoes.domain.ports.output.TransacaoRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListTransacoesUseCase {

    private final TransacaoRepositoryPort repositoryPort;

    public ListTransacoesUseCase(TransacaoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    public List<Transacao> executar(
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fim,
            TipoTransacao tipo) {

        return repositoryPort.buscarComFiltros(
                usuarioId, inicio, fim, tipo
        );
    }
}