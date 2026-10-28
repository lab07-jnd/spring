package com.project.ponto.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ponto.domain.enums.ContractStatusEnum;
import com.project.ponto.domain.enums.EmployeeStatusEnum;
import com.project.ponto.domain.enums.TimeRecordTypeEnum;
import com.project.ponto.domain.model.Contract;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.TimeRecord;
import com.project.ponto.domain.vo.Geolocation;
import com.project.ponto.dto.TimeRecordRequest;
import com.project.ponto.dto.TimeRecordResponse;
import com.project.ponto.exception.ConflictException;
import com.project.ponto.exception.ResourceNotFoundException;
import com.project.ponto.repository.ContractRepository;
import com.project.ponto.repository.EmployeeRepository;
import com.project.ponto.repository.TimeRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeRecordService {

    private static final long DEBOUNCE_MINIMUM_SECONDS = 60;
    private static final int DEFAULT_PAGE_SIZE = 10;

    // A ordem dos campos define a assinatura do construtor gerado pelo Lombok
    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final Clock clock;

    @Transactional
    public TimeRecordResponse register(TimeRecordRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.employeeId()));

        if (employee.getStatus() != EmployeeStatusEnum.ACTIVE) {
            throw new ConflictException("Employee is not active. Current status: " + employee.getStatus());
        }

        Contract contract = contractRepository.findFirstByEmployeeIdAndStatus(employee.getId(), ContractStatusEnum.ACTIVE)
                .orElseGet(() -> {
                    if (contractRepository.existsByEmployeeId(employee.getId())) {
                        throw new ConflictException("Employee does not have an active contract.");
                    } else {
                        throw new ResourceNotFoundException("Employee does not have a registered contract.");
                    }
                });

        validateDebounce(contract.getId(), now);
        validateDailySequence(contract.getId(), request.type(), now);

        Geolocation geolocation = Geolocation.of(request.latitude(), request.longitude());

        TimeRecord record = new TimeRecord();
        record.setContract(contract);
        record.setDateTime(now);
        record.setType(request.type());
        record.setOrigin(request.origin());
        record.setNotes(request.notes());
        record.setGeolocation(geolocation);

        TimeRecord saved = timeRecordRepository.save(record);
        return TimeRecordResponse.from(saved);
    }

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

    private void validateDebounce(Long contractId, LocalDateTime now) {
        timeRecordRepository.findTopByContractIdOrderByDateTimeDesc(contractId)
                .ifPresent(lastRecord -> {
                    long secondsSinceLast = Duration.between(lastRecord.getDateTime(), now).getSeconds();
                    if (secondsSinceLast < DEBOUNCE_MINIMUM_SECONDS) {
                        throw new ConflictException(
                                "Duplicate time record. Wait at least 1 minute between records. Elapsed time: %d seconds."
                                        .formatted(secondsSinceLast)
                        );
                    }
                });
    }

    private void validateDailySequence(Long contractId, TimeRecordTypeEnum requestedType, LocalDateTime now) {
        LocalDate today = now.toLocalDate();

        Optional<TimeRecord> lastRecordOpt = timeRecordRepository
                .findTopByContractIdAndDateTimeBetweenOrderByDateTimeDesc(
                        contractId,
                        today.atStartOfDay(),
                        today.atTime(LocalTime.MAX)
                );

        if (lastRecordOpt.isEmpty()) {
            if (!requestedType.canStartDay()) {
                throw new ConflictException(
                        "The first clock-in of the day must be of type CLOCK_IN (Entry). Attempt: " + requestedType
                );
            }
            return;
        }

        TimeRecordTypeEnum lastType = lastRecordOpt.get().getType();

        if (!lastType.canBeFollowedBy(requestedType)) {
            throw new ConflictException(
                    "After %s, the next clock-in must be one of %s. Attempt: %s"
                            .formatted(lastType, lastType.allowedNextTypes(), requestedType)
            );
        }
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