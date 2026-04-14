package com.stockzeno.wms.inventory;

import com.stockzeno.wms.audit.AuditAction;
import com.stockzeno.wms.audit.AuditEntityType;
import com.stockzeno.wms.audit.AuditLog;
import com.stockzeno.wms.audit.AuditLogRepository;
import com.stockzeno.wms.jobs.JobProperties;
import com.stockzeno.wms.notification.EmailNotificationRequest;
import com.stockzeno.wms.notification.NotificationProperties;
import com.stockzeno.wms.notification.NotificationService;
import com.stockzeno.wms.notification.SmsNotificationRequest;
import com.stockzeno.wms.webhook.WebhookEventType;
import com.stockzeno.wms.webhook.WebhookService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryMonitoringService {

    private final BatchRepository batchRepository;
    private final ReorderRuleRepository reorderRuleRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final AuditLogRepository auditLogRepository;
    private final JobProperties jobProperties;
    private final WebhookService webhookService;
    private final NotificationService notificationService;
    private final NotificationProperties notificationProperties;

    public InventoryMonitoringService(BatchRepository batchRepository,
                                      ReorderRuleRepository reorderRuleRepository,
                                      InventoryBalanceRepository balanceRepository,
                                      AuditLogRepository auditLogRepository,
                                      JobProperties jobProperties,
                                      WebhookService webhookService,
                                      NotificationService notificationService,
                                      NotificationProperties notificationProperties) {
        this.batchRepository = batchRepository;
        this.reorderRuleRepository = reorderRuleRepository;
        this.balanceRepository = balanceRepository;
        this.auditLogRepository = auditLogRepository;
        this.jobProperties = jobProperties;
        this.webhookService = webhookService;
        this.notificationService = notificationService;
        this.notificationProperties = notificationProperties;
    }

    @Scheduled(cron = "${stockzeno.jobs.expiry-cron:0 0 * * * *}")
    @Transactional
    public void updateExpiryStatuses() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(jobProperties.getNearExpiryDays());

        List<Batch> expired = batchRepository.findByExpiryDateLessThanEqual(today);
        for (Batch batch : expired) {
            if (batch.getStatus() != BatchStatus.EXPIRED) {
                batch.setStatus(BatchStatus.EXPIRED);
                auditLogRepository.save(buildAudit(AuditAction.BATCH_EXPIRED, AuditEntityType.BATCH, batch.getId(), null, "EXPIRED"));
                webhookService.dispatchEvent(
                        WebhookEventType.BATCH_EXPIRED,
                        "batch",
                        batch.getId(),
                        Map.of(
                                "productId", batch.getProduct().getId(),
                                "expiryDate", batch.getExpiryDate()
                        )
                );
                notifyExpiry(batch, true);
            }
        }

        List<Batch> nearExpiry = batchRepository.findByExpiryDateBetweenAndStatusNot(today, threshold, BatchStatus.EXPIRED);
        for (Batch batch : nearExpiry) {
            if (batch.getStatus() != BatchStatus.NEAR_EXPIRY) {
                batch.setStatus(BatchStatus.NEAR_EXPIRY);
                auditLogRepository.save(buildAudit(AuditAction.BATCH_NEAR_EXPIRY, AuditEntityType.BATCH, batch.getId(), null, "NEAR_EXPIRY"));
                webhookService.dispatchEvent(
                        WebhookEventType.BATCH_NEAR_EXPIRY,
                        "batch",
                        batch.getId(),
                        Map.of(
                                "productId", batch.getProduct().getId(),
                                "expiryDate", batch.getExpiryDate()
                        )
                );
                notifyExpiry(batch, false);
            }
        }

        List<Batch> noLongerNear = batchRepository.findByStatusAndExpiryDateAfter(BatchStatus.NEAR_EXPIRY, threshold);
        for (Batch batch : noLongerNear) {
            batch.setStatus(BatchStatus.ACTIVE);
        }
    }

    @Scheduled(cron = "${stockzeno.jobs.low-stock-cron:0 */30 * * * *}")
    @Transactional
    public void evaluateLowStock() {
        Instant cutoff = Instant.now().minus(jobProperties.getLowStockCooldown());
        List<ReorderRule> rules = reorderRuleRepository.findByEnabledTrue();
        for (ReorderRule rule : rules) {
            BigDecimal available = balanceRepository.sumAvailableQuantity(rule.getProduct().getId());
            if (available.compareTo(rule.getReorderPoint()) <= 0) {
                if (!auditLogRepository.existsByActionTypeAndEntityTypeAndEntityIdAndCreatedAtAfter(
                        AuditAction.LOW_STOCK,
                        AuditEntityType.PRODUCT,
                        rule.getProduct().getId(),
                        cutoff)) {
                    auditLogRepository.save(buildAudit(AuditAction.LOW_STOCK, AuditEntityType.PRODUCT, rule.getProduct().getId(), available, "LOW_STOCK"));
                    webhookService.dispatchEvent(
                            WebhookEventType.LOW_STOCK,
                            "product",
                            rule.getProduct().getId(),
                            Map.of(
                                    "availableQuantity", available,
                                    "reorderPoint", rule.getReorderPoint(),
                                    "reorderQuantity", rule.getReorderQuantity()
                            )
                    );
                    notifyLowStock(rule, available);
                }
            }
        }
    }

    private void notifyExpiry(Batch batch, boolean expired) {
        String productName = batch.getProduct().getName();
        String sku = batch.getProduct().getSku();
        String batchCode = batch.getBatchCode();
        String expiryDate = String.valueOf(batch.getExpiryDate());
        String subject = expired
                ? "Batch expired: " + batchCode
                : "Batch near expiry: " + batchCode;
        String body = String.format(
                "Product: %s (%s)%nBatch: %s%nExpiry Date: %s%nStatus: %s",
                productName,
                sku,
                batchCode,
                expiryDate,
                expired ? "EXPIRED" : "NEAR_EXPIRY"
        );
        String smsMessage = String.format(
                "%s: %s (%s) exp %s",
                expired ? "BATCH EXPIRED" : "BATCH NEAR EXPIRY",
                batchCode,
                sku,
                expiryDate
        );
        sendNotifications(subject, body, smsMessage);
    }

    private void notifyLowStock(ReorderRule rule, BigDecimal available) {
        String productName = rule.getProduct().getName();
        String sku = rule.getProduct().getSku();
        String subject = "Low stock alert: " + productName;
        String body = String.format(
                "Product: %s (%s)%nAvailable: %s%nReorder Point: %s%nReorder Quantity: %s",
                productName,
                sku,
                available.toPlainString(),
                rule.getReorderPoint().toPlainString(),
                rule.getReorderQuantity().toPlainString()
        );
        String smsMessage = String.format(
                "LOW STOCK: %s (%s) avail %s rp %s",
                productName,
                sku,
                available.toPlainString(),
                rule.getReorderPoint().toPlainString()
        );
        sendNotifications(subject, body, smsMessage);
    }

    private void sendNotifications(String subject, String body, String smsMessage) {
        String emailTo = notificationProperties.getEmailTo();
        if (emailTo != null && !emailTo.isBlank()) {
            notificationService.sendEmail(new EmailNotificationRequest(emailTo, subject, body));
        }
        String smsTo = notificationProperties.getSmsTo();
        if (smsTo != null && !smsTo.isBlank()) {
            notificationService.sendSms(new SmsNotificationRequest(smsTo, smsMessage));
        }
    }

    private @NonNull AuditLog buildAudit(AuditAction action,
                                         AuditEntityType entityType,
                                         java.util.UUID entityId,
                                         BigDecimal quantityDelta,
                                         String reasonCode) {
        AuditLog log = new AuditLog();
        log.setActionType(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setQuantityDelta(quantityDelta);
        log.setReasonCode(reasonCode);
        return log;
    }
}
