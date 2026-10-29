package com.project.ponto.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ponto.dto.JustificationRejectionRequest;
import com.project.ponto.dto.JustificationRequest;
import com.project.ponto.dto.JustificationResponse;
import com.project.ponto.security.AuthenticatedEmployeeIdProvider;
import com.project.ponto.service.JustificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class JustificationController {

    private final JustificationService service;
    private final AuthenticatedEmployeeIdProvider employeeIdProvider;

    // POST /justifications -> 201 (or 400 / 404 / 409)
    @PostMapping("/justifications")
    public ResponseEntity<JustificationResponse> create(
            @Valid @RequestBody JustificationRequest request,
            Authentication authentication) {
        Long employeeId = employeeIdProvider.getEmployeeId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(employeeId, request));
    }

    // GET /employees/{employeeId}/justifications?page=1&size=10 -> 200 (or 400 / 404)
    @GetMapping("/employees/{employeeId}/justifications")
    public ResponseEntity<List<JustificationResponse>> listByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(service.listByEmployee(employeeId, page, size));
    }

    // GET /justifications/pending?page=1&size=10 -> 200 (or 400)
    @GetMapping("/justifications/pending")
    public ResponseEntity<List<JustificationResponse>> listPending(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(service.listPending(page, size));
    }

    // POST /justifications/{id}/approval -> 200 (or 404 / 409)
    @PostMapping("/justifications/{id}/approval")
    public ResponseEntity<JustificationResponse> approve(
            @PathVariable Long id,
            Authentication authentication) {
        Long managerId = employeeIdProvider.getEmployeeId(authentication);
        return ResponseEntity.ok(service.approve(managerId, id));
    }

    // POST /justifications/{id}/rejection -> 200 (or 400 / 404 / 409)
    @PostMapping("/justifications/{id}/rejection")
    public ResponseEntity<JustificationResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) JustificationRejectionRequest request,
            Authentication authentication) {
        Long managerId = employeeIdProvider.getEmployeeId(authentication);
        String rejectionReason = request != null ? request.rejectionReason() : null;
        return ResponseEntity.ok(service.reject(managerId, id, rejectionReason));
    }

}
