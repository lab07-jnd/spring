package com.project.ponto.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class TimeRecordService {

    private static final long DEBOUNCE_MINIMUM_SECONDS = 60;

    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final Clock clock;

    public TimeRecordService(
            TimeRecordRepository timeRecordRepository,
            EmployeeRepository employeeRepository,
            ContractRepository contractRepository,
            @Autowired(required = false) Clock clock) {
        this.timeRecordRepository = timeRecordRepository;
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.clock = clock != null ? clock : Clock.systemDefaultZone();
    }

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
}