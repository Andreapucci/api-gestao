
package com.apigestao.usuarios.domain.ports.input;

import com.apigestao.usuarios.domain.model.Usuario;

public interface CreateUsuarioUseCase {

    Usuario executar(Usuario usuario);
}
