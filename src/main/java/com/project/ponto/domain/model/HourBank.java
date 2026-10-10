package com.project.ponto.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int balanceMinutes;

    private int previousBalanceMinutes;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate expirationDate;

    private LocalDateTime lastUpdatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "hourBank")
    private List<HourBankMovement> movements;
}
