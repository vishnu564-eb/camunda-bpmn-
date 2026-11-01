package com.camunda.labs.lab3.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PatientRegistrationWorker {

    private static final Logger log = LoggerFactory.getLogger(PatientRegistrationWorker.class);

    /**
     * Requirement 3: Java/Spring Boot external job worker to validate registration fields
     * and request appointment availability.
     */
    @JobWorker(type = "validate-patient-registration", autoComplete = true)
    public Map<String, Object> validateRegistration(final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String patientId = (String) variables.getOrDefault("patientId", "PAT-UNKNOWN");
        String patientName = (String) variables.getOrDefault("patientName", "");
        Number ageNum = (Number) variables.getOrDefault("age", 30);
        String specialty = (String) variables.getOrDefault("preferredSpecialty", "GENERAL_MEDICINE");
        Number painNum = (Number) variables.getOrDefault("painLevel", 3);

        int age = ageNum != null ? ageNum.intValue() : 30;
        int painLevel = painNum != null ? painNum.intValue() : 3;

        log.info("[Lab 3] Validating patient registration: id={}, name={}, age={}, specialty={}",
                patientId, patientName, age, specialty);

        // Lab 3 Requirement 9: Intentional failure trigger
        if ("PAT-FAIL".equalsIgnoreCase(patientId)) {
            log.error("[Lab 3] Simulating patient database deadlock for: {}", patientId);
            throw new RuntimeException("PatientRegistrationException: Electronic Medical Record (EMR) locking failure for " + patientId);
        }

        boolean isValid = true;
        StringBuilder remarks = new StringBuilder();

        if (patientName == null || patientName.trim().isEmpty()) {
            isValid = false;
            remarks.append("Patient name is required. ");
        }
        if (age <= 0 || age > 130) {
            isValid = false;
            remarks.append("Patient age must be between 1 and 130. ");
        }

        // Predefined urgency indicator estimation if not provided
        String urgencyIndicator = (String) variables.getOrDefault("urgencyIndicator", null);
        if (urgencyIndicator == null) {
            if (painLevel >= 8) {
                urgencyIndicator = "HIGH";
            } else if (painLevel >= 5) {
                urgencyIndicator = "MODERATE";
            } else {
                urgencyIndicator = "LOW";
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("isValidRegistration", isValid);
        result.put("urgencyIndicator", urgencyIndicator);
        result.put("registrationRemarks", remarks.toString());

        log.info("[Lab 3] Registration validated: isValid={}, urgency={}", isValid, urgencyIndicator);
        return result;
    }

    /**
     * Requirement 4: Retrieve Available Slots (External Worker / REST Integration)
     */
    @JobWorker(type = "retrieve-hospital-slots", autoComplete = true)
    public Map<String, Object> retrieveSlots(final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String specialty = (String) variables.getOrDefault("preferredSpecialty", "GENERAL_MEDICINE");
        String prefDate = (String) variables.getOrDefault("appointmentPreferenceDate", "2026-11-05");

        log.info("[Lab 3] Retrieving scheduling slots: specialty={}, date={}", specialty, prefDate);

        // Lab 3 Requirement 9: Simulate scheduling service failure for incident recovery
        if ("FAIL_TEST".equalsIgnoreCase(specialty)) {
            log.error("[Lab 3] Simulating Hospital Scheduling API 503 Service Unavailable");
            throw new RuntimeException("HospitalSchedulingException: Scheduling Service 503 Service Unavailable for department " + specialty);
        }

        Map<String, Object> result = new HashMap<>();
        if ("NEUROLOGY".equalsIgnoreCase(specialty)) {
            result.put("slotsAvailable", false);
            result.put("slotId", null);
            result.put("assignedDoctor", null);
        } else {
            result.put("slotsAvailable", true);
            result.put("slotId", "SLOT-C8-101");
            result.put("slotTime", "10:30 AM");
            result.put("assignedDoctor", "Dr. Gregory House");
            result.put("roomNumber", "B-204");
        }

        log.info("[Lab 3] Slots response: available={}, slotId={}", result.get("slotsAvailable"), result.get("slotId"));
        return result;
    }

    @JobWorker(type = "admit-emergency-patient", autoComplete = true)
    public void admitEmergencyPatient(final ActivatedJob job) {
        log.info("[Lab 3] Emergency patient admitted and red alert dispatched for instance: {}", job.getProcessInstanceKey());
    }

    @JobWorker(type = "confirm-patient-booking", autoComplete = true)
    public Map<String, Object> confirmBooking(final ActivatedJob job) {
        log.info("[Lab 3] Appointment confirmed and notification sent for instance: {}", job.getProcessInstanceKey());
        Map<String, Object> result = new HashMap<>();
        result.put("bookingStatus", "CONFIRMED");
        return result;
    }
}
