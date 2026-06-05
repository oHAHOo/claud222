package com.example.team3trimcommercepaymentproject.domain.payment.controller;

import com.example.team3trimcommercepaymentproject.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PaymentConfirmResponse;
import com.example.team3trimcommercepaymentproject.domain.payment.handler.PortOneWebhookHandler;
import com.example.team3trimcommercepaymentproject.domain.payment.service.PaymentService;
import com.example.team3trimcommercepaymentproject.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PortOneWebhookHandler portOneWebhookHandler;

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirm(
            @AuthenticationPrincipal Long memberId,
            @RequestBody PaymentConfirmRequest request
    ) {

        PaymentConfirmResponse response = paymentService.confirm(memberId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/webhooks/portone")
    public ResponseEntity<ApiResponse<Void>> receiveWebhook(
            @RequestHeader("PortOne-Webhook-Signature") String signature,
            @RequestBody String body
    ) {
        portOneWebhookHandler.handle(signature, body);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}