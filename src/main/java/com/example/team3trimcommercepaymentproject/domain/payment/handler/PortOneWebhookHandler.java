package com.example.team3trimcommercepaymentproject.domain.payment.handler;

import com.example.team3trimcommercepaymentproject.domain.payment.portOne.PortOneClient;
import com.example.team3trimcommercepaymentproject.domain.payment.portOne.PortOnePaymentInfo;
import com.example.team3trimcommercepaymentproject.domain.payment.service.PaymentService;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


@Component
@RequiredArgsConstructor
public class PortOneWebhookHandler {

    private final PortOneWebhookVerifer portOneWebhookVerifier;
    private final PortOneClient portOneClient;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public void handle(String signature, String body) {
        portOneWebhookVerifier.verify(signature, body);

        String portonePaymentId = extractPortonePaymentId(body);

        PortOnePaymentInfo paymentInfo = portOneClient.getPayment(portonePaymentId);

        paymentService.processPortOnePaymentResult(portonePaymentId, paymentInfo);
    }


    private String extractPortonePaymentId(String body) {

        try {
            JsonNode root = objectMapper.readTree(body);
            String paymentId = root.path("data").path("paymentId").asText();

            if (paymentId == null || paymentId.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            return paymentId;
        } catch (JacksonException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}


