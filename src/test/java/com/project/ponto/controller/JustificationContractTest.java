package com.project.ponto.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.project.ponto.domain.enums.ApprovalStatusEnum;
import com.project.ponto.domain.enums.EmployeeStatusEnum;
import com.project.ponto.domain.enums.JustificationTypeEnum;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.Justification;
import com.project.ponto.domain.vo.CPF;
import com.project.ponto.domain.vo.Email;
import com.project.ponto.dto.JustificationRejectionRequest;
import com.project.ponto.dto.JustificationRequest;
import com.project.ponto.repository.EmployeeRepository;
import com.project.ponto.repository.JustificationRepository;
import com.project.ponto.util.JwtTestUtils;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Justification API Contract Tests")
class JustificationContractTest {

    private static final String ATTACHMENT_URL = "https://storage.company.com/docs/cert-12345.pdf";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JustificationRepository justificationRepository;

    private Employee employee;
    private Employee manager;
    private String employeeToken;
    private String managerToken;

    @BeforeEach
    void setUp() {
        justificationRepository.deleteAll();
        employeeRepository.deleteAll();

        employee = persistEmployee("REG-100", "Ana Souza", "529.982.247-25", "ana.souza@empresa.com");
        manager = persistEmployee("REG-200", "Carlos Lima", "111.444.777-35", "carlos.lima@empresa.com");

        employeeToken = JwtTestUtils.validToken(employee.getId().toString());
        managerToken = JwtTestUtils.validToken(manager.getId().toString());
    }

    // =========================================================================
    // Normal BVA
    // =========================================================================

