package com.project.ponto.controller;

import com.project.ponto.dto.TimeRecordRequest;
import com.project.ponto.dto.TimeRecordResponse;
import com.project.ponto.service.TimeRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pontos")
@RequiredArgsConstructor
public class TimeRecordController {

    private final TimeRecordService service;

    @PostMapping
    public ResponseEntity<TimeRecordResponse> register(@Valid @RequestBody TimeRecordRequest request) {
        TimeRecordResponse response = service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}