package com.apigestao.transacoes.application;

import com.apigestao.transacoes.application.usecases.CreateTransacaoUseCase;
import com.apigestao.transacoes.domain.model.StatusTransacao;
import com.apigestao.transacoes.domain.model.TipoTransacao;
import com.apigestao.transacoes.domain.model.Transacao;
import com.apigestao.transacoes.domain.ports.output.ContaClientPort;
import com.apigestao.transacoes.domain.ports.output.TransacaoRepositoryPort;
import com.apigestao.transacoes.domain.ports.output.UsuarioClientPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class CreateTransacaoUseCaseTest {

    @Mock
    private TransacaoRepositoryPort transacaoRepository;

    @Mock
    private UsuarioClientPort usuarioClient;

    @Mock
    private ContaClientPort contaClient;

    private CreateTransacaoUseCase createTransacaoUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createTransacaoUseCase = new CreateTransacaoUseCase(
                transacaoRepository, usuarioClient, contaClient);

        when(usuarioClient.usuarioExiste(anyLong())).thenReturn(true);

        when(transacaoRepository.salvar(any(Transacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transacaoRepository.buscarPorUsuarioId(anyLong()))
                .thenReturn(List.of());
    }

    private ContaClientPort.ContaDados contaComSaldo(BigDecimal saldo) {
        return new ContaClientPort.ContaDados(
                "1", 1L, saldo, new BigDecimal("1000"), new BigDecimal("500"));
    }

    // 1. Saque maior que o saldo deve ser rejeitado, com motivo, e ficar gravado.
    @Test
    void deveRejeitarSaqueMaiorQueOSaldo() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setValor(new BigDecimal("200"));
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("100")));

        Transacao resultado = createTransacaoUseCase.executar(transacao);

        assertEquals(StatusTransacao.REJEITADA, resultado.getStatus());
        assertEquals("Saldo insuficiente.", resultado.getMotivoRejeicao());
    }

    // 2. Compra dentro do saldo e dos limites deve ser aprovada.
    @Test
    void deveAprovarCompraDentroDoSaldoELimites() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.COMPRA);
        transacao.setValor(new BigDecimal("50"));
        transacao.setCategoria("Mercado");
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("1000")));

        Transacao resultado = createTransacaoUseCase.executar(transacao);

        assertEquals(StatusTransacao.APROVADA, resultado.getStatus());
        assertNull(resultado.getMotivoRejeicao());
    }

    // 3. Compra acima do limite por transação deve ser rejeitada.
    @Test
    void deveRejeitarCompraAcimaDoLimitePorTransacao() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.COMPRA);
        transacao.setValor(new BigDecimal("600")); // limite por transação é 500
        transacao.setCategoria("Eletrônicos");
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("1000")));

        Transacao resultado = createTransacaoUseCase.executar(transacao);

        assertEquals(StatusTransacao.REJEITADA, resultado.getStatus());
        assertEquals("O valor ultrapassa o limite por transação.", resultado.getMotivoRejeicao());
    }

    // 4. Depósito não passa pela validação de limites: sempre aprovado.
    @Test
    void depositoNaoDeveValidarLimites() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.DEPOSITO);
        transacao.setValor(new BigDecimal("10000")); // acima do "limite", mas é depósito
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("0")));

        Transacao resultado = createTransacaoUseCase.executar(transacao);

        assertEquals(StatusTransacao.APROVADA, resultado.getStatus());
    }

    // 5. Transferência exige usuário destinatário.
    @Test
    void transferenciaSemDestinatarioDeveLancarExcecao() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.TRANSFERENCIA);
        transacao.setValor(new BigDecimal("100"));
        transacao.setUsuarioId(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> createTransacaoUseCase.executar(transacao)
        );
    }

    // 6. Compra sem categoria deve lançar exceção.
    @Test
    void compraSemCategoriaDeveLancarExcecao() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.COMPRA);
        transacao.setValor(new BigDecimal("100"));
        transacao.setUsuarioId(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> createTransacaoUseCase.executar(transacao)
        );
    }
}
