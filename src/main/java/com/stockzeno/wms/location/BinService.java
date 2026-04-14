package com.stockzeno.wms.location;

import com.stockzeno.wms.location.dto.BinRequest;
import com.stockzeno.wms.location.dto.BinResponse;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BinService {

    private final BinRepository binRepository;
    private final ShelfRepository shelfRepository;

    public BinService(BinRepository binRepository, ShelfRepository shelfRepository) {
        this.binRepository = binRepository;
        this.shelfRepository = shelfRepository;
    }

    @Transactional(readOnly = true)
    public List<BinResponse> list(UUID shelfId) {
        List<Bin> bins = shelfId == null ? binRepository.findAll() : binRepository.findByShelfId(shelfId);
        return bins.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BinResponse get(UUID id) {
        return toResponse(resolveBin(id));
    }

    @Transactional
    public BinResponse create(BinRequest request) {
        Bin bin = new Bin();
        bin.setShelf(resolveShelf(request.getShelfId()));
        applyRequest(bin, request);
        return toResponse(binRepository.save(bin));
    }

    @Transactional
    public BinResponse update(UUID id, BinRequest request) {
        Bin bin = resolveBin(id);
        bin.setShelf(resolveShelf(request.getShelfId()));
        applyRequest(bin, request);
        return toResponse(binRepository.save(bin));
    }

    @Transactional
    public void delete(UUID id) {
        binRepository.delete(Objects.requireNonNull(resolveBin(id), "bin"));
    }

    private Bin resolveBin(UUID id) {
        return binRepository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bin not found"));
    }

    private Shelf resolveShelf(UUID id) {
        return shelfRepository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));
    }

    private void applyRequest(Bin bin, BinRequest request) {
        bin.setCode(request.getCode().trim());
        bin.setLabel(request.getLabel());
        bin.setBarcode(request.getBarcode());
        if (request.getActive() != null) {
            bin.setActive(request.getActive());
        }
    }

    private BinResponse toResponse(Bin bin) {
        return new BinResponse(
                bin.getId(),
                bin.getShelf().getId(),
                bin.getCode(),
                bin.getLabel(),
                bin.getBarcode(),
                bin.isActive(),
                bin.getCreatedAt(),
                bin.getUpdatedAt()
        );
    }
}
