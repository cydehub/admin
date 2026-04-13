package com.stockzeno.wms.audit;

import com.stockzeno.wms.audit.dto.AdjustmentAuditResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AdjustmentAuditResponse> listAdjustments(Instant from, Instant to, int limit) {
        Instant resolvedFrom = from == null ? Instant.EPOCH : from;
        Instant resolvedTo = to == null ? Instant.now() : to;
        int cappedLimit = Math.min(Math.max(limit, 1), 500);

        return auditLogRepository.findByActionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
                        AuditAction.ADJUSTMENT,
                        resolvedFrom,
                        resolvedTo,
                        PageRequest.of(0, cappedLimit))
                .stream()
                .map(this::toAdjustmentResponse)
                .collect(Collectors.toList());
    }

    private AdjustmentAuditResponse toAdjustmentResponse(AuditLog log) {
        UUID userId = log.getUser() == null ? null : log.getUser().getId();
        return new AdjustmentAuditResponse(
                log.getId(),
                log.getEntityId(),
                log.getQuantityDelta(),
                log.getReasonCode(),
                log.getCreatedAt(),
                userId
        );
    }
}
