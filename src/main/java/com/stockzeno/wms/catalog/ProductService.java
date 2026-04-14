package com.stockzeno.wms.catalog;

import com.stockzeno.wms.catalog.dto.ProductRequest;
import com.stockzeno.wms.catalog.dto.ProductResponse;
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
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse get(@NonNull UUID id) {
        return toResponse(resolveProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        return toResponse(Objects.requireNonNull(productRepository.save(product), "product"));
    }

    @Transactional
    public ProductResponse update(@NonNull UUID id, ProductRequest request) {
        Product product = resolveProduct(id);
        applyRequest(Objects.requireNonNull(product, "product"), request);
        return toResponse(Objects.requireNonNull(productRepository.save(product), "product"));
    }

    @Transactional
    public void delete(@NonNull UUID id) {
        productRepository.delete(Objects.requireNonNull(resolveProduct(id), "product"));
    }

    private @NonNull Product resolveProduct(@NonNull UUID id) {
        return Objects.requireNonNull(productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")), "product");
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setSku(request.getSku().trim());
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setUnitOfMeasure(request.getUnitOfMeasure());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getUnitOfMeasure(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
