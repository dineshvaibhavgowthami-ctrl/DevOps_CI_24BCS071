package com.warehouse.service;

import com.warehouse.exception.BusinessException;
import com.warehouse.exception.NotFoundException;
import com.warehouse.model.Cell;
import com.warehouse.model.InventoryItem;
import com.warehouse.repository.InventoryItemRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BarcodeService {
    private final InventoryItemRepository itemRepository;

    public BarcodeService(InventoryItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public String generateBarcode(Cell confirmedAllocation) {
        if (confirmedAllocation == null) {
            throw new BusinessException("A warehouse location must be allocated before generating a barcode.");
        }
        String barcode;
        do {
            barcode = "WH-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        } while (!isBarcodeUnique(barcode));
        return barcode;
    }

    public InventoryItem findItemByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) throw new BusinessException("Barcode is required.");
        return itemRepository.findByBarcode(barcode.trim())
                .orElseThrow(() -> new NotFoundException("No item found for barcode: " + barcode.trim()));
    }

    public boolean isBarcodeUnique(String barcode) {
        return !itemRepository.existsByBarcode(barcode);
    }
}
