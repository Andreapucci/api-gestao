package com.apigestao.transacoes.infrastructure.adapters.output.brasilapi;

import com.apigestao.transacoes.domain.ports.output.CambioClientPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class BrasilApiCambioClient implements CambioClientPort {

    private final RestClient brasilApiRestClient;

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BrasilApiCambioClient(
            @Qualifier("brasilApiRestClient") RestClient brasilApiRestClient) {
        this.brasilApiRestClient = brasilApiRestClient;
    }

    @Override
    public Optional<Cotacao> buscarCotacao(String moeda) {

        // Tenta a cotação de hoje; se não houver (fim de semana/feriado)
        // ou a API falhar, tenta o dia anterior antes de desistir.
        Optional<Cotacao> cotacaoHoje = buscarCotacaoNaData(
                moeda, LocalDate.now());

        if (cotacaoHoje.isPresent()) {
            return cotacaoHoje;
        }

        return buscarCotacaoNaData(moeda, LocalDate.now().minusDays(1));
    }

    private Optional<Cotacao> buscarCotacaoNaData(
            String moeda, LocalDate data) {

        try {
            BrasilApiResponse resposta = brasilApiRestClient.get()
                    .uri("/api/cambio/v1/cotacao/{moeda}/{data}",
                            moeda, data.format(FORMATO_DATA))
                    .retrieve()
                    .body(BrasilApiResponse.class);

            if (resposta == null
                    || resposta.cotacoes() == null
                    || resposta.cotacoes().isEmpty()) {
                return Optional.empty();
            }

            BigDecimal valorVenda = resposta.cotacoes()
                    .get(0)
                    .cotacaoVenda();

            return Optional.of(
                    new Cotacao(moeda, valorVenda, data)
            );

        } catch (RestClientException e) {
            // A BrasilAPI pode estar fora do ar, lenta ou sem cotação
            // para a data informada. Em qualquer caso, não propagamos
            // o erro: a conversão simplesmente fica indisponível.
            return Optional.empty();
        }
    }

    private record BrasilApiResponse(
            String moeda,
            List<CotacaoItem> cotacoes
    ) {
    }

    private record CotacaoItem(
            BigDecimal cotacaoCompra,
            BigDecimal cotacaoVenda
    ) {
    }
}
