package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.TipoMovimentoEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentoBancoHoras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int minutos;

    private LocalDateTime dataMovimento;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private TipoMovimentoEnum tipo;

    @ManyToOne
    @JoinColumn(name = "banco_horas_id")
    private BancoHoras bancoHoras;
}
