package com.warehouse.service;

import com.warehouse.exception.BusinessException;
import com.warehouse.exception.NotFoundException;
import com.warehouse.model.Cell;
import com.warehouse.model.InventoryItem;
import com.warehouse.repository.CellRepository;
import com.warehouse.repository.InventoryItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationAllocationService {
    private final CellRepository cellRepository;
    private final InventoryItemRepository itemRepository;
    private final BarcodeService barcodeService;

    public LocationAllocationService(CellRepository cellRepository, InventoryItemRepository itemRepository,
                                     BarcodeService barcodeService) {
        this.cellRepository = cellRepository;
        this.itemRepository = itemRepository;
        this.barcodeService = barcodeService;
    }

    public Cell allocateLocation(int quantity) {
        validateQuantity(quantity);
        return cellRepository.findAllByOrderByIdAsc().stream()
                .filter(cell -> isLocationAvailable(cell, quantity))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No warehouse cell has enough remaining capacity."));
    }

    public boolean checkCapacity(Cell cell, int quantity) {
        return cell != null && quantity > 0 && cell.hasCapacity(quantity);
    }

    public boolean isLocationAvailable(Cell cell, int quantity) {
        return checkCapacity(cell, quantity);
    }

    @Transactional
    public InventoryItem addItem(String name, int quantity) {
        if (name == null || name.isBlank()) throw new BusinessException("Item name is required.");
        Cell cell = allocateLocation(quantity);
        String barcode = barcodeService.generateBarcode(cell);
        cell.addQuantity(quantity);
        cellRepository.save(cell);
        return itemRepository.save(new InventoryItem(name.trim(), quantity, barcode, cell));
    }

    public Cell findCellById(Long id) {
        return cellRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Warehouse cell not found: " + id));
    }

    public List<Cell> getCells() { return cellRepository.findAllByOrderByIdAsc(); }

    public void validateQuantity(int quantity) {
        if (quantity <= 0) throw new BusinessException("Quantity must be greater than zero.");
    }
}
