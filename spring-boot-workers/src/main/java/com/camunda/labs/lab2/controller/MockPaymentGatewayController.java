package com.camunda.labs.lab2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment/process")
public class MockPaymentGatewayController {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGatewayController.class);

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> request) {
        String orderId = (String) request.getOrDefault("orderId", "ORD-UNKNOWN");
        String paymentToken = (String) request.getOrDefault("paymentToken", "tok_visa_valid");
        Number amountNum = (Number) request.getOrDefault("amount", 0.0);
        double amount = amountNum != null ? amountNum.doubleValue() : 0.0;

        log.info("[Payment REST API] Processing charge: orderId={}, amount={}, token={}", orderId, amount, paymentToken);

        // Lab 2 Requirement 9: Simulating gateway downtime incident
        if ("tok_gateway_error".equalsIgnoreCase(paymentToken)) {
            log.warn("[Payment REST API] Simulating 504 Gateway Outage for token: {}", paymentToken);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Payment Gateway Timeout 504");
            error.put("code", "PAYMENT_GATEWAY_DOWN");
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("timestamp", Instant.now().toString());

        if ("tok_declined_funds".equalsIgnoreCase(paymentToken)) {
            response.put("paymentStatus", "DECLINED");
            response.put("transactionId", null);
            response.put("message", "Card declined: Insufficient funds");
        } else {
            response.put("paymentStatus", "SUCCESS");
            response.put("transactionId", "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            response.put("message", "Payment authorized and captured successfully");
        }

        return ResponseEntity.ok(response);
    }
}
