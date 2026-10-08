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
public class BancoHoras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int saldoMinutos;

    private int saldoMinutosAnterior;

    private LocalDate periodoInicio;

    private LocalDate periodoFim;

    private LocalDate dataExpiracao;

    private LocalDateTime ultimaAtualizacao;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "bancoHoras")
    private List<MovimentoBancoHoras> movimentos;
}
