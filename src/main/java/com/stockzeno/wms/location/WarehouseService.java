package com.stockzeno.wms.location;

import com.stockzeno.wms.location.dto.WarehouseRequest;
import com.stockzeno.wms.location.dto.WarehouseResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> list() {
        return warehouseRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WarehouseResponse get(UUID id) {
        return toResponse(resolveWarehouse(id));
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        Warehouse warehouse = new Warehouse();
        applyRequest(warehouse, request);
        return toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseResponse update(UUID id, WarehouseRequest request) {
        Warehouse warehouse = resolveWarehouse(id);
        applyRequest(warehouse, request);
        return toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public void delete(UUID id) {
        warehouseRepository.delete(resolveWarehouse(id));
    }

    private Warehouse resolveWarehouse(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found"));
    }

    private void applyRequest(Warehouse warehouse, WarehouseRequest request) {
        warehouse.setCode(request.getCode().trim());
        warehouse.setName(request.getName().trim());
        if (request.getActive() != null) {
            warehouse.setActive(request.getActive());
        }
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.isActive(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt()
        );
    }
}
