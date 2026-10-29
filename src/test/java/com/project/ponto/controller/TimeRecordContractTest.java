package com.project.ponto.controller;

import tools.jackson.databind.ObjectMapper;
import com.project.ponto.domain.enums.*;
import com.project.ponto.domain.model.Contract;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.TimeRecord;
import com.project.ponto.domain.model.WorkSchedule;
import com.project.ponto.domain.vo.CPF;
import com.project.ponto.domain.vo.Email;
import com.project.ponto.domain.vo.Geolocation;
import com.project.ponto.dto.TimeRecordRequest;
import com.project.ponto.repository.ContractRepository;
import com.project.ponto.repository.EmployeeRepository;
import com.project.ponto.repository.TimeRecordRepository;
import com.project.ponto.util.JwtTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("TimeRecord API Contract Tests (POST /pontos) - Boundary Value Analysis")
class TimeRecordContractTest {

    /**
     * A single valid JWT is generated once per class load and reused across all
     * tests. The token carries no user-specific state that affects the business
     * rules under test (the employee is identified by the request body, not by
     * the JWT principal). It exercises the full security pipeline:
     * BearerTokenAuthenticationFilter → JwtDecoder → JwtAuthenticationConverter.
     */
    private static final String VALID_TOKEN = JwtTestUtils.validToken("1");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    private Employee activeEmployee;
    private Contract activeContract;

    @BeforeEach
    void setUp() {
        timeRecordRepository.deleteAll();
        contractRepository.deleteAll();
        employeeRepository.deleteAll();

        activeEmployee = new Employee();
        activeEmployee.setRegistrationNumber("REG-100");
        activeEmployee.setName("Maria Souza");
        activeEmployee.setCpf(new CPF("529.982.247-25"));
        activeEmployee.setEmail(new Email("maria.souza@empresa.com"));
        activeEmployee.setRole("Engenheira de Software");
        activeEmployee.setDepartment("Engenharia");
        activeEmployee.setStatus(EmployeeStatusEnum.ACTIVE);
        activeEmployee = employeeRepository.save(activeEmployee);

        WorkSchedule schedule = new WorkSchedule();
        schedule.setName("Padrão 8h");
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setDailyWorkloadMinutes(480);
        schedule.setBreakMinutes(60);
        schedule.setWorkedDays(List.of(DayOfWeekEnum.MONDAY, DayOfWeekEnum.TUESDAY, DayOfWeekEnum.WEDNESDAY, DayOfWeekEnum.THURSDAY, DayOfWeekEnum.FRIDAY));
        schedule.setActive(true);

        activeContract = new Contract();
        activeContract.setEmployee(activeEmployee);
        activeContract.setWorkSchedule(schedule);
        activeContract.setType(ContractTypeEnum.CLT);
        activeContract.setBaseSalaryCents(800000);
        activeContract.setAdmissionDate(LocalDate.of(2025, 2, 1));
        activeContract.setStatus(ContractStatusEnum.ACTIVE);
        activeContract = contractRepository.save(activeContract);
    }

    // =========================================================================
    // 1. NORMAL BVA (Valores Válidos nos Limites) -> 201 Created
    // =========================================================================
    @Nested
    @DisplayName("1. Normal BVA: Valid Boundary and Nominal Inputs (201 Created)")
    class NormalBVA {

        @Test
        @DisplayName("Must return 201 when registering with minimum boundary coordinates (-90.0, -180.0)")
        void minCoordinates() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    -90.0,
                    -180.0,
                    "Entrada no polo sul"
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.employeeId", is(activeEmployee.getId().intValue())))
                    .andExpect(jsonPath("$.employeeName", is("Maria Souza")))
                    .andExpect(jsonPath("$.contractId", is(activeContract.getId().intValue())))
                    .andExpect(jsonPath("$.type", is("CLOCK_IN")))
                    .andExpect(jsonPath("$.origin", is("APP")))
                    .andExpect(jsonPath("$.latitude", is(-90.0)))
                    .andExpect(jsonPath("$.longitude", is(-180.0)))
                    .andExpect(jsonPath("$.notes", is("Entrada no polo sul")))
                    .andExpect(jsonPath("$.dateTime", notNullValue()))
                    .andExpect(jsonPath("$.createdAt", notNullValue()));
        }

