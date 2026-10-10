package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.ApprovalStatusEnum;
import com.project.ponto.domain.enums.JustificationTypeEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Justification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;

    private LocalDate endDate;

    private String reason;

    @Enumerated(EnumType.STRING)
    private JustificationTypeEnum type;

    @Enumerated(EnumType.STRING)
    private ApprovalStatusEnum status;

    private LocalDateTime requestedAt;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private Employee approver;

    private LocalDateTime approvedAt;

    private String attachmentUrl;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }
}
