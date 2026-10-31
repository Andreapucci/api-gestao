package com.apigestao.transacoes.domain.ports.output;

public interface UsuarioClientPort {

    boolean usuarioExiste(Long usuarioId);

}