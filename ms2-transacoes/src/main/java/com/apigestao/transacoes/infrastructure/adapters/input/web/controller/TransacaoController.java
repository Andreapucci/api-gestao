package com.apigestao.transacoes.infrastructure.adapters.input.web.controller;

import com.apigestao.transacoes.domain.model.TipoTransacao;
import java.time.LocalDateTime;
import com.apigestao.transacoes.application.usecases.CreateTransacaoUseCase;
import com.apigestao.transacoes.application.usecases.FindTransacaoUseCase;
import com.apigestao.transacoes.application.usecases.ListTransacoesUseCase;
import com.apigestao.transacoes.domain.model.Transacao;
import com.apigestao.transacoes.infrastructure.adapters.input.web.dto.CreateTransacaoRequestDTO;
import com.apigestao.transacoes.infrastructure.adapters.input.web.dto.TransacaoResponseDTO;
import com.apigestao.transacoes.infrastructure.adapters.input.web.mapper.TransacaoWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final CreateTransacaoUseCase createUseCase;
    private final FindTransacaoUseCase findUseCase;
    private final ListTransacoesUseCase listUseCase;
    private final TransacaoWebMapper mapper;

    public TransacaoController(
            CreateTransacaoUseCase createUseCase,
            FindTransacaoUseCase findUseCase,
            ListTransacoesUseCase listUseCase,
            TransacaoWebMapper mapper) {
        this.createUseCase = createUseCase;
        this.findUseCase = findUseCase;
        this.listUseCase = listUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(
            @Valid @RequestBody CreateTransacaoRequestDTO dto,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        Transacao transacao = mapper.toDomain(dto);
        transacao.setUsuarioId(usuarioId);

        Transacao criada = createUseCase.executar(transacao);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(criada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return findUseCase.executar(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim,
            @RequestParam(required = false) TipoTransacao tipo) {

        LocalDateTime dataInicio = inicio != null
                ? inicio.atStartOfDay()
                : null;

        LocalDateTime dataFim = fim != null
                ? fim.atTime(LocalTime.MAX)
                : null;

        List<TransacaoResponseDTO> resposta = listUseCase.executar(
                        usuarioId, dataInicio, dataFim, tipo)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(resposta);
    }
}