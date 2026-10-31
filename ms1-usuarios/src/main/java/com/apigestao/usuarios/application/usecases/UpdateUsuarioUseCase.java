
package com.apigestao.usuarios.application.usecases;

import com.apigestao.usuarios.domain.model.Usuario;

public interface UpdateUsuarioUseCase {

    Usuario atualizar(Long id, Usuario usuario);
}
