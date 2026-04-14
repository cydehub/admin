package com.stockzeno.wms.inventory;

import com.stockzeno.wms.audit.AuditAction;
import com.stockzeno.wms.audit.AuditEntityType;
import com.stockzeno.wms.audit.AuditLog;
import com.stockzeno.wms.audit.AuditLogRepository;
import com.stockzeno.wms.catalog.Product;
import com.stockzeno.wms.catalog.ProductRepository;
import com.stockzeno.wms.identity.User;
import com.stockzeno.wms.identity.UserRepository;
import com.stockzeno.wms.inventory.dto.AdjustmentRequest;
import com.stockzeno.wms.inventory.dto.AdjustmentResponse;
import com.stockzeno.wms.inventory.dto.StockInRequest;
import com.stockzeno.wms.inventory.dto.StockInResponse;
import com.stockzeno.wms.inventory.dto.TransferRequest;
import com.stockzeno.wms.inventory.dto.TransferResponse;
import com.stockzeno.wms.location.Bin;
import com.stockzeno.wms.location.BinRepository;
import com.stockzeno.wms.webhook.WebhookEventType;
import com.stockzeno.wms.webhook.WebhookService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final BinRepository binRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final WebhookService webhookService;

    public InventoryService(ProductRepository productRepository,
                            BatchRepository batchRepository,
                            InventoryBalanceRepository balanceRepository,
                            StockMovementRepository movementRepository,
                            BinRepository binRepository,
                            AuditLogRepository auditLogRepository,
                            UserRepository userRepository,
                            WebhookService webhookService) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
        this.binRepository = binRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.webhookService = webhookService;
    }

    @Transactional
    public StockInResponse stockIn(StockInRequest request, Authentication authentication) {
        validateDates(request.getManufactureDate(), request.getExpiryDate());
        UUID productId = Objects.requireNonNull(request.getProductId(), "productId");
        UUID binId = Objects.requireNonNull(request.getBinId(), "binId");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        Bin bin = binRepository.findById(binId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bin not found"));

        Batch batch = batchRepository.findByProductIdAndBatchCode(product.getId(), request.getBatchCode())
                .orElse(null);
        if (batch == null) {
            batch = createBatch(request, product);
        } else if (!batch.getManufactureDate().equals(request.getManufactureDate())
                || !batch.getExpiryDate().equals(request.getExpiryDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Batch dates do not match existing record");
        }

        Batch finalBatch = batch;
        InventoryBalance balance = balanceRepository.findForUpdate(product.getId(), batch.getId(), bin.getId())
                .orElseGet(() -> createBalance(product, finalBatch, bin));

        BigDecimal newQuantity = balance.getQuantityOnHand().add(request.getQuantity());
        balance.setQuantityOnHand(newQuantity);
        balanceRepository.save(balance);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setBatch(batch);
        movement.setToBin(bin);
        movement.setMovementType(MovementType.STOCK_IN);
        movement.setQuantity(request.getQuantity());
        movement.setReasonCode(request.getReasonCode());
        movement.setPerformedBy(resolveUser(authentication));
        movementRepository.save(movement);

        recordAudit(AuditAction.STOCK_IN, AuditEntityType.INVENTORY_BALANCE, balance.getId(), request.getQuantity(), request.getReasonCode(), authentication);
        webhookService.dispatchEvent(
                WebhookEventType.STOCK_IN,
                "inventory-balance",
                balance.getId(),
                Map.of(
                        "productId", product.getId(),
                        "batchId", batch.getId(),
                        "binId", bin.getId(),
                        "quantity", request.getQuantity()
                )
        );

        return new StockInResponse(batch.getId(), balance.getId(), newQuantity);
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, Authentication authentication) {
        UUID productId = Objects.requireNonNull(request.getProductId(), "productId");
        UUID batchId = Objects.requireNonNull(request.getBatchId(), "batchId");
        UUID fromBinId = Objects.requireNonNull(request.getFromBinId(), "fromBinId");
        UUID toBinId = Objects.requireNonNull(request.getToBinId(), "toBinId");
        if (fromBinId.equals(toBinId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and destination bins must differ");
        }

        InventoryBalance sourceBalance = balanceRepository.findForUpdate(productId, batchId, fromBinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source inventory balance not found"));

        if (sourceBalance.getQuantityOnHand().compareTo(request.getQuantity()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient source quantity");
        }

        InventoryBalance destinationBalance = balanceRepository.findForUpdate(productId, batchId, toBinId)
                .orElseGet(() -> createBalance(sourceBalance.getProduct(), sourceBalance.getBatch(), resolveBin(toBinId)));

        sourceBalance.setQuantityOnHand(sourceBalance.getQuantityOnHand().subtract(request.getQuantity()));
        destinationBalance.setQuantityOnHand(destinationBalance.getQuantityOnHand().add(request.getQuantity()));
        balanceRepository.save(sourceBalance);
        balanceRepository.save(destinationBalance);

        StockMovement movement = new StockMovement();
        movement.setProduct(sourceBalance.getProduct());
        movement.setBatch(sourceBalance.getBatch());
        movement.setFromBin(sourceBalance.getBin());
        movement.setToBin(destinationBalance.getBin());
        movement.setMovementType(MovementType.TRANSFER);
        movement.setQuantity(request.getQuantity());
        movement.setReasonCode(request.getReasonCode());
        movement.setPerformedBy(resolveUser(authentication));
        movementRepository.save(movement);

        recordAudit(AuditAction.TRANSFER, AuditEntityType.INVENTORY_BALANCE, sourceBalance.getId(), request.getQuantity().negate(), request.getReasonCode(), authentication);
        recordAudit(AuditAction.TRANSFER, AuditEntityType.INVENTORY_BALANCE, destinationBalance.getId(), request.getQuantity(), request.getReasonCode(), authentication);
        webhookService.dispatchEvent(
                WebhookEventType.TRANSFER,
                "inventory-balance",
                sourceBalance.getId(),
                Map.of(
                        "productId", sourceBalance.getProduct().getId(),
                        "batchId", sourceBalance.getBatch().getId(),
                        "fromBinId", sourceBalance.getBin().getId(),
                        "toBinId", destinationBalance.getBin().getId(),
                        "quantity", request.getQuantity()
                )
        );

        return new TransferResponse(
                sourceBalance.getId(),
                destinationBalance.getId(),
                sourceBalance.getQuantityOnHand(),
                destinationBalance.getQuantityOnHand()
        );
    }

    @Transactional
    public AdjustmentResponse adjust(AdjustmentRequest request, Authentication authentication) {
        if (request.getQuantityDelta().compareTo(BigDecimal.ZERO) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adjustment quantity cannot be zero");
        }

        UUID productId = Objects.requireNonNull(request.getProductId(), "productId");
        UUID batchId = Objects.requireNonNull(request.getBatchId(), "batchId");
        UUID binId = Objects.requireNonNull(request.getBinId(), "binId");
        InventoryBalance balance = balanceRepository.findForUpdate(productId, batchId, binId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory balance not found"));

        BigDecimal newQuantity = balance.getQuantityOnHand().add(request.getQuantityDelta());
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adjustment would result in negative quantity");
        }

        balance.setQuantityOnHand(newQuantity);
        balanceRepository.save(balance);

        StockMovement movement = new StockMovement();
        movement.setProduct(balance.getProduct());
        movement.setBatch(balance.getBatch());
        movement.setToBin(balance.getBin());
        movement.setMovementType(MovementType.ADJUSTMENT);
        movement.setQuantity(request.getQuantityDelta());
        movement.setReasonCode(request.getReasonCode());
        movement.setPerformedBy(resolveUser(authentication));
        movementRepository.save(movement);

        recordAudit(AuditAction.ADJUSTMENT, AuditEntityType.INVENTORY_BALANCE, balance.getId(), request.getQuantityDelta(), request.getReasonCode(), authentication);
        webhookService.dispatchEvent(
                WebhookEventType.ADJUSTMENT,
                "inventory-balance",
                balance.getId(),
                Map.of(
                        "productId", balance.getProduct().getId(),
                        "batchId", balance.getBatch().getId(),
                        "binId", balance.getBin().getId(),
                        "quantityDelta", request.getQuantityDelta()
                )
        );

        return new AdjustmentResponse(balance.getId(), newQuantity);
    }

    private Batch createBatch(StockInRequest request, Product product) {
        Batch batch = new Batch();
        batch.setProduct(product);
        batch.setBatchCode(request.getBatchCode());
        batch.setSupplierReference(request.getSupplierReference());
        batch.setManufactureDate(request.getManufactureDate());
        batch.setExpiryDate(request.getExpiryDate());
        return batchRepository.save(batch);
    }

    private InventoryBalance createBalance(Product product, Batch batch, Bin bin) {
        InventoryBalance balance = new InventoryBalance();
        balance.setProduct(product);
        balance.setBatch(batch);
        balance.setBin(bin);
        return balanceRepository.save(balance);
    }

    private void validateDates(java.time.LocalDate manufactureDate, java.time.LocalDate expiryDate) {
        if (expiryDate.isBefore(manufactureDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date cannot be before manufacture date");
        }
    }

    private Bin resolveBin(@NonNull UUID binId) {
        return binRepository.findById(binId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bin not found"));
    }

    private User resolveUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName()).orElse(null);
    }

    private void recordAudit(AuditAction action,
                             AuditEntityType entityType,
                             UUID entityId,
                             BigDecimal quantityDelta,
                             String reasonCode,
                             Authentication authentication) {
        AuditLog log = new AuditLog();
        log.setActionType(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setQuantityDelta(quantityDelta);
        log.setReasonCode(reasonCode);
        log.setUser(resolveUser(authentication));
        auditLogRepository.save(log);
    }
}
