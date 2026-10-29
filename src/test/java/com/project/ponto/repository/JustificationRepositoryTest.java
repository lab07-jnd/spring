package com.project.ponto.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.project.ponto.domain.enums.ApprovalStatusEnum;
import com.project.ponto.domain.enums.EmployeeStatusEnum;
import com.project.ponto.domain.enums.JustificationTypeEnum;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.Justification;
import com.project.ponto.domain.vo.CPF;
import com.project.ponto.domain.vo.Email;

@DataJpaTest
@DisplayName("JustificationRepository")
class JustificationRepositoryTest {

    private static final List<ApprovalStatusEnum> BLOCKING_STATUSES =
            List.of(ApprovalStatusEnum.PENDING, ApprovalStatusEnum.APPROVED);

    @Autowired
    private JustificationRepository repository;

    @Autowired
    private TestEntityManager em;

    private Employee owner;
    private Employee other;

    @BeforeEach
    void setUp() {
        owner = persistEmployee("REG-1", "Ana Souza", "529.982.247-25", "ana@empresa.com");
        other = persistEmployee("REG-2", "Carlos Lima", "111.444.777-35", "carlos@empresa.com");
    }

    @Test
    @DisplayName("Must detect overlap when a new period touches the existing boundary")
    void mustDetectOverlapWhenPeriodsTouch() {
        persist(owner, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3),
                ApprovalStatusEnum.APPROVED, LocalDateTime.of(2026, 10, 30, 10, 0));
        em.flush();

        assertThat(overlaps(owner, LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 5))).isTrue();
        assertThat(overlaps(owner, LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 1))).isTrue();
        assertThat(overlaps(owner, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2))).isTrue();
    }

    @Test
    @DisplayName("Must not detect overlap when periods are adjacent")
    void mustNotDetectOverlapWhenPeriodsAreAdjacent() {
        persist(owner, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3),
                ApprovalStatusEnum.APPROVED, LocalDateTime.of(2026, 10, 30, 10, 0));
        em.flush();

        assertThat(overlaps(owner, LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 5))).isFalse();
        assertThat(overlaps(owner, LocalDate.of(2026, 10, 30), LocalDate.of(2026, 10, 31))).isFalse();
    }

    @Test
    @DisplayName("Must ignore rejected justifications when checking overlap")
    void mustIgnoreRejectedJustifications() {
        persist(owner, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3),
                ApprovalStatusEnum.REJECTED, LocalDateTime.of(2026, 10, 30, 10, 0));
        em.flush();

        assertThat(overlaps(owner, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3))).isFalse();
    }

    @Test
    @DisplayName("Must ignore justifications from another employee when checking overlap")
    void mustIgnoreOtherEmployees() {
        persist(other, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3),
                ApprovalStatusEnum.APPROVED, LocalDateTime.of(2026, 10, 30, 10, 0));
        em.flush();

        assertThat(overlaps(owner, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3))).isFalse();
    }

    @Test
    @DisplayName("Must list pending justifications ordered by requestedAt and id")
    void mustListPendingOrderedByRequestedAtAndId() {
        Justification later = persist(owner, LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 3),
                ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 2, 10, 0));
        Justification earlier = persist(owner, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 1, 10, 0));
        persist(owner, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 5),
                ApprovalStatusEnum.APPROVED, LocalDateTime.of(2026, 11, 4, 10, 0));
        em.flush();

        List<Justification> result = repository.findByStatusOrderByRequestedAtAscIdAsc(
                ApprovalStatusEnum.PENDING, Pageable.unpaged());

        assertThat(result).extracting(Justification::getId)
                .containsExactly(earlier.getId(), later.getId());
    }

    @Test
    @DisplayName("Must list an employee's justifications ordered by requestedAt desc and id desc")
    void mustListEmployeeJustificationsNewestFirst() {
        Justification early = persist(owner, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 1, 10, 0));
        Justification latest = persist(owner, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 5),
                ApprovalStatusEnum.APPROVED, LocalDateTime.of(2026, 11, 5, 10, 0));
        Justification middle = persist(owner, LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 3),
                ApprovalStatusEnum.REJECTED, LocalDateTime.of(2026, 11, 3, 10, 0));
        persist(other, LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 6),
                ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 6, 10, 0));
        em.flush();

        List<Justification> result = repository.findByEmployeeIdOrderByRequestedAtDescIdDesc(
                owner.getId(), Pageable.unpaged());

        assertThat(result).extracting(Justification::getId)
                .containsExactly(latest.getId(), middle.getId(), early.getId());
    }

    @Test
    @DisplayName("Must honor pagination when listing an employee's justifications")
    void mustPaginateEmployeeJustifications() {
        Justification early = persist(owner, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1),
                ApprovalStatusEnum.PENDING, LocalDateTime.of(2026, 11, 1, 10, 0));
        Justification latest = persist(owner, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 5),
                ApprovalStatusEnum.APPROVED, LocalDateTime.of(2026, 11, 5, 10, 0));
        em.flush();

        List<Justification> firstPage = repository.findByEmployeeIdOrderByRequestedAtDescIdDesc(
                owner.getId(), PageRequest.of(0, 1));
        List<Justification> secondPage = repository.findByEmployeeIdOrderByRequestedAtDescIdDesc(
                owner.getId(), PageRequest.of(1, 1));

        assertThat(firstPage).extracting(Justification::getId).containsExactly(latest.getId());
        assertThat(secondPage).extracting(Justification::getId).containsExactly(early.getId());
    }

    private boolean overlaps(Employee employee, LocalDate startDate, LocalDate endDate) {
        return repository.existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                employee.getId(), BLOCKING_STATUSES, endDate, startDate);
    }

    private Employee persistEmployee(String registrationNumber, String name, String cpf, String email) {
        Employee employee = new Employee();
        employee.setRegistrationNumber(registrationNumber);
        employee.setName(name);
        employee.setCpf(new CPF(cpf));
        employee.setEmail(new Email(email));
        employee.setRole("Analista");
        employee.setDepartment("TI");
        employee.setStatus(EmployeeStatusEnum.ACTIVE);
        return em.persist(employee);
    }

    private Justification persist(Employee employee, LocalDate startDate, LocalDate endDate,
            ApprovalStatusEnum status, LocalDateTime requestedAt) {
        Justification justification = new Justification();
        justification.setEmployee(employee);
        justification.setStartDate(startDate);
        justification.setEndDate(endDate);
        justification.setReason("Motivo da justificativa");
        justification.setType(JustificationTypeEnum.MEDICAL_CERTIFICATE);
        justification.setStatus(status);
        justification.setRequestedAt(requestedAt);
        return em.persist(justification);
    }
}
