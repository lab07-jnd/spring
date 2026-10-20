package com.project.ponto.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ponto.dto.TimeRecordResponse;
import com.project.ponto.service.TimeRecordService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimeRecordController {

    private final TimeRecordService service;

    // GET /funcionarios/{funcionarioId}/pontos?dataInicio=yyyy-MM-dd&dataFim=yyyy-MM-dd&page=1&size=10
    // -> 200 (ou 400 / 404). Sem datas, mês corrente; sem page, todos os pontos
    @GetMapping("/funcionarios/{funcionarioId}/pontos")
    public ResponseEntity<List<TimeRecordResponse>> listByEmployee(
            @PathVariable("funcionarioId") Long employeeId,
            @RequestParam(name = "dataInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "dataFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(service.listByEmployee(employeeId, startDate, endDate, page, size));
    }
//TODO: get period by employee (filter by data)
}
