package com.stockzeno.wms.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockzeno.wms.catalog.Product;
import com.stockzeno.wms.catalog.ProductRepository;
import com.stockzeno.wms.inventory.dto.BatchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAndListBatches() throws Exception {
        Product product = new Product();
        product.setSku("SKU-BATCH");
        product.setName("Batch Product");
        product = productRepository.save(product);

        BatchRequest request = new BatchRequest();
        request.setProductId(product.getId());
        request.setBatchCode("LOT-01");
        request.setSupplierReference("SUP-1");
        request.setManufactureDate(LocalDate.now().minusDays(1));
        request.setExpiryDate(LocalDate.now().plusDays(30));

        mockMvc.perform(post("/inventory/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/inventory/batches")
                        .param("productId", product.getId().toString()))
                .andExpect(status().isOk());
    }
}
