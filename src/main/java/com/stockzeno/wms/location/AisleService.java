package com.stockzeno.wms.location;

import com.stockzeno.wms.location.dto.AisleRequest;
import com.stockzeno.wms.location.dto.AisleResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AisleService {

    private final AisleRepository aisleRepository;
    private final BuildingRepository buildingRepository;

    public AisleService(AisleRepository aisleRepository, BuildingRepository buildingRepository) {
        this.aisleRepository = aisleRepository;
        this.buildingRepository = buildingRepository;
    }

    @Transactional(readOnly = true)
    public List<AisleResponse> list(UUID buildingId) {
        List<Aisle> aisles = buildingId == null ? aisleRepository.findAll() : aisleRepository.findByBuildingId(buildingId);
        return aisles.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AisleResponse get(UUID id) {
        return toResponse(resolveAisle(id));
    }

    @Transactional
    public AisleResponse create(AisleRequest request) {
        Aisle aisle = new Aisle();
        aisle.setBuilding(resolveBuilding(request.getBuildingId()));
        applyRequest(aisle, request);
        return toResponse(aisleRepository.save(aisle));
    }

    @Transactional
    public AisleResponse update(UUID id, AisleRequest request) {
        Aisle aisle = resolveAisle(id);
        aisle.setBuilding(resolveBuilding(request.getBuildingId()));
        applyRequest(aisle, request);
        return toResponse(aisleRepository.save(aisle));
    }

    @Transactional
    public void delete(UUID id) {
        aisleRepository.delete(resolveAisle(id));
    }

    private Aisle resolveAisle(UUID id) {
        return aisleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aisle not found"));
    }

    private Building resolveBuilding(UUID id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Building not found"));
    }

    private void applyRequest(Aisle aisle, AisleRequest request) {
        aisle.setCode(request.getCode().trim());
        aisle.setName(request.getName().trim());
        if (request.getActive() != null) {
            aisle.setActive(request.getActive());
        }
    }

    private AisleResponse toResponse(Aisle aisle) {
        return new AisleResponse(
                aisle.getId(),
                aisle.getBuilding().getId(),
                aisle.getCode(),
                aisle.getName(),
                aisle.isActive(),
                aisle.getCreatedAt(),
                aisle.getUpdatedAt()
        );
    }
}
