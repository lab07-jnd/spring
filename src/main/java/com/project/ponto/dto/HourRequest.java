package com.project.ponto.dto;

public record HourRequest(
        int balanceMinutes,
        String registrationNumber,
        String name,
        String cpf,
        String email
) {
}
