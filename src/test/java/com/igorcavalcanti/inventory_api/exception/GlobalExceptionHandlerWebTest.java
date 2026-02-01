package com.igorcavalcanti.inventory_api.exception;

import com.igorcavalcanti.inventory_api.product.service.ProductNotFoundException;
import com.igorcavalcanti.inventory_api.stockmovement.service.InsufficientStockException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GlobalExceptionHandlerWebTest.TestApp.class)
@AutoConfigureMockMvc
class GlobalExceptionHandlerWebTest {

    @Autowired
    MockMvc mockMvc;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({ GlobalExceptionHandler.class, TestController.class })
    static class TestApp { }

    @RestController
    static class TestController {

        @GetMapping("/test/product-not-found")
        void productNotFound() {
            throw new ProductNotFoundException(123L);
        }

        @GetMapping("/test/illegal-arg")
        void illegalArg() {
            throw new IllegalArgumentException("bad request reason");
        }

        @GetMapping("/test/insufficient-stock")
        void insufficientStock() {
            throw new InsufficientStockException(10L, 0, 1);
        }

        @GetMapping("/test/no-resource")
        void noResource() throws NoResourceFoundException {
            throw new NoResourceFoundException(HttpMethod.GET, "/nope", "No static resource /nope.");
        }

        @GetMapping("/test/optimistic-lock")
        void optimisticLock() {
            throw new ObjectOptimisticLockingFailureException(Object.class, 1L);
        }

        @GetMapping("/test/generic")
        void generic() {
            throw new RuntimeException("boom");
        }

        @PostMapping("/test/validation")
        void validation(@Valid @RequestBody ValidationPayload payload) {
            // nunca chega aqui se invalidar
        }
    }

    static class ValidationPayload {
        @NotBlank(message = "name must not be blank")
        public String name;

        public ValidationPayload() {}
        public ValidationPayload(String name) { this.name = name; }
    }

    @Test
    void shouldReturn404_forProductNotFound() throws Exception {
        mockMvc.perform(get("/test/product-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/test/product-not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400_forIllegalArgument() throws Exception {
        mockMvc.perform(get("/test/illegal-arg"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("bad request reason"))
                .andExpect(jsonPath("$.path").value("/test/illegal-arg"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn409_forInsufficientStock() throws Exception {
        mockMvc.perform(get("/test/insufficient-stock"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/test/insufficient-stock"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn404_forNoResourceFound() throws Exception {
        mockMvc.perform(get("/test/no-resource"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/test/no-resource"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn409_forOptimisticLock() throws Exception {
        mockMvc.perform(get("/test/optimistic-lock"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Concurrent update detected. Please retry the request."))
                .andExpect(jsonPath("$.path").value("/test/optimistic-lock"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn500_forGenericException() throws Exception {
        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Unexpected error occurred."))
                .andExpect(jsonPath("$.path").value("/test/generic"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400_forValidation() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].message").exists());
    }
}
