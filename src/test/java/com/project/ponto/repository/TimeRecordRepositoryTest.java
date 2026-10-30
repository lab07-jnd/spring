package com.project.ponto.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import com.project.ponto.domain.enums.ApprovalStatusEnum;
import com.project.ponto.domain.enums.ContractStatusEnum;
import com.project.ponto.domain.enums.ContractTypeEnum;
import com.project.ponto.domain.enums.EmployeeStatusEnum;
import com.project.ponto.domain.enums.JustificationTypeEnum;
import com.project.ponto.domain.enums.TimeRecordOriginEnum;
import com.project.ponto.domain.enums.TimeRecordTypeEnum;
import com.project.ponto.domain.model.Contract;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.Justification;
import com.project.ponto.domain.model.TimeRecord;
import com.project.ponto.domain.vo.CPF;
import com.project.ponto.domain.vo.Email;
import com.project.ponto.domain.vo.Geolocation;
import com.project.ponto.dto.TimeRecordResponse;

@DataJpaTest
@DisplayName("TimeRecordRepository - list by employee and period")
class TimeRecordRepositoryTest {

    // Window [2026-10-01 00:00, 2026-11-01 00:00): October 2026
    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 1, 0, 0);
    private static final LocalDateTime END_EXCLUSIVE = LocalDateTime.of(2026, 11, 1, 0, 0);

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TimeRecordRepository repository;

    private Employee employee;
    private Contract contract;

    @BeforeEach
    void setUp() {
        employee = persistEmployee("001", "52998224725", "ana@empresa.com");
        contract = persistContract(employee);
    }

    @Test
    @DisplayName("Must project every field of the time record")
    // One id range per table: identity counters are not rolled back between tests, so the
    // four ids could coincide and a swap of t.id, e.id, c.id and j.id in the select new would pass
    @Sql(statements = {
            "ALTER TABLE time_record ALTER COLUMN id RESTART WITH 1000",
            "ALTER TABLE contract ALTER COLUMN id RESTART WITH 2000",
            "ALTER TABLE justification ALTER COLUMN id RESTART WITH 3000",
            "ALTER TABLE employee ALTER COLUMN id RESTART WITH 4000"},
            config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void mustProjectEveryField() {
        Justification justification = persistJustification(employee);

        TimeRecord record = new TimeRecord();
        record.setContract(contract);
        record.setJustification(justification);
        record.setDateTime(LocalDateTime.of(2026, 10, 10, 12, 0));
        record.setType(TimeRecordTypeEnum.LUNCH_OUT);
        // Only the first clock-in of the day gets late minutes in practice; a non-zero value tests the projection
        record.setLateMinutes(17);
        record.setOrigin(TimeRecordOriginEnum.APP);
        record.setNotes("Saída para almoço");
        record.setGeolocation(new Geolocation(-23.55, -46.63));
        em.persist(record);
        // @PrePersist sets createdAt to now(); a fixed value makes the expectation a literal
        record.setCreatedAt(LocalDateTime.of(2026, 10, 10, 12, 0, 5));
        assertThat(List.of(record.getId(), employee.getId(), contract.getId(), justification.getId()))
                .doesNotHaveDuplicates();

        List<TimeRecordResponse> result = findInOctober(employee);

        assertThat(result).containsExactly(new TimeRecordResponse(
                record.getId(),
                employee.getId(),
                employee.getName(),
                contract.getId(),
                LocalDateTime.of(2026, 10, 10, 12, 0),
                TimeRecordTypeEnum.LUNCH_OUT,
                17,
                TimeRecordOriginEnum.APP,
                -23.55,
                -46.63,
                "Saída para almoço",
                LocalDateTime.of(2026, 10, 10, 12, 0, 5),
                justification.getId()));
    }

    @Test
    @DisplayName("Must return a record without justification, with a null justificationId")
    void mustReturnRecordWithoutJustification() {
        TimeRecord record = persistRecord(contract, LocalDateTime.of(2026, 10, 10, 8, 0));

        List<TimeRecordResponse> result = findInOctober(employee);

        assertThat(result)
                .extracting(TimeRecordResponse::id, TimeRecordResponse::justificationId)
                .containsExactly(tuple(record.getId(), null));
    }

    @Test
    @DisplayName("Must return records of every contract of the employee, including closed ones")
    void mustReturnRecordsOfEveryContract() {
        Contract closed = persistContract(employee);
        closed.setStatus(ContractStatusEnum.CLOSED);
        closed.setTerminationDate(LocalDate.of(2026, 10, 15));
        TimeRecord fromClosed = persistRecord(closed, LocalDateTime.of(2026, 10, 14, 8, 0));
        TimeRecord fromActive = persistRecord(contract, LocalDateTime.of(2026, 10, 16, 8, 0));

        List<TimeRecordResponse> result = findInOctober(employee);

        assertThat(result)
                .extracting(TimeRecordResponse::id)
                .containsExactlyInAnyOrder(fromClosed.getId(), fromActive.getId());
    }

    @Test
    @DisplayName("Must return records of an inactive employee")
    void mustReturnRecordsOfInactiveEmployee() {
        employee.setStatus(EmployeeStatusEnum.INACTIVE);
        TimeRecord record = persistRecord(contract, LocalDateTime.of(2026, 10, 10, 8, 0));

        List<TimeRecordResponse> result = findInOctober(employee);

        assertThat(result).extracting(TimeRecordResponse::id).containsExactly(record.getId());
    }

    @Test
    @DisplayName("Must not return records of another employee")
    void mustNotReturnRecordsOfAnotherEmployee() {
        Employee other = persistEmployee("002", "11144477735", "bruno@empresa.com");
        TimeRecord own = persistRecord(contract, LocalDateTime.of(2026, 10, 10, 8, 0));
        persistRecord(persistContract(other), LocalDateTime.of(2026, 10, 10, 8, 0));

        List<TimeRecordResponse> result = findInOctober(employee);

        assertThat(result).extracting(TimeRecordResponse::id).containsExactly(own.getId());
    }

    @Test
    @DisplayName("Must include the start instant and exclude anything before it")
    void mustTreatStartAsInclusive() {
        TimeRecord atStart = persistRecord(contract, LocalDateTime.of(2026, 10, 1, 0, 0));
        persistRecord(contract, LocalDateTime.of(2026, 9, 30, 23, 59, 59, 999_999_000));

        List<TimeRecordResponse> result = findInOctober(employee);

        assertThat(result).extracting(TimeRecordResponse::id).containsExactly(atStart.getId());
    }

    @Test
    @DisplayName("Must include the last microsecond before endExclusive and exclude endExclusive itself")
    void mustTreatEndAsExclusive() {
        TimeRecord lastMicrosecond = persistRecord(contract, LocalDateTime.of(2026, 10, 31, 23, 59, 59, 999_999_000));
        persistRecord(contract, LocalDateTime.of(2026, 11, 1, 0, 0));

        List<TimeRecordResponse> result = findInOctober(employee);

        assertThat(result).extracting(TimeRecordResponse::id).containsExactly(lastMicrosecond.getId());
    }

    @Test
    @DisplayName("Must return records in chronological order, regardless of insertion order")
    void mustOrderByDateTime() {
        TimeRecord third = persistRecord(contract, LocalDateTime.of(2026, 10, 12, 8, 0));
        TimeRecord first = persistRecord(contract, LocalDateTime.of(2026, 10, 10, 8, 0));
        TimeRecord second = persistRecord(contract, LocalDateTime.of(2026, 10, 11, 8, 0));

        List<TimeRecordResponse> result = findInOctober(employee);

        assertThat(result)
                .extracting(TimeRecordResponse::id)
                .containsExactly(first.getId(), second.getId(), third.getId());
    }

    private List<TimeRecordResponse> findInOctober(Employee owner) {
        em.flush();
        em.clear();
        return repository.findByEmployeeAndPeriod(owner.getId(), START, END_EXCLUSIVE, Pageable.unpaged());
    }

    private Employee persistEmployee(String registrationNumber, String cpf, String email) {
        Employee e = new Employee();
        e.setRegistrationNumber(registrationNumber);
        e.setName("Funcionário " + registrationNumber);
        e.setCpf(new CPF(cpf));
        e.setEmail(new Email(email));
        e.setRole("Analista");
        e.setDepartment("TI");
        e.setStatus(EmployeeStatusEnum.ACTIVE);
        return em.persist(e);
    }

    // Only contract.employee is set: filling contract.timeRecords too would make
    // the bidirectional @Data toString/hashCode recurse
    private Contract persistContract(Employee owner) {
        Contract c = new Contract();
        c.setEmployee(owner);
        c.setAdmissionDate(LocalDate.of(2026, 1, 5));
        c.setType(ContractTypeEnum.CLT);
        c.setStatus(ContractStatusEnum.ACTIVE);
        c.setBaseSalaryCents(500_000);
        return em.persist(c);
    }

    private TimeRecord persistRecord(Contract owner, LocalDateTime dateTime) {
        TimeRecord r = new TimeRecord();
        r.setContract(owner);
        r.setDateTime(dateTime);
        r.setType(TimeRecordTypeEnum.CLOCK_IN);
        r.setOrigin(TimeRecordOriginEnum.WEB);
        return em.persist(r);
    }

    private Justification persistJustification(Employee owner) {
        Justification j = new Justification();
        j.setEmployee(owner);
        j.setStartDate(LocalDate.of(2026, 10, 10));
        j.setEndDate(LocalDate.of(2026, 10, 10));
        j.setReason("Consulta médica");
        j.setType(JustificationTypeEnum.MEDICAL_CERTIFICATE);
        j.setStatus(ApprovalStatusEnum.APPROVED);
        return em.persist(j);
    }
}