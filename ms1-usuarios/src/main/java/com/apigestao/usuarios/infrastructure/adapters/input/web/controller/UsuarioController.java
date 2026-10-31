package com.apigestao.usuarios.infrastructure.adapters.input.web.controller;

import com.apigestao.usuarios.domain.model.Usuario;
import com.apigestao.usuarios.application.usecases.AutenticarUsuarioUseCase;
import com.apigestao.usuarios.application.usecases.CreateUsuarioUseCase;
import com.apigestao.usuarios.application.usecases.DeleteUsuarioUseCase;
import com.apigestao.usuarios.application.usecases.FindUsuarioUseCase;
import com.apigestao.usuarios.application.usecases.UpdateUsuarioUseCase;
import com.apigestao.usuarios.infrastructure.adapters.input.web.dto.AutenticarRequestDTO;
import com.apigestao.usuarios.infrastructure.adapters.input.web.dto.CreateUsuarioRequestDTO;
import com.apigestao.usuarios.infrastructure.adapters.input.web.dto.UsuarioResponseDTO;
import com.apigestao.usuarios.infrastructure.adapters.input.web.mapper.UsuarioWebMapper;
import com.apigestao.usuarios.application.services.ImportarUsuariosCsvService;
import com.apigestao.usuarios.infrastructure.adapters.input.web.dto.ImportacaoUsuarioResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.apigestao.usuarios.infrastructure.config.exception.UsuarioNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuários")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final CreateUsuarioUseCase createUsuarioUseCase;
    private final FindUsuarioUseCase findUsuarioUseCase;
    private final UpdateUsuarioUseCase updateUsuarioUseCase;
    private final DeleteUsuarioUseCase deleteUsuarioUseCase;
    private final UsuarioWebMapper usuarioWebMapper;
    private final ImportarUsuariosCsvService importarUsuariosCsvService;
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    public UsuarioController(CreateUsuarioUseCase createUsuarioUseCase, FindUsuarioUseCase findUsuarioUseCase, UpdateUsuarioUseCase updateUsuarioUseCase, DeleteUsuarioUseCase deleteUsuarioUseCase, UsuarioWebMapper usuarioWebMapper, ImportarUsuariosCsvService importarUsuariosCsvService, AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        this.createUsuarioUseCase = createUsuarioUseCase;
        this.findUsuarioUseCase = findUsuarioUseCase;
        this.updateUsuarioUseCase = updateUsuarioUseCase;
        this.deleteUsuarioUseCase = deleteUsuarioUseCase;
        this.usuarioWebMapper = usuarioWebMapper;
        this.importarUsuariosCsvService = importarUsuariosCsvService;
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody CreateUsuarioRequestDTO request) {
        Usuario usuario = usuarioWebMapper.toDomain(request);

        Usuario usuarioCriado = createUsuarioUseCase.executar(usuario);

        UsuarioResponseDTO response = usuarioWebMapper.toResponse(usuarioCriado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacaoUsuarioResponseDTO> importarUsuarios(@RequestPart("arquivo") MultipartFile arquivo) {

        ImportacaoUsuarioResponseDTO resultado = importarUsuariosCsvService.importar(arquivo);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {

        List<UsuarioResponseDTO> usuarios = findUsuarioUseCase.listarTodos().stream().map(usuarioWebMapper::toResponse).toList();

        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {

        Usuario usuario = findUsuarioUseCase.buscarPorId(id).orElseThrow(() -> new UsuarioNotFoundException(id));

        return ResponseEntity.ok(usuarioWebMapper.toResponse(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CreateUsuarioRequestDTO request) {
        Usuario usuario = usuarioWebMapper.toDomain(request);

        Usuario usuarioAtualizado = updateUsuarioUseCase.atualizar(id, usuario);

        return ResponseEntity.ok(usuarioWebMapper.toResponse(usuarioAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        deleteUsuarioUseCase.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/autenticar")
    public ResponseEntity<UsuarioResponseDTO> autenticar(
            @Valid @RequestBody AutenticarRequestDTO request) {

        return autenticarUsuarioUseCase
                .autenticar(request.getEmail(), request.getPassword())
                .map(usuarioWebMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
