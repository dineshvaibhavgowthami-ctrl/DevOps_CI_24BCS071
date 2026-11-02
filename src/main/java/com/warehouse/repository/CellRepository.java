package com.warehouse.repository;

import com.warehouse.model.Cell;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CellRepository extends JpaRepository<Cell, Long> {
    List<Cell> findAllByOrderByIdAsc();
}
