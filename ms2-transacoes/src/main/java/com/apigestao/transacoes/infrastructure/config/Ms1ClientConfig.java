package com.apigestao.transacoes.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class Ms1ClientConfig {

    @Value("${ms1.base-url}")
    private String ms1BaseUrl;

    @Bean
    public RestClient ms1RestClient() {
        return RestClient.builder()
                .baseUrl(ms1BaseUrl)
                .build();
    }
}