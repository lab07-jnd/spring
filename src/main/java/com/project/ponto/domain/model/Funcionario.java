package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.StatusFuncionarioEnum;
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
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matricula;

    private String nome;

    @Embedded
    private CPF cpf;

    @Embedded
    private Email email;

    private String cargo;

    private String departamento;

    @Enumerated(EnumType.STRING)
    private StatusFuncionarioEnum status;

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
