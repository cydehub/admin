package com.stockzeno.wms.audit;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    boolean existsByActionTypeAndEntityTypeAndEntityIdAndCreatedAtAfter(
            AuditAction actionType,
            AuditEntityType entityType,
            UUID entityId,
            Instant createdAt
    );

    List<AuditLog> findByActionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            AuditAction actionType,
            Instant from,
            Instant to,
            Pageable pageable
    );
}
