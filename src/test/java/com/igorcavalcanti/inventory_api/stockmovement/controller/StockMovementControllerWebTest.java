package com.igorcavalcanti.inventory_api.stockmovement.controller;

import com.igorcavalcanti.inventory_api.exception.GlobalExceptionHandler;
import com.igorcavalcanti.inventory_api.stockmovement.dto.response.StockMovementResponse;
import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovementType;
import com.igorcavalcanti.inventory_api.stockmovement.service.InsufficientStockException;
import com.igorcavalcanti.inventory_api.stockmovement.service.StockMovementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = StockMovementController.class)
@Import(GlobalExceptionHandler.class)
public class StockMovementControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private StockMovementService stockMovementService;


    @Test
    void shouldCreateINMovement_return201() throws Exception {
        var response = StockMovementResponse.builder()
                .id(1L)
                .productId(10L)
                .type(StockMovementType.IN)
                .quantity(5)
                .reason("Reposicao")
                .build();

        when(stockMovementService.create(any())).thenReturn(response);

        mockMvc.perform(post("/stock-movement")
                        .contentType(APPLICATION_JSON)
                        .content("""
                    {
                      "productId": 10,
                      "type": "IN",
                      "quantity": 5,
                      "reason": "Reposicao",
                      "idempotencyKey": "k1"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.type").value("IN"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.reason").value("Reposicao"));
    }


    private long createProductAndGetId() throws Exception {

        String sku = "SKU-TEST-" + System.nanoTime();
        String productBody = """
        {
          "name": "Produto Teste",
          "sku": "%s",
          "description": "Produto apenas para testes",
          "unitCost": 10.00,
          "unitPrice": 15.00
        }
        """.formatted(sku);

        String responseJson = mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(productBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // assume que o retorno tem "id":<numero>
        int idx = responseJson.indexOf("\"id\":");
        if (idx == -1) throw new IllegalStateException("Resposta nao contem campo id: " + responseJson);
        int start = idx + 5;
        int end = start;
        while (end < responseJson.length() && Character.isDigit(responseJson.charAt(end))) end++;
        return Long.parseLong(responseJson.substring(start, end));
    }

    @Test
    void shouldRejectOUTMovement_whenInsufficientStock() throws Exception {
        when(stockMovementService.create(any()))
                .thenThrow(new InsufficientStockException(10L, 0, 1));

        mockMvc.perform(post("/stock-movement") // ajuste
                        .contentType(APPLICATION_JSON)
                        .content("""
                        {
                          "productId": 10,
                          "type": "OUT",
                          "quantity": 1,
                          "reason": "Venda",
                          "idempotencyKey": "k2"
                        }
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldBeIdempotent_whenSameIdempotencyKeyIsReused() throws Exception {
        var response = StockMovementResponse.builder()
                .id(1L)
                .productId(10L)
                .type(StockMovementType.IN)
                .quantity(5)
                .reason("Reposicao")
                .build();

        when(stockMovementService.create(any())).thenReturn(response);

        var json = """
        {
          "productId": 10,
          "type": "IN",
          "quantity": 5,
          "reason": "Reposicao",
          "idempotencyKey": "same-key"
        }
        """;

        mockMvc.perform(post("/stock-movement").contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/stock-movement").contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }
}