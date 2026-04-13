package com.stockzeno.wms.location;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockzeno.wms.location.dto.AisleRequest;
import com.stockzeno.wms.location.dto.BinRequest;
import com.stockzeno.wms.location.dto.BuildingRequest;
import com.stockzeno.wms.location.dto.ShelfRequest;
import com.stockzeno.wms.location.dto.WarehouseRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LocationHierarchyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createLocationHierarchy() throws Exception {
        WarehouseRequest warehouseRequest = new WarehouseRequest();
        warehouseRequest.setCode("WH-H1");
        warehouseRequest.setName("Hierarchy Warehouse");

        MvcResult warehouseResult = mockMvc.perform(post("/locations/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(warehouseRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode warehouseJson = objectMapper.readTree(warehouseResult.getResponse().getContentAsString());
        String warehouseId = warehouseJson.get("id").asText();

        BuildingRequest buildingRequest = new BuildingRequest();
        buildingRequest.setWarehouseId(java.util.UUID.fromString(warehouseId));
        buildingRequest.setCode("BLD-H1");
        buildingRequest.setName("Hierarchy Building");

        MvcResult buildingResult = mockMvc.perform(post("/locations/buildings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode buildingJson = objectMapper.readTree(buildingResult.getResponse().getContentAsString());
        String buildingId = buildingJson.get("id").asText();

        AisleRequest aisleRequest = new AisleRequest();
        aisleRequest.setBuildingId(java.util.UUID.fromString(buildingId));
        aisleRequest.setCode("AIS-H1");
        aisleRequest.setName("Hierarchy Aisle");

        MvcResult aisleResult = mockMvc.perform(post("/locations/aisles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aisleRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode aisleJson = objectMapper.readTree(aisleResult.getResponse().getContentAsString());
        String aisleId = aisleJson.get("id").asText();

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setAisleId(java.util.UUID.fromString(aisleId));
        shelfRequest.setCode("SH-H1");
        shelfRequest.setName("Hierarchy Shelf");

        MvcResult shelfResult = mockMvc.perform(post("/locations/shelves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shelfRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode shelfJson = objectMapper.readTree(shelfResult.getResponse().getContentAsString());
        String shelfId = shelfJson.get("id").asText();

        BinRequest binRequest = new BinRequest();
        binRequest.setShelfId(java.util.UUID.fromString(shelfId));
        binRequest.setCode("BIN-H1");
        binRequest.setLabel("Hierarchy Bin");
        binRequest.setBarcode("BIN-H1");

        mockMvc.perform(post("/locations/bins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(binRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/locations/bins")
                        .param("shelfId", shelfId))
                .andExpect(status().isOk());
    }
}
