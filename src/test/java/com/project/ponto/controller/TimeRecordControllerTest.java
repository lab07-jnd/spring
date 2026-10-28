package com.project.ponto.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import com.project.ponto.domain.enums.TimeRecordOriginEnum;
import com.project.ponto.domain.enums.TimeRecordTypeEnum;
import com.project.ponto.dto.TimeRecordResponse;
import com.project.ponto.exception.ResourceNotFoundException;
import com.project.ponto.service.TimeRecordService;

@WebMvcTest(TimeRecordController.class)
@DisplayName("TimeRecordController - GET /funcionarios/{funcionarioId}/pontos")
class TimeRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeRecordService service;

    @Test
    @DisplayName("Must return 200 with the records, passing the converted parameters to the service")
    void mustReturnRecords() throws Exception {
        // Stubbed only for these exact arguments: a wrong conversion gets the mock's empty list
        when(service.listByEmployee(7L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 1, 10))
                .thenReturn(List.of(new TimeRecordResponse(
                        15L, 7L, "Ana Souza", 3L, LocalDateTime.of(2026, 10, 10, 8, 0), TimeRecordTypeEnum.CLOCK_IN,
                        TimeRecordOriginEnum.WEB, -23.55, -46.63, "Entrada", LocalDateTime.of(2026, 10, 10, 8, 0, 5), 4L)));

        mockMvc.perform(get("/funcionarios/7/pontos")
                        .param("dataInicio", "2026-10-01")
                        .param("dataFim", "2026-10-31")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{
                          "id": 15,
                          "employeeId": 7,
                          "employeeName": "Ana Souza",
                          "contractId": 3,
                          "dateTime": "2026-10-10T08:00:00",
                          "type": "CLOCK_IN",
                          "origin": "WEB",
                          "latitude": -23.55,
                          "longitude": -46.63,
                          "notes": "Entrada",
                          "createdAt": "2026-10-10T08:00:05",
                          "justificationId": 4
                        }]
                        """, JsonCompareMode.STRICT));
    }

    @Test
    @DisplayName("Must accept a request without any query parameter")
    void mustAcceptRequestWithoutParameters() throws Exception {
        mockMvc.perform(get("/funcionarios/7/pontos"))
                .andExpect(status().isOk());

        verify(service).listByEmployee(7L, null, null, null, null);
    }

    @Test
    @DisplayName("Must answer 404 with ErrorResponse when the employee does not exist")
    void mustAnswerNotFoundForUnknownEmployee() throws Exception {
        when(service.listByEmployee(99L, null, null, null, null))
                .thenThrow(new ResourceNotFoundException("Funcionário não encontrado com id: 99"));

        mockMvc.perform(get("/funcionarios/99/pontos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Funcionário não encontrado com id: 99"));
    }

    @Test
    @DisplayName("Must answer 400 with ErrorResponse when the service rejects the parameters")
    void mustAnswerBadRequestForRejectedParameters() throws Exception {
        when(service.listByEmployee(7L, LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 10), null, null))
                .thenThrow(new IllegalArgumentException("dataInicio não pode ser posterior a dataFim"));

        mockMvc.perform(get("/funcionarios/7/pontos")
                        .param("dataInicio", "2026-10-11")
                        .param("dataFim", "2026-10-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("dataInicio não pode ser posterior a dataFim"));
    }

    // 10/1/26 is the short date style of the request locale (MockMvc defaults to English):
    // only ISO yyyy-MM-dd is part of the contract, whatever the client's Accept-Language
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "/funcionarios/7/pontos?dataInicio=abc&dataFim=2026-10-31, dataInicio",
            "/funcionarios/7/pontos?dataInicio=10/1/26&dataFim=10/31/26, dataInicio",
            "/funcionarios/7/pontos?page=x, page",
            "/funcionarios/abc/pontos, funcionarioId"})
    @DisplayName("Must answer 400 with ErrorResponse naming the parameter when a value has the wrong type")
    void mustAnswerBadRequestForWrongType(String url, String parameter) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Valor inválido para o parâmetro '" + parameter + "'"));
    }
}
