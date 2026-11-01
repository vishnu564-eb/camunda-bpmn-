package com.camunda.labs.lab1.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoanValidationWorker {

    private static final Logger log = LoggerFactory.getLogger(LoanValidationWorker.class);

    /**
     * Requirement 3: Java/Spring Boot external job worker to validate applicant
     * information and return validation results as process variables.
     */
    @JobWorker(type = "validate-loan-applicant", autoComplete = true)
    public Map<String, Object> validateApplicant(final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String applicantId = (String) variables.getOrDefault("applicantId", "UNKNOWN");
        String applicantName = (String) variables.getOrDefault("applicantName", "");
        Number incomeNum = (Number) variables.getOrDefault("monthlyIncome", 0.0);
        Number requestedAmountNum = (Number) variables.getOrDefault("requestedAmount", 0.0);

        double monthlyIncome = incomeNum != null ? incomeNum.doubleValue() : 0.0;
        double requestedAmount = requestedAmountNum != null ? requestedAmountNum.doubleValue() : 0.0;

        log.info("[Lab 1] Validating applicant: id={}, name={}, income={}, amount={}",
                applicantId, applicantName, monthlyIncome, requestedAmount);

        // Incident Simulation: Worker failure trigger
        if ("WORKER_FAIL".equalsIgnoreCase(applicantId)) {
            log.error("[Lab 1] Intentional worker failure triggered for applicant: {}", applicantId);
            throw new RuntimeException("Simulated External Worker Failure: Downstream Database Unavailable for applicant " + applicantId);
        }

        Map<String, Object> result = new HashMap<>();
        boolean isValid = true;
        StringBuilder remarks = new StringBuilder();

        if (applicantName == null || applicantName.trim().isEmpty()) {
            isValid = false;
            remarks.append("Applicant name cannot be empty. ");
        }
        if (monthlyIncome <= 0) {
            isValid = false;
            remarks.append("Monthly income must be greater than zero. ");
        }
        if (requestedAmount <= 0) {
            isValid = false;
            remarks.append("Requested loan amount must be positive. ");
        }

        double debtToIncomeRatio = monthlyIncome > 0 ? (requestedAmount / (monthlyIncome * 12.0)) : 999.0;

        if (isValid) {
            remarks.append("Applicant data integrity check passed.");
        }

        result.put("isValidApplicant", isValid);
        result.put("validationRemarks", remarks.toString());
        result.put("debtToIncomeRatio", debtToIncomeRatio);

        log.info("[Lab 1] Validation result: isValid={}, remarks={}", isValid, remarks);
        return result;
    }

    /**
     * Requirement 4 & 9: Service integration for Credit Assessment
     * Can act as external worker or bridge to REST API.
     */
    @JobWorker(type = "assess-credit-score", autoComplete = true)
    public Map<String, Object> assessCreditScore(final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String applicantId = (String) variables.getOrDefault("applicantId", "UNKNOWN");
        Number incomeNum = (Number) variables.getOrDefault("monthlyIncome", 5000.0);
        double monthlyIncome = incomeNum != null ? incomeNum.doubleValue() : 5000.0;

        log.info("[Lab 1] Performing credit assessment for applicant: {}", applicantId);

        // Requirement 9: Intentionally cause one service/worker failure, diagnose incident in Operate and retry
        if ("FAIL_TEST".equalsIgnoreCase(applicantId)) {
            log.error("[Lab 1] Simulating Credit Assessment API 500 outage for: {}", applicantId);
            throw new RuntimeException("CreditAssessmentException: Simulated 500 Internal Server Outage on Credit Bureau API for applicant " + applicantId);
        }

        int score = 720;
        String tier = "EXCELLENT";

        if (monthlyIncome >= 6000) {
            score = 760;
            tier = "EXCELLENT";
        } else if (monthlyIncome >= 3500) {
            score = 650;
            tier = "FAIR";
        } else {
            score = 550;
            tier = "POOR";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("creditScore", score);
        result.put("creditTier", tier);
        result.put("riskScore", 1000 - score);

        log.info("[Lab 1] Credit assessment complete: score={}, tier={}", score, tier);
        return result;
    }

    @JobWorker(type = "disburse-loan", autoComplete = true)
    public void disburseLoan(final ActivatedJob job) {
        log.info("[Lab 1] Loan successfully disbursed for instance: {}", job.getProcessInstanceKey());
    }

    @JobWorker(type = "notify-loan-rejection", autoComplete = true)
    public void notifyRejection(final ActivatedJob job) {
        log.info("[Lab 1] Loan rejection notification dispatched for instance: {}", job.getProcessInstanceKey());
    }
}
