package com.igorcavalcanti.inventory_api.stockmovement.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StockMovementControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldCreateINMovement_return201() throws Exception {
        long productId = createProductAndGetId();

        String movementBody = """
        {
          "productId": %d,
          "type": "IN",
          "quantity": 5,
          "reason": "Reposição",
          "idempotencyKey": "test-key-1"
        }
        """.formatted(productId);

        mockMvc.perform(post("/stock-movement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movementBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value((int) productId))
                .andExpect(jsonPath("$.type").value("IN"));
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
                        .contentType(MediaType.APPLICATION_JSON)
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
        long productId = createProductAndGetId();
        String outBody = """
    {
      "productId": %d,
      "type": "OUT",
      "quantity": 1,
      "reason": "Venda",
      "idempotencyKey": "test-key-out-1"
    }
    """.formatted(productId);

        mockMvc.perform(post("/stock-movement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(outBody))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldBeIdempotent_whenSameIdempotencyKeyIsReused() throws Exception {
        long productId = createProductAndGetId();

        String body = """
    {
      "productId": %d,
      "type": "IN",
      "quantity": 5,
      "reason": "Reposição",
      "idempotencyKey": "same-key-1"
    }
    """.formatted(productId);

        String first = mockMvc.perform(post("/stock-movement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String second = mockMvc.perform(post("/stock-movement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // garante que retornou o MESMO movimento (id igual)
        long id1 = extractLongField(first, "id");
        long id2 = extractLongField(second, "id");
        org.junit.jupiter.api.Assertions.assertEquals(id1, id2);
    }

    private long extractLongField(String json, String field) {
        String token = "\"" + field + "\":";
        int idx = json.indexOf(token);
        if (idx == -1) throw new IllegalStateException("Resposta nao contem campo " + field + ": " + json);
        int start = idx + token.length();
        int end = start;
        while (end < json.length() && Character.isWhitespace(json.charAt(end))) end++;
        int s2 = end;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return Long.parseLong(json.substring(s2, end));
    }


}