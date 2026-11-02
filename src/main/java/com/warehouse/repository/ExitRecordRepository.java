package com.warehouse.repository;

import com.warehouse.model.ExitRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExitRecordRepository extends JpaRepository<ExitRecord, Long> {
    List<ExitRecord> findTop10ByAuthorizedFalseOrderByVerificationDateDesc();
}
