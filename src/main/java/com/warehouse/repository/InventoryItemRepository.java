package com.warehouse.repository;

import com.warehouse.model.InventoryItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
    List<InventoryItem> findTop10ByOrderByIdDesc();
    long countByStatus(String status);
}
