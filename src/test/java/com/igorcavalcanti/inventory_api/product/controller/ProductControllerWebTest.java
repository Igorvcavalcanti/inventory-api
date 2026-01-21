package com.igorcavalcanti.inventory_api.product.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldCreateProduct_return201() throws Exception {

        String sku = "SKU-TEST-" + System.nanoTime();
        String body = """
        {
          "name": "Produto Teste",
          "sku": "SKU-TEST-001",
          "description": "Produto apenas para testes",
          "unitCost": 10.00,
          "unitPrice": 15.00
        }
        """.formatted(sku);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Produto Teste"))
                .andExpect(jsonPath("$.sku").value("SKU-TEST-001"))
                .andExpect(jsonPath("$.currentStock").value(0))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldGetProductById_return200() throws Exception {
        CreatedProduct created = createProductAndGetId();

        mockMvc.perform(get("/products/{id}", created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id()))
                .andExpect(jsonPath("$.sku").value(created.sku()));
    }

    @Test
    void shouldReturn404_whenProductNotFound() throws Exception {
        mockMvc.perform(get("/products/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateProduct_return200_andNotChangeStock() throws Exception {
        long id = createProductAndGetId().id();

        String updateBody = """
        {
          "name": "Produto Atualizado",
          "sku": "SKU-TEST-001",
          "description": "Nova desc",
          "unitCost": 11.00,
          "unitPrice": 16.00
        }
        """;

        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Produto Atualizado"))
                .andExpect(jsonPath("$.currentStock").value(0)); // regra do seu service
    }

    @Test
    void shouldDeactivate_return204_andNotAppearInOnlyActiveList() throws Exception {
        long id = createProductAndGetId().id();

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/products")
                        .param("onlyActive", "true")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id==" + id + ")]").doesNotExist());
    }


    private CreatedProduct createProductAndGetId() throws Exception {

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

        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();
        return new CreatedProduct(id, sku);
    }

    private record CreatedProduct(long id, String sku) {}
}
