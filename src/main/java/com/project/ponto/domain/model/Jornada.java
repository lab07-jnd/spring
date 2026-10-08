package com.project.ponto.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private LocalTime horarioEntrada;

    private LocalTime horarioSaida;

    private int cargaHorariaDiariaMinutos;

    private int intervaloMinutos;

    @ElementCollection(targetClass = DiaSemanaEnum.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "jornada_dias_trabalhados", joinColumns = @JoinColumn(name = "jornada_id"))
    @Column(name = "dia_semana")
    private List<DiaSemanaEnum> diasTrabalhados;

    private boolean ativa;
}
