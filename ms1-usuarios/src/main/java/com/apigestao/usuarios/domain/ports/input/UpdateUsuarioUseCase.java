
package com.apigestao.usuarios.domain.ports.input;

import com.apigestao.usuarios.domain.model.Usuario;

public interface UpdateUsuarioUseCase {

    Usuario atualizar(Long id, Usuario usuario);
}
