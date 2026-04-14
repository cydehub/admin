package com.stockzeno.wms.inventory;

import com.stockzeno.wms.catalog.Product;
import com.stockzeno.wms.catalog.ProductRepository;
import com.stockzeno.wms.inventory.dto.BatchRequest;
import com.stockzeno.wms.inventory.dto.BatchResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BatchService {

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;

    public BatchService(BatchRepository batchRepository, ProductRepository productRepository) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> list(UUID productId) {
        List<Batch> batches = productId == null ? batchRepository.findAll() : batchRepository.findByProductId(productId);
        return batches.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BatchResponse get(@NonNull UUID id) {
        return toResponse(resolveBatch(id));
    }

    @Transactional
    public BatchResponse create(BatchRequest request) {
        Product product = resolveProduct(Objects.requireNonNull(request.getProductId(), "productId"));
        validateDates(request.getManufactureDate(), request.getExpiryDate());

        Batch batch = new Batch();
        batch.setProduct(product);
        applyRequest(batch, request);
        return toResponse(batchRepository.save(batch));
    }

    @Transactional
    public BatchResponse update(@NonNull UUID id, BatchRequest request) {
        Batch batch = resolveBatch(id);
        Product product = resolveProduct(Objects.requireNonNull(request.getProductId(), "productId"));
        validateDates(request.getManufactureDate(), request.getExpiryDate());

        batch.setProduct(product);
        applyRequest(batch, request);
        return toResponse(batchRepository.save(batch));
    }

    @Transactional
    public void delete(@NonNull UUID id) {
        batchRepository.delete(Objects.requireNonNull(resolveBatch(id), "batch"));
    }

    private Batch resolveBatch(@NonNull UUID id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found"));
    }

    private Product resolveProduct(@NonNull UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void validateDates(LocalDate manufactureDate, LocalDate expiryDate) {
        if (expiryDate.isBefore(manufactureDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date cannot be before manufacture date");
        }
    }

    private void applyRequest(Batch batch, BatchRequest request) {
        batch.setBatchCode(request.getBatchCode().trim());
        batch.setSupplierReference(request.getSupplierReference());
        batch.setManufactureDate(request.getManufactureDate());
        batch.setExpiryDate(request.getExpiryDate());
        if (request.getStatus() != null) {
            batch.setStatus(request.getStatus());
        }
    }

    private BatchResponse toResponse(Batch batch) {
        return new BatchResponse(
                batch.getId(),
                batch.getProduct().getId(),
                batch.getProduct().getSku(),
                batch.getBatchCode(),
                batch.getSupplierReference(),
                batch.getManufactureDate(),
                batch.getExpiryDate(),
                batch.getStatus(),
                batch.getCreatedAt(),
                batch.getUpdatedAt()
        );
    }
}
