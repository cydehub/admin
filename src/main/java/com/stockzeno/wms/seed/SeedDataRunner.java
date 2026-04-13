package com.stockzeno.wms.seed;

import com.stockzeno.wms.catalog.Product;
import com.stockzeno.wms.catalog.ProductRepository;
import com.stockzeno.wms.identity.Role;
import com.stockzeno.wms.identity.RoleName;
import com.stockzeno.wms.identity.RoleRepository;
import com.stockzeno.wms.identity.User;
import com.stockzeno.wms.identity.UserRepository;
import com.stockzeno.wms.inventory.Batch;
import com.stockzeno.wms.inventory.BatchRepository;
import com.stockzeno.wms.inventory.BatchStatus;
import com.stockzeno.wms.location.Aisle;
import com.stockzeno.wms.location.AisleRepository;
import com.stockzeno.wms.location.Bin;
import com.stockzeno.wms.location.BinRepository;
import com.stockzeno.wms.location.Building;
import com.stockzeno.wms.location.BuildingRepository;
import com.stockzeno.wms.location.Shelf;
import com.stockzeno.wms.location.ShelfRepository;
import com.stockzeno.wms.location.Warehouse;
import com.stockzeno.wms.location.WarehouseRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "stockzeno.seed.enabled", havingValue = "true")
public class SeedDataRunner implements CommandLineRunner {

    private final WarehouseRepository warehouseRepository;
    private final BuildingRepository buildingRepository;
    private final AisleRepository aisleRepository;
    private final ShelfRepository shelfRepository;
    private final BinRepository binRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataRunner(WarehouseRepository warehouseRepository,
                          BuildingRepository buildingRepository,
                          AisleRepository aisleRepository,
                          ShelfRepository shelfRepository,
                          BinRepository binRepository,
                          ProductRepository productRepository,
                          BatchRepository batchRepository,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.warehouseRepository = warehouseRepository;
        this.buildingRepository = buildingRepository;
        this.aisleRepository = aisleRepository;
        this.shelfRepository = shelfRepository;
        this.binRepository = binRepository;
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdminUser();
        Warehouse warehouse = seedWarehouse();
        Building building = seedBuilding(warehouse);
        Aisle aisle = seedAisle(building);
        Shelf shelf = seedShelf(aisle);
        seedBin(shelf);
        Product product = seedProduct();
        seedBatch(product);
    }

    private void seedAdminUser() {
        String email = "admin@stockzeno.local";
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElse(null);
        User user = new User();
        user.setEmail(email);
        user.setFirstName("Stockzeno");
        user.setLastName("Admin");
        user.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
        if (adminRole != null) {
            user.getRoles().add(adminRole);
        }
        userRepository.save(user);
    }

    private Warehouse seedWarehouse() {
        return warehouseRepository.findByCodeIgnoreCase("WH-001")
                .orElseGet(() -> {
                    Warehouse warehouse = new Warehouse();
                    warehouse.setCode("WH-001");
                    warehouse.setName("Main Warehouse");
                    return warehouseRepository.save(warehouse);
                });
    }

    private Building seedBuilding(Warehouse warehouse) {
        return buildingRepository.findByWarehouseIdAndCodeIgnoreCase(warehouse.getId(), "B-01")
                .orElseGet(() -> {
                    Building building = new Building();
                    building.setWarehouse(warehouse);
                    building.setCode("B-01");
                    building.setName("Primary Building");
                    return buildingRepository.save(building);
                });
    }

    private Aisle seedAisle(Building building) {
        return aisleRepository.findByBuildingIdAndCodeIgnoreCase(building.getId(), "A-01")
                .orElseGet(() -> {
                    Aisle aisle = new Aisle();
                    aisle.setBuilding(building);
                    aisle.setCode("A-01");
                    aisle.setName("Aisle 1");
                    return aisleRepository.save(aisle);
                });
    }

    private Shelf seedShelf(Aisle aisle) {
        return shelfRepository.findByAisleIdAndCodeIgnoreCase(aisle.getId(), "S-01")
                .orElseGet(() -> {
                    Shelf shelf = new Shelf();
                    shelf.setAisle(aisle);
                    shelf.setCode("S-01");
                    shelf.setName("Shelf 1");
                    return shelfRepository.save(shelf);
                });
    }

    private Bin seedBin(Shelf shelf) {
        return binRepository.findByShelfIdAndCodeIgnoreCase(shelf.getId(), "BIN-01")
                .orElseGet(() -> {
                    Bin bin = new Bin();
                    bin.setShelf(shelf);
                    bin.setCode("BIN-01");
                    bin.setLabel("Default Bin");
                    bin.setBarcode("BIN-01");
                    return binRepository.save(bin);
                });
    }

    private Product seedProduct() {
        return productRepository.findBySkuIgnoreCase("SKU-1000")
                .orElseGet(() -> {
                    Product product = new Product();
                    product.setSku("SKU-1000");
                    product.setName("Demo Widget");
                    product.setDescription("Sample product for quick testing.");
                    product.setUnitOfMeasure("EA");
                    return productRepository.save(product);
                });
    }

    private void seedBatch(Product product) {
        Optional<Batch> existing = batchRepository.findByProductIdAndBatchCode(product.getId(), "LOT-2024-001");
        if (existing.isPresent()) {
            return;
        }
        Batch batch = new Batch();
        batch.setProduct(product);
        batch.setBatchCode("LOT-2024-001");
        batch.setSupplierReference("SUP-REF-01");
        batch.setManufactureDate(LocalDate.now().minusMonths(1));
        batch.setExpiryDate(LocalDate.now().plusMonths(10));
        batch.setStatus(BatchStatus.ACTIVE);
        batchRepository.save(batch);
    }
}
