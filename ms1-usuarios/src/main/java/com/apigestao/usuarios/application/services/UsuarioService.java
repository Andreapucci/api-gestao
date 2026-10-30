
package com.apigestao.usuarios.application.services;

import com.apigestao.usuarios.domain.model.Usuario;
import com.apigestao.usuarios.domain.ports.input.CreateUsuarioUseCase;
import com.apigestao.usuarios.domain.ports.input.DeleteUsuarioUseCase;
import com.apigestao.usuarios.domain.ports.input.FindUsuarioUseCase;
import com.apigestao.usuarios.domain.ports.input.UpdateUsuarioUseCase;
import com.apigestao.usuarios.domain.ports.output.UsuarioRepositoryPort;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements
        CreateUsuarioUseCase,
        FindUsuarioUseCase,
        UpdateUsuarioUseCase,
        DeleteUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepositoryPort usuarioRepositoryPort,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario executar(Usuario usuario) {

        if (usuarioRepositoryPort.existePorEmail(usuario.getEmail())) {
            throw new IllegalArgumentException(
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        String senhaCriptografada =
                passwordEncoder.encode(usuario.getPassword());

        Usuario novoUsuario = Usuario.create(
                usuario.getName(),
                usuario.getEmail(),
                senhaCriptografada
        );

        return usuarioRepositoryPort.salvar(novoUsuario);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepositoryPort.buscarPorId(id);
    }

    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepositoryPort.listarTodos();
    }

    @Override
    public Usuario atualizar(Long id, Usuario usuario) {

        Usuario usuarioExistente = usuarioRepositoryPort
                .buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."
                ));

        Optional<Usuario> usuarioComEmail =
                usuarioRepositoryPort.buscarPorEmail(usuario.getEmail());

        if (usuarioComEmail.isPresent()
                && !usuarioComEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "Este e-mail já está sendo utilizado."
            );
        }

        usuarioExistente.update(
                usuario.getName(),
                usuario.getEmail()
        );

        return usuarioRepositoryPort.salvar(usuarioExistente);
    }

    @Override
    public void excluir(Long id) {

        usuarioRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."
                ));

        usuarioRepositoryPort.excluir(id);
    }
}
