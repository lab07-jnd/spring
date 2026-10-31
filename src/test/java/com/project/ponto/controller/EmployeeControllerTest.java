package com.project.ponto.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.ponto.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController - errors shared with the time record endpoint")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService service;

    // The MethodArgumentTypeMismatchException handler is global, so GET /funcionarios
    // answers with the same ErrorResponse as GET /funcionarios/{funcionarioId}/pontos
    @Test
    @DisplayName("Must answer 400 with ErrorResponse naming the parameter when page is not a number")
    void mustAnswerBadRequestForNonNumericPage() throws Exception {
        mockMvc.perform(get("/funcionarios").param("page", "x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valor inválido para o parâmetro 'page'"));
    }
}
