package com.project.ponto.service;

import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.HourBank;
import com.project.ponto.repository.HourBankMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.project.ponto.repository.HourBankRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service

public class HourBankService {
    @Autowired
    private final HourBankRepository repository;

    private final HourBankMovementRepository movementRepository;


    public HourBankService(HourBankRepository repository, HourBankMovementRepository movmentRepository) {
        this.repository = repository;
        this.movementRepository = movmentRepository;
    }

    public HourBank findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found: " + id));
    }

    //listar as horas por funcionario
    public List<HourBank> listByEmployee(Long employeeId) {
        return repository.findByEmployeeId(employeeId);
    }

    //adicionar as horas atuais por empregado
    public HourBank addHours(Employee employee, HourBank hours) {
        // Verify that the employee exists
        Employee existingEmployee = repository
                .findById(employee.getId())
                .orElseThrow(() ->
                        new RuntimeException("Employee not found")).getEmployee();

        // Associate the hour bank with the employee
        hours.setEmployee(existingEmployee);

        // Save the hour bank
        return repository.save(hours);
    }

    //Acumular saldo positivo/negativo de horas ao longo do mês por funcionário
    public HourBank balance(Employee employee, int minutes, HourBank hours){
        hours.setPreviousBalanceMinutes(hours.getBalanceMinutes());
        hours.setBalanceMinutes(minutes+hours.getBalanceMinutes());
        return repository.save(hours);
    }


}
