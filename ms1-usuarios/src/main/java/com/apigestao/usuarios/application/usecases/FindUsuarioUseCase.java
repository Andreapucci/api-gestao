
package com.apigestao.usuarios.domain.ports.input;

import com.apigestao.usuarios.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface FindUsuarioUseCase {

    Optional<Usuario> buscarPorId(Long id);

    List<Usuario> listarTodos();
}
