package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.OrigemPontoEnum;
import com.project.ponto.domain.enums.TipoPontoEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private TipoPontoEnum tipo;

    @Enumerated(EnumType.STRING)
    private OrigemPontoEnum origem;

    private String observacao;

    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;

    @OneToOne
    @JoinColumn(name = "justificativa_id")
    private Justificativa justificativa;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }
}
