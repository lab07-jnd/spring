package com.project.ponto.service;

import com.project.ponto.domain.model.HourBank;
import com.project.ponto.repository.HourBankMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.ponto.repository.HourBankRepository;

import java.util.List;
import java.util.Optional;

@Service

public class HourBankService {
    @Autowired
    private final HourBankRepository repository;

    private final HourBankMovementRepository movementRepository;



    public HourBankService(HourBankRepository repository, HourBankMovementRepository movmentRepository) {
        this.repository = repository;
        this.movementRepository = movmentRepository;
    }

    public HourBank findById(Long id){
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found: " + id));
    }

    //listar as horas por funcionario
    public List<HourBank> listByEmployee(Long employeeId) {
        return repository.findByEmployeeId(employeeId);
    }
    //adicionar as horas atuais as antigas

    //Acumular saldo positivo/negativo de horas ao longo do mês por funcionário

    //usar http status para retorno

}
