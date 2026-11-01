# Camunda 8 Practical Assignment — Labs 1, 2 & 3

[![Camunda 8.8](https://img.shields.io/badge/Camunda-8.8.34-orange)](https://camunda.com)
[![Java 21](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.4-green)](https://spring.io/projects/spring-boot)

This repository contains the complete solution for the **Camunda 8 Practical Assignment** covering Labs 1, 2, and 3.

---

## 📦 Repository Structure

```
camunda8-practical-assignment/
│
├── lab1-loan-approval/
│   ├── bpmn/   loan-approval.bpmn            ← End-to-End Loan Approval BPMN
│   ├── dmn/    loan-approval-decision.dmn    ← Loan Approval DMN Decision Table
│   ├── requests/ credit-assessment-api-example.json
│   └── REPORT.md                             ← Lab 1 Written Report
│
├── lab2-ecommerce-fulfilment/
│   ├── bpmn/   order-fulfilment.bpmn         ← Order Processing BPMN
│   ├── dmn/    order-priority-decision.dmn   ← Order Priority DMN Decision Table
│   ├── requests/ payment-api-example.json
│   └── REPORT.md                             ← Lab 2 Written Report
│
├── lab3-hospital-triage/
│   ├── bpmn/   hospital-triage.bpmn          ← Hospital Triage BPMN
│   ├── dmn/    patient-triage-decision.dmn   ← Patient Triage DMN Decision Table
│   ├── requests/ hospital-scheduling-api-example.json
│   └── REPORT.md                             ← Lab 3 Written Report
│
├── spring-boot-workers/                      ← Spring Boot External Job Workers
│   ├── pom.xml
│   └── src/main/java/com/camunda/labs/
│       ├── CamundaLabsApplication.java       ← Main application entry point
│       ├── lab1/worker/ LoanValidationWorker.java
│       ├── lab1/controller/ MockCreditAssessmentController.java
│       ├── lab2/worker/ OrderValidationWorker.java
│       ├── lab2/controller/ MockPaymentGatewayController.java
│       ├── lab3/worker/ PatientRegistrationWorker.java
│       └── lab3/controller/ MockHospitalSchedulingController.java
│
├── start-camunda-run.bat    ← Start Camunda 8 (Zeebe + Operate + Tasklist)
├── start-workers.bat        ← Start Spring Boot Workers & Mock APIs
├── open-modeler.bat         ← Open Camunda Desktop Modeler
└── README.md
```

---

## 🔧 Prerequisites

| Tool | Version | Download |
|---|---|---|
| Java (OpenJDK) | 21 | Already installed at `C:\Users\VISHN\openjdk-21` |
| Apache Maven | 3.9.9 | Already installed at `C:\Users\VISHN\apache-maven-3.9.9` |
| Camunda 8 Run | 8.8.34 | Already installed |
| Camunda Desktop Modeler | 5.50.1 | Already installed |

---

## 🚀 Running the Labs

### Step 1: Start Camunda 8 Run
Double-click **`start-camunda-run.bat`**, or run:
```bat
cd "C:\Users\VISHN\Downloads\camunda8-run-8.8-windows-x86_64 (1)\c8run-8.8.34"
set JAVA_HOME=C:\Users\VISHN\openjdk-21
c8run.exe start
```

Camunda 8 URLs (default credentials `demo` / `demo`):
| Component | URL |
|---|---|
| **Operate** | http://localhost:8080/operate |
| **Tasklist** | http://localhost:8080/tasklist |
| **Identity** | http://localhost:8080/identity |
| **Zeebe gRPC API** | `localhost:26500` |
| **REST API** | http://localhost:8080/v2/ |

### Step 2: Start the Spring Boot Workers & Mock REST APIs
Double-click **`start-workers.bat`**, or run:
```bat
cd spring-boot-workers
set JAVA_HOME=C:\Users\VISHN\openjdk-21
C:\Users\VISHN\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```

Workers will connect to Zeebe at `localhost:26500` and expose mock REST APIs at **`http://localhost:8085`**:

| Endpoint | Lab | Description |
|---|---|---|
| `POST /api/credit-assessment` | Lab 1 | Mock Credit Bureau API |
| `POST /api/payment/process` | Lab 2 | Mock Payment Gateway API |
| `GET  /api/hospital/slots` | Lab 3 | Mock Hospital Scheduling API |

### Step 3: Deploy BPMN/DMN Models
Open **Camunda Desktop Modeler** (`open-modeler.bat`) and deploy each `.bpmn` and `.dmn` file to the local Zeebe cluster:
- Set Connection: `http://localhost:8080` (no auth needed in local mode)
- Deploy each model from the `bpmn/` and `dmn/` directories

> **Alternatively**, the Spring Boot app auto-deploys all models from `src/main/resources/models/` on startup via `@Deployment`.

---

## 📋 Labs Summary

### Lab 1 — End-to-End Loan Approval Workflow
**Process ID:** `Process_LoanApproval`
- Worker validates applicant → REST credit assessment → DMN decision → User task (underwriter) → Disburse/Reject
- **DMN Hit Policy:** FIRST | 5 rules: Excellent/Poor credit, DTI ratio, Manual review fallback
- **Incident Simulation:** Set `applicantId = "FAIL_TEST"` to trigger a worker failure for Operate recovery exercise

**Test Cases:**
| Variable | Scenario | Expected Path |
|---|---|---|
| `applicantId="APP-1001"`, `monthlyIncome=6500`, `creditScore=750` | High Credit | Direct APPROVED → Disburse |
| `applicantId="APP-1002"`, `monthlyIncome=3200`, `requestedAmount=80000` | Low Credit | REJECTED → Notify |
| `applicantId="APP-1003"`, `monthlyIncome=4500` | Moderate Credit | MANUAL_REVIEW → Tasklist → Approve/Reject |
| `applicantId="FAIL_TEST"` | Incident | Worker failure → Operate incident → Retry |

---

### Lab 2 — E-Commerce Order Processing & Fulfilment
**Process ID:** `Process_OrderProcessing`
- Worker validates stock → DMN priority → (High value → Tasklist approval) → REST payment → Fulfil/Notify
- **DMN Hit Policy:** FIRST | 5 rules: VIP/High-value/Express/Regular/Low priority
- **Incident Simulation:** Set `paymentToken = "tok_gateway_error"` to trigger a 504 gateway timeout

**Test Cases:**
| Variable | Scenario | Expected Path |
|---|---|---|
| `customerCategory="VIP"`, `quantity=1`, `unitPrice=450` | VIP HIGH priority | Payment → Fulfilled |
| `productId="OOS_ITEM"` | Out of Stock | Notified & Cancelled |
| `paymentToken="tok_declined_funds"` | Payment Declined | Notify Payment Failed |
| `customerId="CUST-FAIL"` | Incident | Worker failure → Operate retry |

---

### Lab 3 — Hospital Appointment & Patient Triage Management
**Process ID:** `Process_HospitalTriage`
- Worker validates patient → DMN triage → (URGENT → Emergency Tasklist) OR (PRIORITY → Nurse Tasklist) OR (ROUTINE → REST slot) → Confirm/Manual
- **DMN Hit Policy:** FIRST | 5 rules: Pain level, urgency indicator, preexisting conditions
- **Incident Simulation:** Set `preferredSpecialty = "FAIL_TEST"` to trigger a 503 scheduling outage

**Test Cases:**
| Variable | Scenario | Expected Path |
|---|---|---|
| `painLevel=9` | Critical pain | URGENT_ASSESSMENT → Emergency Protocol |
| `painLevel=3`, `hasPreexistingCondition=false`, `preferredSpecialty="CARDIOLOGY"` | Routine | Slot booked → Confirmed |
| `preferredSpecialty="NEUROLOGY"` | No slots | Manual staff resolution |
| `preferredSpecialty="FAIL_TEST"` | Incident | REST outage → Operate retry |

---

## 🔍 Incident Investigation & Recovery (Camunda Operate)

1. **Trigger**: Use the special variable values listed above (`FAIL_TEST` / `tok_gateway_error` etc.)
2. **Detect**: Open [Operate](http://localhost:8080/operate) → find the process instance with a red incident badge
3. **Diagnose**: Click the incident → view the error message, stack trace, and failed service task
4. **Resolve**: Fix the variable directly in Operate (e.g. change `applicantId` to a valid value)
5. **Retry**: Click **"Retry Incident"** → Zeebe resumes execution without data loss

---

## 👤 Camunda Operate & Tasklist User Tasks

Login at http://localhost:8080/tasklist with `demo`/`demo` to complete:
- **Lab 1**: `loan-underwriters` group — Underwriter manual approval/rejection
- **Lab 2**: `order-supervisors` group — High-value order verification
- **Lab 3**: `emergency-clinical-team`, `triage-nurses`, `hospital-coordinators` groups
