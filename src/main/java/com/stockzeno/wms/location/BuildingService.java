package com.stockzeno.wms.location;

import com.stockzeno.wms.location.dto.BuildingRequest;
import com.stockzeno.wms.location.dto.BuildingResponse;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final WarehouseRepository warehouseRepository;

    public BuildingService(BuildingRepository buildingRepository, WarehouseRepository warehouseRepository) {
        this.buildingRepository = buildingRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> list(UUID warehouseId) {
        List<Building> buildings = warehouseId == null
                ? buildingRepository.findAll()
                : buildingRepository.findByWarehouseId(warehouseId);
        return buildings.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BuildingResponse get(UUID id) {
        return toResponse(resolveBuilding(id));
    }

    @Transactional
    public BuildingResponse create(BuildingRequest request) {
        Building building = new Building();
        building.setWarehouse(resolveWarehouse(request.getWarehouseId()));
        applyRequest(building, request);
        return toResponse(buildingRepository.save(building));
    }

    @Transactional
    public BuildingResponse update(UUID id, BuildingRequest request) {
        Building building = resolveBuilding(id);
        building.setWarehouse(resolveWarehouse(request.getWarehouseId()));
        applyRequest(building, request);
        return toResponse(buildingRepository.save(building));
    }

    @Transactional
    public void delete(UUID id) {
        buildingRepository.delete(Objects.requireNonNull(resolveBuilding(id), "building"));
    }

    private Building resolveBuilding(UUID id) {
        return buildingRepository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Building not found"));
    }

    private Warehouse resolveWarehouse(UUID id) {
        return warehouseRepository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found"));
    }

    private void applyRequest(Building building, BuildingRequest request) {
        building.setCode(request.getCode().trim());
        building.setName(request.getName().trim());
        if (request.getActive() != null) {
            building.setActive(request.getActive());
        }
    }

    private BuildingResponse toResponse(Building building) {
        return new BuildingResponse(
                building.getId(),
                building.getWarehouse().getId(),
                building.getCode(),
                building.getName(),
                building.isActive(),
                building.getCreatedAt(),
                building.getUpdatedAt()
        );
    }
}
