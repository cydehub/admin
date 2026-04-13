package com.stockzeno.wms.location;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, UUID> {
    List<Building> findByWarehouseId(UUID warehouseId);

    Optional<Building> findByWarehouseIdAndCodeIgnoreCase(UUID warehouseId, String code);
}
