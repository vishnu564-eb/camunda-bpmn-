package com.camunda.labs.lab3.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/hospital/slots")
public class MockHospitalSchedulingController {

    private static final Logger log = LoggerFactory.getLogger(MockHospitalSchedulingController.class);

    @GetMapping
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam(defaultValue = "GENERAL_MEDICINE") String specialty,
            @RequestParam(defaultValue = "2026-11-05") String date) {

        log.info("[Hospital REST API] Slot query received for specialty={}, date={}", specialty, date);

        // Lab 3 Requirement 9: Simulating downstream 503 outage for incident resolution
        if ("FAIL_TEST".equalsIgnoreCase(specialty)) {
            log.warn("[Hospital REST API] Simulating 503 service outage for specialty: {}", specialty);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Scheduling Service 503 Service Unavailable");
            error.put("code", "SCHEDULING_SERVICE_DOWN");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("specialty", specialty);
        response.put("appointmentDate", date);

        if ("NEUROLOGY".equalsIgnoreCase(specialty)) {
            response.put("status", "UNAVAILABLE");
            response.put("slotsAvailable", false);
            response.put("slotId", null);
            response.put("slotTime", null);
            response.put("message", "All clinical slots fully booked for requested specialty and date");
        } else {
            response.put("status", "AVAILABLE");
            response.put("slotsAvailable", true);
            response.put("slotId", "SLOT-C8-101");
            response.put("slotTime", "10:30 AM");
            response.put("assignedDoctor", "Dr. Gregory House");
            response.put("roomNumber", "B-204");
        }

        return ResponseEntity.ok(response);
    }
}
