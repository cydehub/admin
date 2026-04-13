package com.stockzeno.wms.inventory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    @Query("select sm from StockMovement sm where sm.product.id = :productId and sm.createdAt >= :from")
    List<StockMovement> findRecentMovements(@Param("productId") UUID productId, @Param("from") Instant from);
}
