package com.project.ponto.repository;

import com.project.ponto.domain.enums.*;
import com.project.ponto.domain.model.Contract;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.TimeRecord;
import com.project.ponto.domain.model.WorkSchedule;
import com.project.ponto.domain.vo.CPF;
import com.project.ponto.domain.vo.Email;
import com.project.ponto.domain.vo.Geolocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TimeRecord Entity & Repository Persistence Tests")
class TimeRecordPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    private Employee employee;
    private Contract contract;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setRegistrationNumber("REG-001");
        employee.setName("João Silva");
        employee.setCpf(new CPF("529.982.247-25"));
        employee.setEmail(new Email("joao.silva@empresa.com"));
        employee.setRole("Desenvolvedor");
        employee.setDepartment("TI");
        employee.setStatus(EmployeeStatusEnum.ACTIVE);
        em.persistAndFlush(employee);

        WorkSchedule schedule = new WorkSchedule();
        schedule.setName("Comercial");
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setDailyWorkloadMinutes(480);
        schedule.setBreakMinutes(60);
        schedule.setWorkedDays(List.of(DayOfWeekEnum.MONDAY, DayOfWeekEnum.TUESDAY, DayOfWeekEnum.WEDNESDAY, DayOfWeekEnum.THURSDAY, DayOfWeekEnum.FRIDAY));
        schedule.setActive(true);
        em.persistAndFlush(schedule);

        contract = new Contract();
        contract.setEmployee(employee);
        contract.setWorkSchedule(schedule);
        contract.setType(ContractTypeEnum.CLT);
        contract.setBaseSalaryCents(500000);
        contract.setAdmissionDate(LocalDate.of(2025, 1, 1));
        contract.setStatus(ContractStatusEnum.ACTIVE);
        em.persistAndFlush(contract);

        em.clear();
    }

    @Nested
    @DisplayName("1. Basic Persistence and Round-Trip")
    class BasicPersistence {

        @Test
        @DisplayName("Must persist and reload TimeRecord with complete attributes")
        void mustPersistAndReloadSuccessfully() {
            Contract attachedContract = em.find(Contract.class, contract.getId());

            TimeRecord record = new TimeRecord();
            record.setContract(attachedContract);
            record.setDateTime(LocalDateTime.of(2026, 10, 16, 9, 0, 0));
            record.setType(TimeRecordTypeEnum.CLOCK_IN);
            record.setOrigin(TimeRecordOriginEnum.APP);
            record.setNotes("Entrada normal");
            record.setGeolocation(new Geolocation(-23.550520, -46.633308));

            TimeRecord saved = em.persistAndFlush(record);
            em.clear();

            TimeRecord reloaded = em.find(TimeRecord.class, saved.getId());

            assertThat(reloaded).isNotNull();
            assertThat(reloaded.getContract().getId()).isEqualTo(contract.getId());
            assertThat(reloaded.getType()).isEqualTo(TimeRecordTypeEnum.CLOCK_IN);
            assertThat(reloaded.getOrigin()).isEqualTo(TimeRecordOriginEnum.APP);
            assertThat(reloaded.getNotes()).isEqualTo("Entrada normal");
            assertThat(reloaded.getGeolocation()).isNotNull();
            assertThat(reloaded.getGeolocation().latitude()).isEqualTo(-23.550520);
            assertThat(reloaded.getGeolocation().longitude()).isEqualTo(-46.633308);
            assertThat(reloaded.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Must persist and reload TimeRecord without geolocation (null)")
        void mustPersistWithoutGeolocation() {
            Contract attachedContract = em.find(Contract.class, contract.getId());

            TimeRecord record = new TimeRecord();
            record.setContract(attachedContract);
            record.setDateTime(LocalDateTime.of(2026, 10, 16, 9, 0, 0));
            record.setType(TimeRecordTypeEnum.CLOCK_IN);
            record.setOrigin(TimeRecordOriginEnum.WEB);
            record.setNotes("Registro Web sem GPS");
            record.setGeolocation(null);

            TimeRecord saved = em.persistAndFlush(record);
            em.clear();

            TimeRecord reloaded = em.find(TimeRecord.class, saved.getId());

            assertThat(reloaded).isNotNull();
            assertThat(reloaded.getGeolocation()).isNull();
        }
    }

    @Nested
    @DisplayName("2. BVA Boundary Persistence")
    class BoundaryPersistence {

        @Test
        @DisplayName("Must persist extreme boundary coordinates (-90.0, -180.0)")
        void mustPersistMinCoordinates() {
            Contract attachedContract = em.find(Contract.class, contract.getId());

            TimeRecord record = new TimeRecord();
            record.setContract(attachedContract);
            record.setDateTime(LocalDateTime.of(2026, 10, 16, 9, 0, 0));
            record.setType(TimeRecordTypeEnum.CLOCK_IN);
            record.setOrigin(TimeRecordOriginEnum.APP);
            record.setGeolocation(new Geolocation(-90.0, -180.0));

            TimeRecord saved = em.persistAndFlush(record);
            em.clear();

            TimeRecord reloaded = em.find(TimeRecord.class, saved.getId());
            assertThat(reloaded.getGeolocation().latitude()).isEqualTo(-90.0);
            assertThat(reloaded.getGeolocation().longitude()).isEqualTo(-180.0);
        }

        @Test
        @DisplayName("Must persist extreme boundary coordinates (90.0, 180.0)")
        void mustPersistMaxCoordinates() {
            Contract attachedContract = em.find(Contract.class, contract.getId());

            TimeRecord record = new TimeRecord();
            record.setContract(attachedContract);
            record.setDateTime(LocalDateTime.of(2026, 10, 16, 9, 0, 0));
            record.setType(TimeRecordTypeEnum.CLOCK_IN);
            record.setOrigin(TimeRecordOriginEnum.APP);
            record.setGeolocation(new Geolocation(90.0, 180.0));

            TimeRecord saved = em.persistAndFlush(record);
            em.clear();

            TimeRecord reloaded = em.find(TimeRecord.class, saved.getId());
            assertThat(reloaded.getGeolocation().latitude()).isEqualTo(90.0);
            assertThat(reloaded.getGeolocation().longitude()).isEqualTo(180.0);
        }

        @Test
        @DisplayName("Must persist maximum allowed notes length (255 characters)")
        void mustPersistMaxNotesLength() {
            Contract attachedContract = em.find(Contract.class, contract.getId());
            String maxNotes = "a".repeat(255);

            TimeRecord record = new TimeRecord();
            record.setContract(attachedContract);
            record.setDateTime(LocalDateTime.of(2026, 10, 16, 9, 0, 0));
            record.setType(TimeRecordTypeEnum.CLOCK_IN);
            record.setOrigin(TimeRecordOriginEnum.WEB);
            record.setNotes(maxNotes);

            TimeRecord saved = em.persistAndFlush(record);
            em.clear();

            TimeRecord reloaded = em.find(TimeRecord.class, saved.getId());
            assertThat(reloaded.getNotes()).hasSize(255);
        }
    }

    @Nested
    @DisplayName("3. Repository Query Methods")
    class RepositoryQueries {

        @Test
        @DisplayName("Must find records of a contract within a date-time interval ordered ascending")
        void mustFindRecordsBetweenDatesAscending() {
            Contract attachedContract = em.find(Contract.class, contract.getId());

            TimeRecord rec1 = new TimeRecord();
            rec1.setContract(attachedContract);
            rec1.setDateTime(LocalDateTime.of(2026, 10, 16, 9, 0, 0));
            rec1.setType(TimeRecordTypeEnum.CLOCK_IN);
            rec1.setOrigin(TimeRecordOriginEnum.APP);
            em.persist(rec1);

            TimeRecord rec2 = new TimeRecord();
            rec2.setContract(attachedContract);
            rec2.setDateTime(LocalDateTime.of(2026, 10, 16, 12, 0, 0));
            rec2.setType(TimeRecordTypeEnum.LUNCH_OUT);
            rec2.setOrigin(TimeRecordOriginEnum.APP);
            em.persist(rec2);

            em.flush();
            em.clear();

            LocalDateTime startOfDay = LocalDateTime.of(2026, 10, 16, 0, 0, 0);
            LocalDateTime endOfDay = LocalDateTime.of(2026, 10, 16, 23, 59, 59);

            List<TimeRecord> records = timeRecordRepository
                    .findByContractIdAndDateTimeBetweenOrderByDateTimeAsc(contract.getId(), startOfDay, endOfDay);

            assertThat(records).hasSize(2);
            assertThat(records.get(0).getType()).isEqualTo(TimeRecordTypeEnum.CLOCK_IN);
            assertThat(records.get(1).getType()).isEqualTo(TimeRecordTypeEnum.LUNCH_OUT);
        }

        @Test
        @DisplayName("Must find latest record of a contract")
        void mustFindLatestRecord() {
            Contract attachedContract = em.find(Contract.class, contract.getId());

            TimeRecord rec1 = new TimeRecord();
            rec1.setContract(attachedContract);
            rec1.setDateTime(LocalDateTime.of(2026, 10, 16, 9, 0, 0));
            rec1.setType(TimeRecordTypeEnum.CLOCK_IN);
            rec1.setOrigin(TimeRecordOriginEnum.APP);
            em.persist(rec1);

            TimeRecord rec2 = new TimeRecord();
            rec2.setContract(attachedContract);
            rec2.setDateTime(LocalDateTime.of(2026, 10, 16, 12, 30, 0));
            rec2.setType(TimeRecordTypeEnum.LUNCH_OUT);
            rec2.setOrigin(TimeRecordOriginEnum.APP);
            em.persist(rec2);

            em.flush();
            em.clear();

            Optional<TimeRecord> latest = timeRecordRepository.findTopByContractIdOrderByDateTimeDesc(contract.getId());

            assertThat(latest).isPresent();
            assertThat(latest.get().getType()).isEqualTo(TimeRecordTypeEnum.LUNCH_OUT);
            assertThat(latest.get().getDateTime()).isEqualTo(LocalDateTime.of(2026, 10, 16, 12, 30, 0));
        }
    }
}
