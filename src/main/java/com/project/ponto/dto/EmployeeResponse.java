package com.project.ponto.dto;

import java.time.LocalDateTime;

import com.project.ponto.domain.enums.EmployeeStatusEnum;
import com.project.ponto.domain.model.Employee;

public record EmployeeResponse(
        Long id,
        String registrationNumber,
        String name,
        String cpf,
        String email,
        String role,
        String department,
        EmployeeStatusEnum status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(
                e.getId(),
                e.getRegistrationNumber(),
                e.getName(),
                e.getCpf() == null ? null : e.getCpf().toString(),
                e.getEmail() == null ? null : e.getEmail().toString(),
                e.getRole(),
                e.getDepartment(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}