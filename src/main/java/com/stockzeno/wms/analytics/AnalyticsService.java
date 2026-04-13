package com.stockzeno.wms.analytics;

import com.stockzeno.wms.analytics.dto.ReorderSuggestionResponse;
import com.stockzeno.wms.analytics.dto.StockVelocityResponse;
import com.stockzeno.wms.catalog.ProductRepository;
import com.stockzeno.wms.inventory.InventoryBalanceRepository;
import com.stockzeno.wms.inventory.MovementType;
import com.stockzeno.wms.inventory.ReorderRule;
import com.stockzeno.wms.inventory.ReorderRuleRepository;
import com.stockzeno.wms.inventory.StockMovement;
import com.stockzeno.wms.inventory.StockMovementRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AnalyticsService {

    private final StockMovementRepository movementRepository;
    private final ReorderRuleRepository reorderRuleRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final ProductRepository productRepository;

    public AnalyticsService(StockMovementRepository movementRepository,
                            ReorderRuleRepository reorderRuleRepository,
                            InventoryBalanceRepository balanceRepository,
                            ProductRepository productRepository) {
        this.movementRepository = movementRepository;
        this.reorderRuleRepository = reorderRuleRepository;
        this.balanceRepository = balanceRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public StockVelocityResponse getVelocity(UUID productId, int days) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        Instant to = Instant.now();
        Instant from = to.minus(days, ChronoUnit.DAYS);
        List<StockMovement> movements = movementRepository.findRecentMovements(productId, from);

        BigDecimal inbound = BigDecimal.ZERO;
        BigDecimal outbound = BigDecimal.ZERO;

        for (StockMovement movement : movements) {
            MovementType type = movement.getMovementType();
            if (type == MovementType.TRANSFER) {
                continue;
            }
            BigDecimal quantity = movement.getQuantity();
            if (quantity == null) {
                continue;
            }

            if (type == MovementType.PICK) {
                outbound = outbound.add(quantity.abs());
                continue;
            }
            if (type == MovementType.RETURN) {
                inbound = inbound.add(quantity.abs());
                continue;
            }

            if (quantity.compareTo(BigDecimal.ZERO) >= 0) {
                inbound = inbound.add(quantity);
            } else {
                outbound = outbound.add(quantity.abs());
            }
        }

        BigDecimal net = inbound.subtract(outbound);
        BigDecimal averageDailyOutbound = days > 0
                ? outbound.divide(BigDecimal.valueOf(days), 3, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new StockVelocityResponse(productId, from, to, inbound, outbound, net, averageDailyOutbound);
    }

    @Transactional(readOnly = true)
    public List<ReorderSuggestionResponse> getReorderSuggestions() {
        return reorderRuleRepository.findByEnabledTrue().stream()
                .map(this::buildSuggestion)
                .collect(Collectors.toList());
    }

    private ReorderSuggestionResponse buildSuggestion(ReorderRule rule) {
        BigDecimal available = balanceRepository.sumAvailableQuantity(rule.getProduct().getId());
        boolean shouldReorder = available.compareTo(rule.getReorderPoint()) <= 0;
        return new ReorderSuggestionResponse(
                rule.getProduct().getId(),
                rule.getProduct().getSku(),
                available,
                rule.getReorderPoint(),
                rule.getReorderQuantity(),
                shouldReorder
        );
    }
}
