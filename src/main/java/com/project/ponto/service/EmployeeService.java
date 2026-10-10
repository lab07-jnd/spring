package com.project.ponto.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.vo.CPF;
import com.project.ponto.domain.vo.Email;
import com.project.ponto.dto.EmployeeRequest;
import com.project.ponto.dto.EmployeeResponse;
import com.project.ponto.exception.ConflictException;
import com.project.ponto.exception.ResourceNotFoundException;
import com.project.ponto.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;

    // 1. Listar todos (com suporte à paginação ou lista completa)
    public List<EmployeeResponse> listAll(Integer page, Integer size) {
        if (page != null) {
            if (page < 1) {
                throw new IllegalArgumentException("A página deve ser maior ou igual a 1");
            }
            int pageSize = (size != null && size > 0) ? size : 10;
            PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
            return repository.findAll(pageRequest)
                    .getContent()
                    .stream()
                    .map(EmployeeResponse::from)
                    .toList();
        }

        return repository.findAll()
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    // 2. Buscar por ID (ou 404 se não achar)
    public EmployeeResponse findById(Long id) {
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com id: " + id));
        return EmployeeResponse.from(employee);
    }

    // 3. Criar (POST) - valida conflito de CPF e Email
    public EmployeeResponse create(EmployeeRequest request) {
        CPF cpf = new CPF(request.cpf());
        Email email = new Email(request.email());

        if (repository.existsByCpf(cpf)) {
            throw new ConflictException("CPF já cadastrado");
        }

        if (repository.existsByEmail(email)) {
            throw new ConflictException("Email já cadastrado");
        }

        Employee employee = new Employee();
        employee.setRegistrationNumber(request.registrationNumber());
        employee.setName(request.name());
        employee.setCpf(cpf);
        employee.setEmail(email);
        employee.setRole(request.role());
        employee.setDepartment(request.department());
        employee.setStatus(request.status());

        Employee saved = repository.save(employee);
        return EmployeeResponse.from(saved);
    }

    // 4. Atualizar tudo (PUT)
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com id: " + id));

        CPF newCpf = new CPF(request.cpf());
        Email newEmail = new Email(request.email());

        // Se mudou o CPF, garante que não pertence a outro funcionário
        if (!existing.getCpf().equals(newCpf) && repository.existsByCpf(newCpf)) {
            throw new ConflictException("CPF já cadastrado para outro funcionário");
        }

        // Se mudou o Email, garante que não pertence a outro funcionário
        if (!existing.getEmail().equals(newEmail) && repository.existsByEmail(newEmail)) {
            throw new ConflictException("Email já cadastrado para outro funcionário");
        }

        existing.setRegistrationNumber(request.registrationNumber());
        existing.setName(request.name());
        existing.setCpf(newCpf);
        existing.setEmail(newEmail);
        existing.setRole(request.role());
        existing.setDepartment(request.department());
        existing.setStatus(request.status());

        Employee updated = repository.save(existing);
        return EmployeeResponse.from(updated);
    }

    // 5. Atualizar parcialmente (PATCH)
    public EmployeeResponse patch(Long id, EmployeeRequest request) {
        Employee existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com id: " + id));

        if (request.cpf() != null) {
            CPF newCpf = new CPF(request.cpf());
            if (!existing.getCpf().equals(newCpf) && repository.existsByCpf(newCpf)) {
                throw new ConflictException("CPF já cadastrado para outro funcionário");
            }
            existing.setCpf(newCpf);
        }

        if (request.email() != null) {
            Email newEmail = new Email(request.email());
            if (!existing.getEmail().equals(newEmail) && repository.existsByEmail(newEmail)) {
                throw new ConflictException("Email já cadastrado para outro funcionário");
            }
            existing.setEmail(newEmail);
        }

        if (request.name() != null) {
            existing.setName(request.name());
        }
        if (request.registrationNumber() != null) {
            existing.setRegistrationNumber(request.registrationNumber());
        }
        if (request.role() != null) {
            existing.setRole(request.role());
        }
        if (request.department() != null) {
            existing.setDepartment(request.department());
        }
        if (request.status() != null) {
            existing.setStatus(request.status());
        }

        Employee updated = repository.save(existing);
        return EmployeeResponse.from(updated);
    }

    // 6. Deletar (DELETE)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Funcionário não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }
}