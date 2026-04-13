package com.stockzeno.wms.location;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AisleRepository extends JpaRepository<Aisle, UUID> {
    List<Aisle> findByBuildingId(UUID buildingId);

    Optional<Aisle> findByBuildingIdAndCodeIgnoreCase(UUID buildingId, String code);
}
