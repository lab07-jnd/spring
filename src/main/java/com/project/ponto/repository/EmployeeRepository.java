package com.project.ponto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.vo.CPF;
import com.project.ponto.domain.vo.Email;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByCpf(CPF cpf);

    boolean existsByEmail(Email email);
}