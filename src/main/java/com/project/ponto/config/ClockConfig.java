package com.project.ponto.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    // Datas de negócio (ex.: o mês padrão da listagem de pontos) seguem o horário de Brasília,
    // independentemente do fuso da JVM ou do container
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
