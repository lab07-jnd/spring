package com.project.ponto.dto;

import java.time.LocalDateTime;
import java.util.Optional;

import com.project.ponto.domain.enums.TimeRecordOriginEnum;
import com.project.ponto.domain.enums.TimeRecordTypeEnum;
import com.project.ponto.domain.model.Contract;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.TimeRecord;
import com.project.ponto.domain.vo.Geolocation;

// A ordem dos componentes precisa bater com o "select new" de
// TimeRecordRepository.findByEmployeeAndPeriod
public record TimeRecordResponse(
        Long id,
        Long employeeId,
        String employeeName,
        Long contractId,
        LocalDateTime dateTime,
        TimeRecordTypeEnum type,
        int lateMinutes,
        TimeRecordOriginEnum origin,
        Double latitude,
        Double longitude,
        String notes,
        LocalDateTime createdAt,
        Long justificationId
) {

    public static TimeRecordResponse from(TimeRecord record) {
        Contract contract = record.getContract();
        Employee employee = Optional.ofNullable(contract).map(Contract::getEmployee).orElse(null);
        Geolocation geo = record.getGeolocation();

        return new TimeRecordResponse(
                record.getId(),
                employee != null ? employee.getId() : null,
                employee != null ? employee.getName() : null,
                contract != null ? contract.getId() : null,
                record.getDateTime(),
                record.getType(),
                record.getLateMinutes(),
                record.getOrigin(),
                geo != null ? geo.latitude() : null,
                geo != null ? geo.longitude() : null,
                record.getNotes(),
                record.getCreatedAt(),
                record.getJustification() != null ? record.getJustification().getId() : null
        );
    }
}