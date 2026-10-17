package com.project.ponto.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.project.ponto.dto.TimeRecordResponse;
import com.project.ponto.exception.ResourceNotFoundException;
import com.project.ponto.repository.EmployeeRepository;
import com.project.ponto.repository.TimeRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeRecordService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final Clock clock;

    // Histórico de ponto do funcionário (todos os contratos) no período, com paginação opcional
    public List<TimeRecordResponse> listByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate,
            Integer page, Integer size) {
        // Parâmetros inválidos -> 400 antes de consultar o banco
        validatePeriod(startDate, endDate);
        Pageable pageable = toPageable(page, size);

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Funcionário não encontrado com id: " + employeeId);
        }

        // Sem datas: mês corrente no fuso do Clock (America/Sao_Paulo)
        if (startDate == null) {
            LocalDate today = LocalDate.now(clock);
            startDate = today.withDayOfMonth(1);
            endDate = today.with(TemporalAdjusters.lastDayOfMonth());
        }

        // Janela semiaberta [início 00:00, fim + 1 dia 00:00): não depende da precisão de 23:59:59.999...
        return timeRecordRepository.findByEmployeeAndPeriod(employeeId,
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay(), pageable);
    }

    private static void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if ((startDate == null) != (endDate == null)) {
            throw new IllegalArgumentException("Informe dataInicio e dataFim juntas, ou nenhuma das duas");
        }
        if (startDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("dataInicio não pode ser posterior a dataFim");
        }
    }

    // Mesma regra do GET /funcionarios: page começa em 1 e, sem page, vem tudo
    private static Pageable toPageable(Integer page, Integer size) {
        if (page == null) {
            return Pageable.unpaged();
        }
        if (page < 1) {
            throw new IllegalArgumentException("A página deve ser maior ou igual a 1");
        }
        int pageSize = (size != null && size > 0) ? size : DEFAULT_PAGE_SIZE;
        return PageRequest.of(page - 1, pageSize);
    }
}
