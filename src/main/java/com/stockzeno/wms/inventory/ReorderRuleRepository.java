package com.stockzeno.wms.inventory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReorderRuleRepository extends JpaRepository<ReorderRule, UUID> {
    List<ReorderRule> findByEnabledTrue();
}
