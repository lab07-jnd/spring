package com.project.ponto.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.project.ponto.domain.enums.JustificationTypeEnum;
import com.project.ponto.dto.JustificationRequest;
import com.project.ponto.repository.EmployeeRepository;
import com.project.ponto.repository.JustificationRepository;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

@DisplayName("JustificationService - Jakarta Bean Validation")
class JustificationServiceValidationTest {

    private JustificationRepository justificationRepository;
    private EmployeeRepository employeeRepository;
    private JustificationService service;

    @BeforeEach
    void setUp() {
        justificationRepository = mock(JustificationRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new JustificationService(justificationRepository, employeeRepository, validator, List.of());
    }

    @Test
    @DisplayName("Must delegate request validation to Jakarta Validator before touching repositories")
    void mustValidateRequestWithJakartaValidator() {
        JustificationRequest request = new JustificationRequest(
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 11, 1),
                "   ",
                JustificationTypeEnum.OTHER,
                "u".repeat(501));

        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date must be on or after start date")
                .hasMessageContaining("Reason must not be blank")
                .hasMessageContaining("Attachment URL must not exceed 500 characters");

        verifyNoInteractions(justificationRepository, employeeRepository);
    }

    @Test
    @DisplayName("Must validate the rejection reason with the DTO constraints")
    void mustValidateRejectionReasonWithJakartaValidator() {
        assertThatThrownBy(() -> service.reject(1L, 1L, "r".repeat(256)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rejection reason must not exceed 255 characters");

        verifyNoInteractions(justificationRepository, employeeRepository);
    }
}
