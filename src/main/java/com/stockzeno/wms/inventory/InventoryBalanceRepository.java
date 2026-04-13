package com.stockzeno.wms.inventory;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, UUID> {
    Optional<InventoryBalance> findByProductIdAndBatchIdAndBinId(UUID productId, UUID batchId, UUID binId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from InventoryBalance b where b.product.id = :productId and b.batch.id = :batchId and b.bin.id = :binId")
    Optional<InventoryBalance> findForUpdate(@Param("productId") UUID productId,
                                             @Param("batchId") UUID batchId,
                                             @Param("binId") UUID binId);

    @Query("select coalesce(sum(b.quantityOnHand - b.reservedQuantity), 0) from InventoryBalance b where b.product.id = :productId")
    java.math.BigDecimal sumAvailableQuantity(@Param("productId") UUID productId);

    @Query("select b from InventoryBalance b join fetch b.batch batch where b.product.id = :productId order by batch.expiryDate asc")
    java.util.List<InventoryBalance> findByProductIdOrderByBatchExpiryDate(@Param("productId") UUID productId);
}
