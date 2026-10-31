
package com.apigestao.usuarios.application.usecases;

import com.apigestao.usuarios.domain.model.Usuario;

public interface CreateUsuarioUseCase {

    Usuario executar(Usuario usuario);
}
