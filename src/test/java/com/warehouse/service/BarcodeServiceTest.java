package com.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.warehouse.exception.BusinessException;
import com.warehouse.exception.NotFoundException;
import com.warehouse.model.Cell;
import com.warehouse.model.InventoryItem;
import com.warehouse.model.Rack;
import com.warehouse.model.Shelf;
import com.warehouse.model.Warehouse;
import com.warehouse.repository.InventoryItemRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BarcodeServiceTest {
    @Mock private InventoryItemRepository itemRepository;
    private BarcodeService service;
    private Cell allocatedCell;

    @BeforeEach
    void setUp() {
        service = new BarcodeService(itemRepository);
        allocatedCell = new Cell("Cell 1", 20,
                new Shelf("Shelf 1", new Rack("Rack 1", new Warehouse("Test warehouse"))));
    }

    @Test
    void generatesBarcodeOnlyWhenAllocationIsConfirmed() {
        when(itemRepository.existsByBarcode(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);

        String barcode = service.generateBarcode(allocatedCell);

        assertTrue(barcode.matches("WH-[A-F0-9]{12}"));
        verify(itemRepository).existsByBarcode(barcode);
    }

    @Test
    void generatesDifferentUniqueBarcodesForDifferentItems() {
        when(itemRepository.existsByBarcode(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);

        String firstBarcode = service.generateBarcode(allocatedCell);
        String secondBarcode = service.generateBarcode(allocatedCell);

        assertNotEquals(firstBarcode, secondBarcode);
    }

    @Test
    void rejectsBarcodeGenerationWithoutAnAllocatedCell() {
        assertThrows(BusinessException.class, () -> service.generateBarcode(null));
    }

    @Test
    void findsItemByValidBarcode() {
        InventoryItem item = new InventoryItem("Nails", 2, "WH-VALID", allocatedCell);
        when(itemRepository.findByBarcode("WH-VALID")).thenReturn(Optional.of(item));

        assertEquals(item, service.findItemByBarcode(" WH-VALID "));
    }

    @Test
    void rejectsInvalidOrNonexistentBarcode() {
        assertThrows(BusinessException.class, () -> service.findItemByBarcode(" "));
        when(itemRepository.findByBarcode("WH-MISSING")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findItemByBarcode("WH-MISSING"));
    }
}
