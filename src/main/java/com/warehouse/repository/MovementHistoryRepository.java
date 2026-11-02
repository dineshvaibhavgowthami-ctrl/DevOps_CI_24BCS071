package com.warehouse.repository;

import com.warehouse.model.MovementHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementHistoryRepository extends JpaRepository<MovementHistory, Long> {
    List<MovementHistory> findTop10ByOrderByMovementDateDesc();
    List<MovementHistory> findByItemBarcodeOrderByMovementDateDesc(String barcode);
}