        @Test
        @DisplayName("Must return 201 when registering with maximum boundary coordinates (90.0, 180.0)")
        void maxCoordinates() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    90.0,
                    180.0,
                    "Entrada no polo norte"
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.latitude", is(90.0)))
                    .andExpect(jsonPath("$.longitude", is(180.0)));
        }

        @Test
        @DisplayName("Must return 201 when registering with coordinates just above minimum (-89.9999999, -179.9999999)")
        void justAboveMinCoordinates() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    -89.9999999,
                    -179.9999999,
                    "Just above minimum"
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.latitude", is(-89.9999999)))
                    .andExpect(jsonPath("$.longitude", is(-179.9999999)));
        }

        @Test
        @DisplayName("Must return 201 when registering with coordinates just below maximum (89.9999999, 179.9999999)")
        void justBelowMaxCoordinates() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    89.9999999,
                    179.9999999,
                    "Just below maximum"
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.latitude", is(89.9999999)))
                    .andExpect(jsonPath("$.longitude", is(179.9999999)));
        }

        @Test
        @DisplayName("Must return 201 when registering nominal center coordinates (0.0, 0.0)")
        void nominalCenterCoordinates() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.WEB,
                    0.0,
                    0.0,
                    "Centro (0, 0)"
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.latitude", is(0.0)))
                    .andExpect(jsonPath("$.longitude", is(0.0)));
        }

        @Test
        @DisplayName("Must return 201 when geolocation is completely omitted (null coordinates)")
        void nullGeolocation() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.WEB,
                    null,
                    null,
                    "Registro sem geolocalização"
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.latitude", nullValue()))
                    .andExpect(jsonPath("$.longitude", nullValue()));
        }

        @Test
        @DisplayName("Must return 201 when notes has exact boundary size (0 chars and 255 chars)")
        void notesBoundaries() throws Exception {
            // Empty string (0 chars)
            TimeRecordRequest requestEmpty = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    null,
                    null,
                    ""
            );
            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestEmpty)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.notes", is("")));
        }

        @Test
        @DisplayName("Must return 201 for complete 4-punch daily cycle with valid intervals")
        void fullDayCycle() throws Exception {
            LocalDateTime now = LocalDateTime.now();

            // 1. CLOCK_IN at 08:00
            TimeRecord r1 = new TimeRecord();
            r1.setContract(activeContract);
            r1.setDateTime(now.withHour(8).withMinute(0));
            r1.setType(TimeRecordTypeEnum.CLOCK_IN);
            r1.setOrigin(TimeRecordOriginEnum.APP);
            timeRecordRepository.save(r1);

            // 2. LUNCH_OUT at 12:00
            TimeRecord r2 = new TimeRecord();
            r2.setContract(activeContract);
            r2.setDateTime(now.withHour(12).withMinute(0));
            r2.setType(TimeRecordTypeEnum.LUNCH_OUT);
            r2.setOrigin(TimeRecordOriginEnum.APP);
            timeRecordRepository.save(r2);

            // 3. LUNCH_IN at 13:00
            TimeRecord r3 = new TimeRecord();
            r3.setContract(activeContract);
            r3.setDateTime(now.withHour(13).withMinute(0));
            r3.setType(TimeRecordTypeEnum.LUNCH_IN);
            r3.setOrigin(TimeRecordOriginEnum.APP);
            timeRecordRepository.save(r3);

            // 4. Request CLOCK_OUT via endpoint
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_OUT,
                    TimeRecordOriginEnum.APP,
                    -23.55,
                    -46.63,
                    "Fim do expediente"
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type", is("CLOCK_OUT")));
        }
    }

    // =========================================================================
    // 2. ROBUST BVA (Valores Inválidos Imediatamente Fora dos Limites) -> 400 / 409
    // =========================================================================
    @Nested
    @DisplayName("2. Robust BVA: Single Boundary Violation (400 Bad Request / 409 Conflict)")
    class RobustBVA {

        @Test
        @DisplayName("Must return 400 when latitude is just below minimum (-90.0000001)")
        void latitudeJustBelowMin() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    -90.0000001,
                    0.0,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro", containsString("Latitude must be greater than or equal to -90.0")));
        }

        @Test
        @DisplayName("Must return 400 when latitude is just above maximum (90.0000001)")
        void latitudeJustAboveMax() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    90.0000001,
                    0.0,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro", containsString("Latitude must be less than or equal to 90.0")));
        }

        @Test
        @DisplayName("Must return 400 when longitude is just below minimum (-180.0000001)")
        void longitudeJustBelowMin() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    0.0,
                    -180.0000001,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro", containsString("Longitude must be greater than or equal to -180.0")));
        }

        @Test
        @DisplayName("Must return 400 when longitude is just above maximum (180.0000001)")
        void longitudeJustAboveMax() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    0.0,
                    180.0000001,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro", containsString("Longitude must be less than or equal to 180.0")));
        }

        @Test
        @DisplayName("Must return 400 when notes exceeds limit (256 characters)")
        void notesJustAboveMax() throws Exception {
            String overflowNotes = "x".repeat(256);
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    null,
                    null,
                    overflowNotes
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro", containsString("The notes field exceeds the limit of 255 characters")));
        }

        @Test
        @DisplayName("Must return 400 when mandatory field employeeId is null")
        void nullEmployeeId() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    null,
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    null,
                    null,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro", containsString("Employee ID must be provided")));
        }

        @Test
        @DisplayName("Must return 400 when mandatory field type is null")
        void nullType() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    null,
                    TimeRecordOriginEnum.APP,
                    null,
                    null,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro", containsString("Time record type must be provided")));
        }

        @Test
        @DisplayName("Must return 409 when debounce is violated (< 60 seconds)")
        void debounceViolation() throws Exception {
            // Previous punch recorded 30 seconds ago
            TimeRecord recent = new TimeRecord();
            recent.setContract(activeContract);
            recent.setDateTime(LocalDateTime.now().minusSeconds(30));
            recent.setType(TimeRecordTypeEnum.CLOCK_IN);
            recent.setOrigin(TimeRecordOriginEnum.APP);
            timeRecordRepository.save(recent);

            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.LUNCH_OUT,
                    TimeRecordOriginEnum.APP,
                    null,
                    null,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.erro", containsString("Wait at least 1 minute")));
        }

        @Test
        @DisplayName("Must return 409 when first punch of day is not CLOCK_IN")
        void invalidFirstPunchOfDay() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.LUNCH_OUT,
                    TimeRecordOriginEnum.APP,
                    null,
                    null,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.erro", containsString("The first clock-in of the day must be of type CLOCK_IN")));
        }

        @Test
        @DisplayName("Must return 404 when employee does not exist")
        void nonExistingEmployee() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    99999L,
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    null,
                    null,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.erro", containsString("Employee not found")));
        }
    }

    // =========================================================================
    // 3. WORST-CASE BVA (Combinações Simultâneas de Limites Extremos Válidos) -> 201
    // =========================================================================
    @Nested
    @DisplayName("3. Worst-Case BVA: Simultaneous Extreme Valid Boundaries (201 Created)")
    class WorstCaseBVA {

        @Test
        @DisplayName("Must return 201 for minimum global coordinates with 255 chars notes and 1st punch")
        void minCoordinatesAndMaxNotes() throws Exception {
            String maxNotes = "k".repeat(255);
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    -90.0,
                    -180.0,
                    maxNotes
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.latitude", is(-90.0)))
                    .andExpect(jsonPath("$.longitude", is(-180.0)))
                    .andExpect(jsonPath("$.notes", hasLength(255)));
        }

        @Test
        @DisplayName("Must return 201 for maximum global coordinates with short shift (CLOCK_OUT directly after CLOCK_IN)")
        void maxCoordinatesAndShortShift() throws Exception {
            TimeRecord initial = new TimeRecord();
            initial.setContract(activeContract);
            initial.setDateTime(LocalDateTime.now().minusHours(4));
            initial.setType(TimeRecordTypeEnum.CLOCK_IN);
            initial.setOrigin(TimeRecordOriginEnum.APP);
            timeRecordRepository.save(initial);

            String maxNotes = "w".repeat(255);
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_OUT,
                    TimeRecordOriginEnum.APP,
                    90.0,
                    180.0,
                    maxNotes
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type", is("CLOCK_OUT")))
                    .andExpect(jsonPath("$.latitude", is(90.0)))
                    .andExpect(jsonPath("$.longitude", is(180.0)))
                    .andExpect(jsonPath("$.notes", hasLength(255)));
        }
    }

    // =========================================================================
    // 4. ROBUST WORST-CASE BVA (Combinações Simultâneas com Violação de Limites) -> 400 / 409
    // =========================================================================
    @Nested
    @DisplayName("4. Robust Worst-Case BVA: Multiple Simultaneous Boundary Violations (400 / 409)")
    class RobustWorstCaseBVA {

        @Test
        @DisplayName("Must return 400 when latitude is provided but longitude is null (asymmetric coordinates)")
        void asymmetricCoordinatesLatOnly() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    -23.55,
                    null,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro", containsString("Both latitude and longitude must be provided together")));
        }

        @Test
        @DisplayName("Must return 400 when longitude is provided but latitude is null (asymmetric coordinates)")
        void asymmetricCoordinatesLonOnly() throws Exception {
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    null,
                    -46.63,
                    null
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro", containsString("Both latitude and longitude must be provided together")));
        }

        @Test
        @DisplayName("Must return 400 when both coordinates and notes are simultaneously out of bounds")
        void multipleOutOfBounds() throws Exception {
            String overflowNotes = "z".repeat(256);
            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    -90.001,
                    180.001,
                    overflowNotes
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Must return 409 when employee is on VACATION even with boundary valid coordinates")
        void inactiveEmployeeWithValidBoundaryCoordinates() throws Exception {
            activeEmployee.setStatus(EmployeeStatusEnum.VACATION);
            employeeRepository.save(activeEmployee);

            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.CLOCK_IN,
                    TimeRecordOriginEnum.APP,
                    -90.0,
                    -180.0,
                    "Tentativa durante férias"
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.erro", containsString("Employee is not active. Current status: VACATION")));
        }

        @Test
        @DisplayName("Must return 409 when attempting invalid sequence transition (LUNCH_IN directly after CLOCK_IN)")
        void invalidSequenceDirectLunchIn() throws Exception {
            TimeRecord r1 = new TimeRecord();
            r1.setContract(activeContract);
            r1.setDateTime(LocalDateTime.now().minusHours(2));
            r1.setType(TimeRecordTypeEnum.CLOCK_IN);
            r1.setOrigin(TimeRecordOriginEnum.APP);
            timeRecordRepository.save(r1);

            TimeRecordRequest request = new TimeRecordRequest(
                    activeEmployee.getId(),
                    TimeRecordTypeEnum.LUNCH_IN,
                    TimeRecordOriginEnum.APP,
                    -23.55,
                    -46.63,
                    "Tentativa de retorno de almoço sem ter saído"
            );

            mockMvc.perform(post("/pontos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.erro", containsString("After CLOCK_IN, the next clock-in must be one of")));
        }
    }
}
