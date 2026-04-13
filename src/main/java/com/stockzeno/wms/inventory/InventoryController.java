package com.stockzeno.wms.inventory;

import com.stockzeno.wms.inventory.dto.AdjustmentRequest;
import com.stockzeno.wms.inventory.dto.AdjustmentResponse;
import com.stockzeno.wms.inventory.dto.FefoSuggestion;
import com.stockzeno.wms.inventory.dto.StockInRequest;
import com.stockzeno.wms.inventory.dto.StockInResponse;
import com.stockzeno.wms.inventory.dto.TransferRequest;
import com.stockzeno.wms.inventory.dto.TransferResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final PickingService pickingService;

    public InventoryController(InventoryService inventoryService, PickingService pickingService) {
        this.inventoryService = inventoryService;
        this.pickingService = pickingService;
    }

    @PostMapping("/stock-in")
    public ResponseEntity<StockInResponse> stockIn(@Valid @RequestBody StockInRequest request, Authentication authentication) {
        return ResponseEntity.ok(inventoryService.stockIn(request, authentication));
    }

    @PutMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request, Authentication authentication) {
        return ResponseEntity.ok(inventoryService.transfer(request, authentication));
    }

    @GetMapping("/fefo")
    public ResponseEntity<List<FefoSuggestion>> fefo(@RequestParam UUID productId) {
        return ResponseEntity.ok(pickingService.suggestFefo(productId));
    }

    @PostMapping("/adjustment")
    public ResponseEntity<AdjustmentResponse> adjustment(@Valid @RequestBody AdjustmentRequest request, Authentication authentication) {
        return ResponseEntity.ok(inventoryService.adjust(request, authentication));
    }
}
