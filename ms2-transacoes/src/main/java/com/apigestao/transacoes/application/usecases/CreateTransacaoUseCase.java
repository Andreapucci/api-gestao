package com.apigestao.transacoes.application.usecases;

import com.apigestao.transacoes.domain.model.Transacao;
import com.apigestao.transacoes.domain.ports.output.TransacaoRepositoryPort;
import com.apigestao.transacoes.domain.ports.output.UsuarioClientPort;
import org.springframework.stereotype.Service;

@Service
public class CreateTransacaoUseCase {

    private final TransacaoRepositoryPort transacaoRepository;
    private final UsuarioClientPort usuarioClient;

    public CreateTransacaoUseCase(
            TransacaoRepositoryPort transacaoRepository,
            UsuarioClientPort usuarioClient) {
        this.transacaoRepository = transacaoRepository;
        this.usuarioClient = usuarioClient;
    }

    public Transacao executar(Transacao transacao) {

        // Verifica se o usuário dono da transação existe no MS1
        if (!usuarioClient.usuarioExiste(transacao.getUsuarioId())) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        // Verifica o destinatário, quando for uma transferência
        if (transacao.getUsuarioDestinoId() != null) {
            if (!usuarioClient.usuarioExiste(transacao.getUsuarioDestinoId())) {
                throw new IllegalArgumentException("Usuário destinatário não encontrado.");
            }
        }

        // Salva a transação
        return transacaoRepository.salvar(transacao);
    }
}