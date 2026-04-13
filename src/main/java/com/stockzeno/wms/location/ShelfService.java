package com.stockzeno.wms.location;

import com.stockzeno.wms.location.dto.ShelfRequest;
import com.stockzeno.wms.location.dto.ShelfResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShelfService {

    private final ShelfRepository shelfRepository;
    private final AisleRepository aisleRepository;

    public ShelfService(ShelfRepository shelfRepository, AisleRepository aisleRepository) {
        this.shelfRepository = shelfRepository;
        this.aisleRepository = aisleRepository;
    }

    @Transactional(readOnly = true)
    public List<ShelfResponse> list(UUID aisleId) {
        List<Shelf> shelves = aisleId == null ? shelfRepository.findAll() : shelfRepository.findByAisleId(aisleId);
        return shelves.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ShelfResponse get(UUID id) {
        return toResponse(resolveShelf(id));
    }

    @Transactional
    public ShelfResponse create(ShelfRequest request) {
        Shelf shelf = new Shelf();
        shelf.setAisle(resolveAisle(request.getAisleId()));
        applyRequest(shelf, request);
        return toResponse(shelfRepository.save(shelf));
    }

    @Transactional
    public ShelfResponse update(UUID id, ShelfRequest request) {
        Shelf shelf = resolveShelf(id);
        shelf.setAisle(resolveAisle(request.getAisleId()));
        applyRequest(shelf, request);
        return toResponse(shelfRepository.save(shelf));
    }

    @Transactional
    public void delete(UUID id) {
        shelfRepository.delete(resolveShelf(id));
    }

    private Shelf resolveShelf(UUID id) {
        return shelfRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));
    }

    private Aisle resolveAisle(UUID id) {
        return aisleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aisle not found"));
    }

    private void applyRequest(Shelf shelf, ShelfRequest request) {
        shelf.setCode(request.getCode().trim());
        shelf.setName(request.getName().trim());
        if (request.getActive() != null) {
            shelf.setActive(request.getActive());
        }
    }

    private ShelfResponse toResponse(Shelf shelf) {
        return new ShelfResponse(
                shelf.getId(),
                shelf.getAisle().getId(),
                shelf.getCode(),
                shelf.getName(),
                shelf.isActive(),
                shelf.getCreatedAt(),
                shelf.getUpdatedAt()
        );
    }
}
