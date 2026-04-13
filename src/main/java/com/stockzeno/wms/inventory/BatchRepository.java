package com.stockzeno.wms.inventory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRepository extends JpaRepository<Batch, UUID> {
    Optional<Batch> findByProductIdAndBatchCode(UUID productId, String batchCode);

    List<Batch> findByProductId(UUID productId);

    List<Batch> findByExpiryDateLessThanEqual(LocalDate date);

    List<Batch> findByExpiryDateBetweenAndStatusNot(LocalDate start, LocalDate end, BatchStatus status);

    List<Batch> findByStatusAndExpiryDateAfter(BatchStatus status, LocalDate date);
}
