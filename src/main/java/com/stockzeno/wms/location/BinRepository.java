package com.stockzeno.wms.location;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinRepository extends JpaRepository<Bin, UUID> {
    List<Bin> findByShelfId(UUID shelfId);

    Optional<Bin> findByShelfIdAndCodeIgnoreCase(UUID shelfId, String code);
}
