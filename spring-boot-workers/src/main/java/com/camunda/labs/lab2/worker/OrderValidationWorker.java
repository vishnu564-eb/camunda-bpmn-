package com.camunda.labs.lab2.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class OrderValidationWorker {

    private static final Logger log = LoggerFactory.getLogger(OrderValidationWorker.class);

    /**
     * Requirement 3: External job worker using Java and Spring Boot to validate the order,
     * check simulated inventory, and calculate order total.
     */
    @JobWorker(type = "validate-order-inventory", autoComplete = true)
    public Map<String, Object> validateOrderAndStock(final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String orderId = (String) variables.getOrDefault("orderId", "ORD-UNKNOWN");
        String customerId = (String) variables.getOrDefault("customerId", "CUST-UNKNOWN");
        String productId = (String) variables.getOrDefault("productId", "PROD-DEFAULT");
        Number qtyNum = (Number) variables.getOrDefault("quantity", 1);
        Number priceNum = (Number) variables.getOrDefault("unitPrice", 100.0);

        int quantity = qtyNum != null ? qtyNum.intValue() : 1;
        double unitPrice = priceNum != null ? priceNum.doubleValue() : 100.0;

        log.info("[Lab 2] Validating order: id={}, product={}, qty={}, unitPrice={}",
                orderId, productId, quantity, unitPrice);

        // Lab 2 Requirement 9: Simulate service/worker failure for incident resolution in Operate
        if ("CUST-FAIL".equalsIgnoreCase(customerId)) {
            log.error("[Lab 2] Simulating inventory database deadlock incident for customer: {}", customerId);
            throw new RuntimeException("InventoryServiceException: Database deadlock detected while locking stock for " + orderId);
        }

        double orderTotal = quantity * unitPrice;

        // Simulated stock rule: SKU containing 'OOS' or quantity > 50 considered Out of Stock
        String stockStatus = "IN_STOCK";
        if (quantity > 50 || productId.toUpperCase().contains("OOS") || productId.toUpperCase().contains("OUT_OF_STOCK")) {
            stockStatus = "OUT_OF_STOCK";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderTotal", orderTotal);
        result.put("stockStatus", stockStatus);
        result.put("inventoryCheckedAt", Instant.now().toString());

        log.info("[Lab 2] Order processed: total={}, stockStatus={}", orderTotal, stockStatus);
        return result;
    }

    /**
     * Requirement 4: REST / External worker payment processing
     */
    @JobWorker(type = "process-payment-service", autoComplete = true)
    public Map<String, Object> processPayment(final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String orderId = (String) variables.getOrDefault("orderId", "ORD-UNKNOWN");
        String paymentToken = (String) variables.getOrDefault("paymentToken", "tok_visa_valid");
        Number amountNum = (Number) variables.getOrDefault("orderTotal", 100.0);
        double amount = amountNum != null ? amountNum.doubleValue() : 100.0;

        log.info("[Lab 2] Processing payment for order={}, token={}, amount={}", orderId, paymentToken, amount);

        // Simulate Gateway Outage for Operate Incident
        if ("tok_gateway_error".equalsIgnoreCase(paymentToken)) {
            log.error("[Lab 2] Simulating Payment Gateway Timeout 504 for order: {}", orderId);
            throw new RuntimeException("PaymentGatewayException: Gateway Timeout 504 Gateway Unavailable for order " + orderId);
        }

        Map<String, Object> result = new HashMap<>();
        if ("tok_declined_funds".equalsIgnoreCase(paymentToken)) {
            result.put("paymentStatus", "DECLINED");
            result.put("transactionId", null);
        } else {
            result.put("paymentStatus", "SUCCESS");
            result.put("transactionId", "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        log.info("[Lab 2] Payment result: status={}, txnId={}", result.get("paymentStatus"), result.get("transactionId"));
        return result;
    }

    @JobWorker(type = "fulfill-order", autoComplete = true)
    public Map<String, Object> fulfillOrder(final ActivatedJob job) {
        String trackingNumber = "TRK-EXP-" + System.currentTimeMillis();
        log.info("[Lab 2] Order fulfilled! Tracking number assigned: {}", trackingNumber);
        Map<String, Object> result = new HashMap<>();
        result.put("trackingNumber", trackingNumber);
        result.put("shippingStatus", "DISPATCHED");
        return result;
    }

    @JobWorker(type = "notify-out-of-stock", autoComplete = true)
    public void notifyOutOfStock(final ActivatedJob job) {
        log.info("[Lab 2] Notified customer of out of stock condition for instance: {}", job.getProcessInstanceKey());
    }

    @JobWorker(type = "notify-order-rejection", autoComplete = true)
    public void notifyRejection(final ActivatedJob job) {
        log.info("[Lab 2] Notified customer of manager rejection for instance: {}", job.getProcessInstanceKey());
    }

    @JobWorker(type = "notify-payment-failure", autoComplete = true)
    public void notifyPaymentFailure(final ActivatedJob job) {
        log.info("[Lab 2] Notified customer of payment failure for instance: {}", job.getProcessInstanceKey());
    }
}
