package com.project.ponto.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ponto.dto.EmployeeRequest;
import com.project.ponto.dto.EmployeeResponse;
import com.project.ponto.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    // GET /funcionarios (com suporte a ?page=1&size=10) -> 200
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> listAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        List<EmployeeResponse> list = service.listAll(page, size);
        return ResponseEntity.ok(list);
    }

    // GET /funcionarios/{id} -> 200 (ou 404)
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // POST /funcionarios -> 201 Created (ou 400 / 409)
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@RequestBody EmployeeRequest request) {
        EmployeeResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /funcionarios/{id} -> 200 (ou 400 / 404 / 409)
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // PATCH /funcionarios/{id} -> 200 (ou 400 / 404 / 409)
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> patch(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(service.patch(id, request));
    }

    // DELETE /funcionarios/{id} -> 200 (ou 404)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}