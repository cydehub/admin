package com.stockzeno.wms.inventory;

import com.stockzeno.wms.inventory.dto.FefoSuggestion;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PickingService {

    private final InventoryBalanceRepository balanceRepository;

    public PickingService(InventoryBalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public List<FefoSuggestion> suggestFefo(UUID productId) {
        return balanceRepository.findByProductIdOrderByBatchExpiryDate(productId).stream()
                .filter(balance -> balance.getQuantityOnHand().subtract(balance.getReservedQuantity()).compareTo(BigDecimal.ZERO) > 0)
                .map(balance -> new FefoSuggestion(
                        balance.getBatch().getId(),
                        balance.getBatch().getBatchCode(),
                        balance.getBatch().getExpiryDate(),
                        balance.getQuantityOnHand().subtract(balance.getReservedQuantity())
                ))
                .collect(Collectors.toList());
    }
}
