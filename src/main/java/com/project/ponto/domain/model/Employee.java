package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.EmployeeStatusEnum;
import com.project.ponto.domain.vo.CPF;
import com.project.ponto.domain.vo.Email;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String registrationNumber;

    private String name;

    @Embedded
    private CPF cpf;

    @Embedded
    private Email email;

    private String role;

    private String department;

    @Enumerated(EnumType.STRING)
    private EmployeeStatusEnum status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
