package com.project.ponto.dto;

import com.project.ponto.domain.enums.EmployeeStatusEnum;

public record EmployeeRequest(
        String registrationNumber,
        String name,
        String cpf,
        String email,
        String role,
        String department,
        EmployeeStatusEnum status
) {}