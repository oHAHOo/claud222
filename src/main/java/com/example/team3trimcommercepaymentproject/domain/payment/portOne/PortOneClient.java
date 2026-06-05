package com.example.team3trimcommercepaymentproject.domain.payment.portOne;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOneClient {

    private final WebClient portOneWebClient;

    @Value("${portone.store-id}")
    private String storeId;

    public PortOnePaymentInfo getPayment(String portonePaymentId) {
        return portOneWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/{paymentId}")
                        .queryParam("storeId", storeId)
                        .build(portonePaymentId)
                )
                .retrieve()
                .bodyToMono(PortOnePaymentInfo.class)
                .block();
    }

    // portOne으로 결제 취소 post요청
    public void cancelPayment(String paymentId, String reason) {
        String idempotencyKey = UUID.randomUUID().toString();
        Map<String, String> body = Map.of("storeId", storeId, "reason", reason);

        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                portOneWebClient.post()
                    .uri("/payments/{paymentId}/cancel", paymentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", idempotencyKey)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
                return;
            } catch (WebClientRequestException e) {
                // 네트워크 오류 재시도
                log.warn("PortOne 취소 요청 타임아웃 (시도 {}/{})", attempt, maxRetries);
                if (attempt == maxRetries) throw e;
            }
        }
    }

}