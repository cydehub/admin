package com.stockzeno.wms.location;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelfRepository extends JpaRepository<Shelf, UUID> {
    List<Shelf> findByAisleId(UUID aisleId);

    Optional<Shelf> findByAisleIdAndCodeIgnoreCase(UUID aisleId, String code);
}
