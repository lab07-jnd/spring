package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.StatusContratoEnum;
import com.project.ponto.domain.enums.TipoContratoEnum;
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
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataAdmissao;

    private LocalDate dataDemissao;

    @Enumerated(EnumType.STRING)
    private TipoContratoEnum tipo;

    private int salarioBaseCentavos;

    @Enumerated(EnumType.STRING)
    private StatusContratoEnum status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "jornada_id")
    private Jornada jornada;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "banco_horas_id")
    private BancoHoras bancoHoras;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contrato")
    private List<RegistroPonto> registrosPonto;

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