    @Test
    @DisplayName("Must return 201 when startDate equals endDate")
    void mustCreateMinimumPeriod() throws Exception {
        create(employeeToken, request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                "Consulta médica", JustificationTypeEnum.MEDICAL_CERTIFICATE, ATTACHMENT_URL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.employeeId", is(employee.getId().intValue())))
                .andExpect(jsonPath("$.employeeName", is("Ana Souza")))
                .andExpect(jsonPath("$.approverId", nullValue()))
                .andExpect(jsonPath("$.approverName", nullValue()))
                .andExpect(jsonPath("$.startDate", is("2026-11-01")))
                .andExpect(jsonPath("$.endDate", is("2026-11-01")))
                .andExpect(jsonPath("$.reason", is("Consulta médica")))
                .andExpect(jsonPath("$.rejectionReason", nullValue()))
                .andExpect(jsonPath("$.type", is("MEDICAL_CERTIFICATE")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.requestedAt", notNullValue()))
                .andExpect(jsonPath("$.approvedAt", nullValue()))
                .andExpect(jsonPath("$.attachmentUrl", is(ATTACHMENT_URL)));
    }

    @Test
    @DisplayName("Must return 201 when startDate is before endDate")
    void mustCreateMultiDayPeriod() throws Exception {
        create(employeeToken, request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3),
                "Recuperação", JustificationTypeEnum.MEDICAL_CERTIFICATE, null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startDate", is("2026-11-01")))
                .andExpect(jsonPath("$.endDate", is("2026-11-03")));
    }

    @Test
    @DisplayName("Must return 201 when attachmentUrl has exactly 500 characters")
    void mustAcceptAttachmentAtMaximumLength() throws Exception {
        String attachment = "u".repeat(500);

        create(employeeToken, request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                "Anexo no limite", JustificationTypeEnum.OTHER, attachment))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attachmentUrl", hasLength(500)));
    }

    @Test
    @DisplayName("Must return 201 when attachmentUrl is null")
    void mustAcceptNullAttachment() throws Exception {
        create(employeeToken, request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                "Sem anexo", JustificationTypeEnum.JUSTIFIED_ABSENCE, null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attachmentUrl", nullValue()));
    }

    @Test
    @DisplayName("Must paginate employee justifications with page=1 and size=1")
    void mustPaginateEmployeeJustifications() throws Exception {
        persistJustification(employee, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 1, 10, 0));
        persistJustification(employee, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2),
                ApprovalStatusEnum.APPROVED, LocalDateTime.of(2026, 11, 2, 10, 0));

        mockMvc.perform(get("/employees/{employeeId}/justifications", employee.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + employeeToken)
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Must paginate pending justifications with page=1 and size=1")
    void mustPaginatePendingJustifications() throws Exception {
        persistJustification(employee, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 1, 10, 0));
        persistJustification(employee, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2),
                ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 2, 10, 0));

        mockMvc.perform(get("/justifications/pending")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // =========================================================================
    // Robust BVA
    // =========================================================================

    @Test
    @DisplayName("Must return 400 when startDate is after endDate")
    void mustRejectInvertedDates() throws Exception {
        create(employeeToken, request(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 1),
                "Período invertido", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("End date must be on or after start date")));
    }

    @Test
    @DisplayName("Must return 400 when reason is blank")
    void mustRejectBlankReason() throws Exception {
        create(employeeToken, request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                "   ", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Reason must not be blank")));
    }

    @Test
    @DisplayName("Must return 400 when attachmentUrl has 501 characters")
    void mustRejectAttachmentOverflow() throws Exception {
        create(employeeToken, request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                "Anexo acima do limite", JustificationTypeEnum.OTHER, "u".repeat(501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Attachment URL must not exceed 500 characters")));
    }

    @Test
    @DisplayName("Must return 400 when employee justification list receives page=0")
    void mustRejectInvalidPageOnEmployeeList() throws Exception {
        mockMvc.perform(get("/employees/{employeeId}/justifications", employee.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + employeeToken)
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("A página deve ser maior ou igual a 1")));
    }

    @Test
    @DisplayName("Must return 400 when pending list receives page=0")
    void mustRejectInvalidPageOnPendingList() throws Exception {
        mockMvc.perform(get("/justifications/pending")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("A página deve ser maior ou igual a 1")));
    }

    @Test
    @DisplayName("Must return 409 when re-approving an approved justification")
    void mustRejectReapprovingApprovedJustification() throws Exception {
        Justification justification = persistJustification(employee, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1), ApprovalStatusEnum.APPROVED, null);

        mockMvc.perform(post("/justifications/{id}/approval", justification.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Must return 404 when approving an unknown justification")
    void mustRejectApprovingUnknownJustification() throws Exception {
        mockMvc.perform(post("/justifications/{id}/approval", 99999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must return 404 when rejecting an unknown justification")
    void mustRejectRejectingUnknownJustification() throws Exception {
        mockMvc.perform(post("/justifications/{id}/rejection", 99999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // Worst-case BVA
    // =========================================================================

    @Test
    @DisplayName("Must return 201 when combining minimum period, max reason and max attachment")
    void mustCreateWorstCaseValidBoundaries() throws Exception {
        create(employeeToken, request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                "r".repeat(255), JustificationTypeEnum.MEDICAL_CERTIFICATE, "u".repeat(500)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reason", hasLength(255)))
                .andExpect(jsonPath("$.attachmentUrl", hasLength(500)));
    }

    @Test
    @DisplayName("Must aggregate multiple validation errors in a single 400 response")
    void mustAggregateMultipleValidationErrors() throws Exception {
        create(employeeToken, request(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 1),
                "   ", JustificationTypeEnum.OTHER, "u".repeat(505)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("End date must be on or after start date")))
                .andExpect(jsonPath("$.erro", containsString("Reason must not be blank")))
                .andExpect(jsonPath("$.erro", containsString("Attachment URL must not exceed 500 characters")));
    }

    // =========================================================================
    // Edge cases
    // =========================================================================

    @Test
    @DisplayName("Must return 409 when a manager approves their own justification")
    void mustRejectSelfApproval() throws Exception {
        Justification justification = persistJustification(employee, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1), ApprovalStatusEnum.PENDING, null);

        mockMvc.perform(post("/justifications/{id}/approval", justification.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + employeeToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Must return 409 when a manager rejects their own justification")
    void mustRejectSelfRejection() throws Exception {
        Justification justification = persistJustification(employee, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1), ApprovalStatusEnum.PENDING, null);

        mockMvc.perform(post("/justifications/{id}/rejection", justification.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new JustificationRejectionRequest("Tentativa de auto-rejeição"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Must return 409 when the period overlaps an approved justification")
    void mustRejectOverlapWithApprovedJustification() throws Exception {
        persistJustification(employee, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3),
                ApprovalStatusEnum.APPROVED, null);

        create(employeeToken, request(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 5),
                "Sobreposição", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Must return 409 when the period overlaps a pending justification")
    void mustRejectOverlapWithPendingJustification() throws Exception {
        create(employeeToken, request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3),
                "Primeira solicitação", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isCreated());

        create(employeeToken, request(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 4),
                "Segunda solicitação", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Must return 201 when the new period is adjacent to an existing one")
    void mustAllowAdjacentPeriods() throws Exception {
        persistJustification(employee, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3),
                ApprovalStatusEnum.APPROVED, null);

        create(employeeToken, request(LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 5),
                "Período adjacente", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Must return 409 when the employee is inactive")
    void mustRejectInactiveEmployee() throws Exception {
        employee.setStatus(EmployeeStatusEnum.INACTIVE);
        employeeRepository.save(employee);

        create(employeeToken, request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                "Funcionário inativo", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro", containsString("not active")));
    }

    @Test
    @DisplayName("Must return 409 when the employee is on vacation")
    void mustRejectVacationEmployee() throws Exception {
        employee.setStatus(EmployeeStatusEnum.VACATION);
        employeeRepository.save(employee);

        create(employeeToken, request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                "Funcionário em férias", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro", containsString("not active")));
    }

    @Test
    @DisplayName("Must return 201 for a period covering the leap day 2028-02-29")
    void mustAcceptLeapYearPeriod() throws Exception {
        create(employeeToken, request(LocalDate.of(2028, 2, 28), LocalDate.of(2028, 3, 1),
                "Período com ano bissexto", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startDate", is("2028-02-28")))
                .andExpect(jsonPath("$.endDate", is("2028-03-01")));
    }

    @Test
    @DisplayName("Must return 401 when the JWT has no id claim")
    void mustRejectTokenWithoutIdClaim() throws Exception {
        create(JwtTestUtils.tokenWithoutId(), request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                "Sem claim id", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Must return 401 when the JWT id claim is not numeric")
    void mustRejectTokenWithNonNumericIdClaim() throws Exception {
        create(JwtTestUtils.validToken("not-a-number"), request(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                "Claim id não numérica", JustificationTypeEnum.OTHER, null))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Must return 404 when listing justifications of an unknown employee")
    void mustRejectUnknownEmployeeOnList() throws Exception {
        mockMvc.perform(get("/employees/{employeeId}/justifications", 99999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must list every status for a specific employee")
    void mustListAllStatusesForEmployee() throws Exception {
        persistJustification(employee, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 1, 10, 0));
        persistJustification(employee, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2),
                ApprovalStatusEnum.APPROVED, LocalDateTime.of(2026, 11, 2, 10, 0));
        persistJustification(employee, LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 3),
                ApprovalStatusEnum.REJECTED, LocalDateTime.of(2026, 11, 3, 10, 0));
        persistJustification(manager, LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 4),
                ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 4, 10, 0));

        mockMvc.perform(get("/employees/{employeeId}/justifications", employee.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Must list only pending justifications across the organization")
    void mustListOnlyPendingAcrossOrganization() throws Exception {
        Justification pending = persistJustification(employee, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1), ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 1, 10, 0));
        persistJustification(employee, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2),
                ApprovalStatusEnum.APPROVED, LocalDateTime.of(2026, 11, 2, 10, 0));
        persistJustification(manager, LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 3),
                ApprovalStatusEnum.REJECTED, LocalDateTime.of(2026, 11, 3, 10, 0));

        mockMvc.perform(get("/justifications/pending")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(pending.getId().intValue())))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    // =========================================================================
    // Approval and rejection flows
    // =========================================================================

    @Test
    @DisplayName("Must approve a pending justification and persist the approver")
    void mustApprovePendingJustification() throws Exception {
        Justification justification = persistJustification(employee, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1), ApprovalStatusEnum.PENDING, null);

        mockMvc.perform(post("/justifications/{id}/approval", justification.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(justification.getId().intValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.approverId", is(manager.getId().intValue())))
                .andExpect(jsonPath("$.approverName", is("Carlos Lima")))
                .andExpect(jsonPath("$.approvedAt", notNullValue()))
                .andExpect(jsonPath("$.rejectionReason", nullValue()));
    }

    @Test
    @DisplayName("Must reject a pending justification and record the rejection reason")
    void mustRejectPendingJustificationWithReason() throws Exception {
        Justification justification = persistJustification(employee, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1), ApprovalStatusEnum.PENDING, null);

        mockMvc.perform(post("/justifications/{id}/rejection", justification.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new JustificationRejectionRequest("Insufficient documentation attached"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.approverId", is(manager.getId().intValue())))
                .andExpect(jsonPath("$.approverName", is("Carlos Lima")))
                .andExpect(jsonPath("$.approvedAt", notNullValue()))
                .andExpect(jsonPath("$.rejectionReason", is("Insufficient documentation attached")));
    }

    @Test
    @DisplayName("Must reject a pending justification without a request body")
    void mustRejectPendingJustificationWithoutReason() throws Exception {
        Justification justification = persistJustification(employee, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1), ApprovalStatusEnum.PENDING, null);

        mockMvc.perform(post("/justifications/{id}/rejection", justification.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.rejectionReason", nullValue()));
    }

    private ResultActions create(String token, JustificationRequest request) throws Exception {
        return mockMvc.perform(post("/justifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private static JustificationRequest request(LocalDate startDate, LocalDate endDate, String reason,
            JustificationTypeEnum type, String attachmentUrl) {
        return new JustificationRequest(startDate, endDate, reason, type, attachmentUrl);
    }

    private Employee persistEmployee(String registrationNumber, String name, String cpf, String email) {
        Employee entity = new Employee();
        entity.setRegistrationNumber(registrationNumber);
        entity.setName(name);
        entity.setCpf(new CPF(cpf));
        entity.setEmail(new Email(email));
        entity.setRole("Analista");
        entity.setDepartment("TI");
        entity.setStatus(EmployeeStatusEnum.ACTIVE);
        return employeeRepository.save(entity);
    }

    private Justification persistJustification(Employee owner, LocalDate startDate, LocalDate endDate,
            ApprovalStatusEnum status, LocalDateTime requestedAt) {
        Justification justification = new Justification();
        justification.setEmployee(owner);
        justification.setStartDate(startDate);
        justification.setEndDate(endDate);
        justification.setReason("Motivo da justificativa");
        justification.setType(JustificationTypeEnum.MEDICAL_CERTIFICATE);
        justification.setStatus(status);
        justification.setRequestedAt(requestedAt);
        return justificationRepository.save(justification);
    }
}
