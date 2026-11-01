package com.camunda.labs.lab1.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/credit-assessment")
public class MockCreditAssessmentController {

    private static final Logger log = LoggerFactory.getLogger(MockCreditAssessmentController.class);

    @PostMapping
    public ResponseEntity<?> assessCredit(@RequestBody Map<String, Object> request) {
        String applicantId = (String) request.getOrDefault("applicantId", "APP-UNKNOWN");
        String applicantName = (String) request.getOrDefault("applicantName", "Anonymous");
        Number incomeNum = (Number) request.getOrDefault("monthlyIncome", 0.0);
        double monthlyIncome = incomeNum != null ? incomeNum.doubleValue() : 0.0;

        log.info("[REST API] Credit assessment request received for applicantId={}, income={}", applicantId, monthlyIncome);

        // Lab 1 Requirement 9: Intentionally cause one service failure for incident investigation
        if ("FAIL_TEST".equalsIgnoreCase(applicantId)) {
            log.warn("[REST API] Simulating 500 downstream failure for applicant: {}", applicantId);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Simulated Credit Service Outage 500");
            error.put("code", "CREDIT_BUREAU_DOWN");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        int creditScore;
        String creditTier;
        double riskScore;

        if (monthlyIncome >= 6000) {
            creditScore = 750;
            creditTier = "EXCELLENT";
            riskScore = 12.5;
        } else if (monthlyIncome >= 3500) {
            creditScore = 660;
            creditTier = "FAIR";
            riskScore = 34.2;
        } else {
            creditScore = 540;
            creditTier = "POOR";
            riskScore = 68.0;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("applicantId", applicantId);
        response.put("applicantName", applicantName);
        response.put("creditScore", creditScore);
        response.put("creditTier", creditTier);
        response.put("riskScore", riskScore);
        response.put("status", "ASSESSED");

        return ResponseEntity.ok(response);
    }
}
