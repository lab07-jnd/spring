package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.MovementTypeEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourBankMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int minutes;

    private LocalDateTime movementDate;

    private String description;

    @Enumerated(EnumType.STRING)
    private MovementTypeEnum type;

    @ManyToOne
    @JoinColumn(name = "hour_bank_id")
    private HourBank hourBank;
}
