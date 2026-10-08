package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.StatusAprovacaoEnum;
import com.project.ponto.domain.enums.TipoJustificativaEnum;
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
public class Justificativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataInicio;

    private LocalDate dataFim;

    private String motivo;

    @Enumerated(EnumType.STRING)
    private TipoJustificativaEnum tipo;

    @Enumerated(EnumType.STRING)
    private StatusAprovacaoEnum status;

    private LocalDateTime dataSolicitacao;

    @ManyToOne
    @JoinColumn(name = "aprovador_id")
    private Funcionario aprovador;

    private LocalDateTime dataAprovacao;

    private String anexoUrl;

    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;
}
