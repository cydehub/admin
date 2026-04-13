package com.stockzeno.wms.analytics;

import com.stockzeno.wms.analytics.dto.ReorderSuggestionResponse;
import com.stockzeno.wms.analytics.dto.StockVelocityResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/velocity")
    public ResponseEntity<StockVelocityResponse> velocity(@RequestParam UUID productId,
                                                          @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getVelocity(productId, days));
    }

    @GetMapping("/reorder-suggestions")
    public ResponseEntity<List<ReorderSuggestionResponse>> reorderSuggestions() {
        return ResponseEntity.ok(analyticsService.getReorderSuggestions());
    }
}
