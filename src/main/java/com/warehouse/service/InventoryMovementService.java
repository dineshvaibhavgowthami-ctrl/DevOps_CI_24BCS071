package com.warehouse.service;

import com.warehouse.exception.BusinessException;
import com.warehouse.exception.NotFoundException;
import com.warehouse.model.Cell;
import com.warehouse.model.ExitRecord;
import com.warehouse.model.InventoryItem;
import com.warehouse.model.MovementHistory;
import com.warehouse.repository.ExitRecordRepository;
import com.warehouse.repository.InventoryItemRepository;
import com.warehouse.repository.MovementHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryMovementService {
    private final BarcodeService barcodeService;
    private final LocationAllocationService allocationService;
    private final InventoryItemRepository itemRepository;
    private final MovementHistoryRepository movementRepository;
    private final ExitRecordRepository exitRepository;

    public InventoryMovementService(BarcodeService barcodeService, LocationAllocationService allocationService,
                                    InventoryItemRepository itemRepository, MovementHistoryRepository movementRepository,
                                    ExitRecordRepository exitRepository) {
        this.barcodeService = barcodeService;
        this.allocationService = allocationService;
        this.itemRepository = itemRepository;
        this.movementRepository = movementRepository;
        this.exitRepository = exitRepository;
    }

    @Transactional
    public MovementHistory moveItem(String barcode, Long destinationCellId) {
        InventoryItem item = barcodeService.findItemByBarcode(barcode);
        if (!"IN_STOCK".equals(item.getStatus()) || item.getCurrentCell() == null) {
            throw new BusinessException("Exited items cannot be moved.");
        }
        Cell destination = allocationService.findCellById(destinationCellId);
        Cell previous = item.getCurrentCell();
        if (previous.getId().equals(destination.getId())) {
            throw new BusinessException("Item is already in the selected cell.");
        }
        if (!allocationService.checkCapacity(destination, item.getQuantity())) {
            throw new BusinessException("Destination cell does not have enough remaining capacity.");
        }

        String previousLocation = previous.getFullLocation();
        String newLocation = destination.getFullLocation();
        previous.removeQuantity(item.getQuantity());
        destination.addQuantity(item.getQuantity());
        item.moveTo(destination);
        itemRepository.save(item);
        MovementHistory history = new MovementHistory(item, previousLocation, newLocation, LocalDateTime.now());
        return movementRepository.save(history);
    }

    public List<MovementHistory> getMovementHistory(String barcode) {
        barcodeService.findItemByBarcode(barcode);
        return movementRepository.findByItemBarcodeOrderByMovementDateDesc(barcode.trim());
    }

    @Transactional
    public ExitRecord verifyExit(String barcode, boolean authorized) {
        InventoryItem item = barcodeService.findItemByBarcode(barcode);
        if (!"IN_STOCK".equals(item.getStatus()) || item.getCurrentCell() == null) {
            throw new BusinessException("This item has already exited the warehouse.");
        }

        boolean accepted = authorized;
        ExitRecord record = exitRepository.save(new ExitRecord(item, authorized, accepted, LocalDateTime.now()));
        if (accepted) {
            item.getCurrentCell().removeQuantity(item.getQuantity());
            item.markExited();
            itemRepository.save(item);
        }
        return record;
    }
}
