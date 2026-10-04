package com.gastromind.infrastructure.web;

import com.gastromind.domain.exception.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ApiExceptionHandler debería")
class ApiExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FailingController())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("responder 400 cuando el dominio rechaza un dato")
    void shouldAnswerBadRequestOnDomainValidation() throws Exception {
        mockMvc.perform(get("/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Product name cannot be empty"));
    }

    @Test
    @DisplayName("no disfrazar de 400 un fallo de programación")
    void shouldNotAnswerBadRequestOnProgrammingErrors() {
        assertThatThrownBy(() -> mockMvc.perform(get("/bug")))
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage("Unexpected state");
    }

    //Anidado en un test: el escaneo de Spring Boot lo ignora (TestTypeExcludeFilter) y no llega a la aplicación
    @RestController
    static class FailingController {

        @GetMapping("/validation")
        void validation() {
            throw new DomainValidationException("Product name cannot be empty");
        }

        @GetMapping("/bug")
        void bug() {
            throw new IllegalArgumentException("Unexpected state");
        }
    }
}
