package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.ContractStatusEnum;
import com.project.ponto.domain.enums.ContractTypeEnum;
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
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate admissionDate;

    private LocalDate terminationDate;

    @Enumerated(EnumType.STRING)
    private ContractTypeEnum type;

    private int baseSalaryCents;

    @Enumerated(EnumType.STRING)
    private ContractStatusEnum status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "work_schedule_id")
    private WorkSchedule workSchedule;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hour_bank_id")
    private HourBank hourBank;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private List<TimeRecord> timeRecords;

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
