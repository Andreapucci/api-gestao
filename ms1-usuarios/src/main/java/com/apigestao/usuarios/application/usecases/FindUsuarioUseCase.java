
package com.apigestao.usuarios.application.usecases;

import com.apigestao.usuarios.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface FindUsuarioUseCase {

    Optional<Usuario> buscarPorId(Long id);

    List<Usuario> listarTodos();
}
