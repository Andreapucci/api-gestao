
package com.apigestao.transacoes.infrastructure.adapters.output.brasilapi;

import com.apigestao.transacoes.domain.ports.output.CambioClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BrasilApiCambioClientTest {

    private MockRestServiceServer servidor;
    private BrasilApiCambioClient client;

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost");

        servidor = MockRestServiceServer.bindTo(builder).build();

        client = new BrasilApiCambioClient(builder.build());
    }

    @Test
    void deveRetornarCotacaoQuandoApiResponderComSucesso() {
        LocalDate hoje = LocalDate.now();

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/USD/"
                                + hoje.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "moeda": "USD",
                            "data": "%s",
                            "cotacoes": [
                                {
                                    "cotacao_compra": 5.40,
                                    "cotacao_venda": 5.50
                                },
                                {
                                    "cotacao_compra": 5.41,
                                    "cotacao_venda": 5.51
                                }
                            ]
                        }
                        """.formatted(hoje), MediaType.APPLICATION_JSON));

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("USD");

        assertTrue(resultado.isPresent());
        assertEquals("USD", resultado.get().moeda());
        assertEquals(new BigDecimal("5.51"), resultado.get().valor());
        assertEquals(hoje, resultado.get().data());

        servidor.verify();
    }

    @Test
    void deveBuscarCotacaoDoDiaAnteriorQuandoHojeNaoPossuirCotacao() {
        LocalDate hoje = LocalDate.now();
        LocalDate ontem = hoje.minusDays(1);

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/EUR/"
                                + hoje.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withResourceNotFound());

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/EUR/"
                                + ontem.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "moeda": "EUR",
                            "data": "%s",
                            "cotacoes": [
                                {
                                    "cotacao_compra": 6.00,
                                    "cotacao_venda": 6.10
                                }
                            ]
                        }
                        """.formatted(ontem), MediaType.APPLICATION_JSON));

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("EUR");

        assertTrue(resultado.isPresent());
        assertEquals(new BigDecimal("6.10"), resultado.get().valor());
        assertEquals(ontem, resultado.get().data());

        servidor.verify();
    }

    @Test
    void deveRetornarVazioQuandoNaoEncontrarCotacaoNosUltimosSeteDias() {
        LocalDate hoje = LocalDate.now();

        for (int dias = 0; dias < 7; dias++) {
            LocalDate data = hoje.minusDays(dias);

            servidor.expect(requestTo(
                            "http://localhost/api/cambio/v1/cotacao/USD/"
                                    + data.format(FORMATO)))
                    .andExpect(method(GET))
                    .andRespond(withResourceNotFound());
        }

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("USD");

        assertTrue(resultado.isEmpty());

        servidor.verify();
    }

    @Test
    void deveIgnorarCotacaoComValorZero() {
        LocalDate hoje = LocalDate.now();

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/USD/"
                                + hoje.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "data": "%s",
                            "cotacoes": [
                                {
                                    "cotacao_compra": 0,
                                    "cotacao_venda": 0
                                }
                            ]
                        }
                        """.formatted(hoje), MediaType.APPLICATION_JSON));

        for (int dias = 1; dias < 7; dias++) {
            LocalDate data = hoje.minusDays(dias);

            servidor.expect(requestTo(
                            "http://localhost/api/cambio/v1/cotacao/USD/"
                                    + data.format(FORMATO)))
                    .andExpect(method(GET))
                    .andRespond(withResourceNotFound());
        }

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("USD");

        assertTrue(resultado.isEmpty());

        servidor.verify();
    }

    @Test
    void deveIgnorarCotacaoComValorNegativo() {
        LocalDate hoje = LocalDate.now();

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/USD/"
                                + hoje.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "data": "%s",
                            "cotacoes": [
                                {
                                    "cotacao_compra": 5.00,
                                    "cotacao_venda": -1.00
                                }
                            ]
                        }
                        """.formatted(hoje), MediaType.APPLICATION_JSON));

        for (int dias = 1; dias < 7; dias++) {
            LocalDate data = hoje.minusDays(dias);

            servidor.expect(requestTo(
                            "http://localhost/api/cambio/v1/cotacao/USD/"
                                    + data.format(FORMATO)))
                    .andExpect(method(GET))
                    .andRespond(withResourceNotFound());
        }

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("USD");

        assertTrue(resultado.isEmpty());

        servidor.verify();
    }

    @Test
    void deveUsarDataConsultadaQuandoRespostaNaoInformarData() {
        LocalDate hoje = LocalDate.now();

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/USD/"
                                + hoje.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "moeda": "USD",
                            "cotacoes": [
                                {
                                    "cotacao_compra": 5.00,
                                    "cotacao_venda": 5.25
                                }
                            ]
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("USD");

        assertTrue(resultado.isPresent());
        assertEquals(hoje, resultado.get().data());
        assertEquals(new BigDecimal("5.25"), resultado.get().valor());

        servidor.verify();
    }
}
