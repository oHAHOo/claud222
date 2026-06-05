package com.example.team3trimcommercepaymentproject.domain.payment.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PortOneWebClientConfig {

    @Value("${portone.api-secret}")
    private String apiSecret;

    @Bean
    public WebClient portOneWebClient() {
        return WebClient
                .builder()
                .baseUrl("https://api.portone.io")
                .defaultHeader("Authorization", "PortOne " + apiSecret)
                .build();
    }
}