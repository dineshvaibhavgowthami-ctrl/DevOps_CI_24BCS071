package com.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.warehouse.exception.BusinessException;
import com.warehouse.model.Cell;
import com.warehouse.model.InventoryItem;
import com.warehouse.model.Rack;
import com.warehouse.model.Shelf;
import com.warehouse.model.Warehouse;
import com.warehouse.repository.CellRepository;
import com.warehouse.repository.InventoryItemRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationAllocationServiceTest {
    @Mock private CellRepository cellRepository;
    @Mock private InventoryItemRepository itemRepository;
    @Mock private BarcodeService barcodeService;

    private LocationAllocationService service;
    private Cell firstCell;
    private Cell secondCell;

    @BeforeEach
    void setUp() {
        service = new LocationAllocationService(cellRepository, itemRepository, barcodeService);
        Warehouse warehouse = new Warehouse("Test warehouse");
        Rack rack = new Rack("Rack 1", warehouse);
        Shelf shelf = new Shelf("Shelf 1", rack);
        firstCell = new Cell("Cell 1", 10, shelf);
        secondCell = new Cell("Cell 2", 10, shelf);
    }

    @Test
    void allocatesAndPersistsItemInFirstSuitableCell() {
        when(cellRepository.findAllByOrderByIdAsc()).thenReturn(List.of(firstCell, secondCell));
        when(barcodeService.generateBarcode(firstCell)).thenReturn("WH-ALLOCATED");
        when(itemRepository.save(org.mockito.ArgumentMatchers.any(InventoryItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItem item = service.addItem("Bolts", 4);

        assertNotNull(item);
        assertEquals(firstCell, item.getCurrentCell());
        assertEquals(4, firstCell.getOccupiedCapacity());
        assertEquals("WH-ALLOCATED", item.getBarcode());
        verify(cellRepository).save(firstCell);
        verify(barcodeService).generateBarcode(firstCell);
    }

    @Test
    void rejectsAllocationWhenNoCellHasCapacity() {
        firstCell.addQuantity(8);
        secondCell.addQuantity(10);
        when(cellRepository.findAllByOrderByIdAsc()).thenReturn(List.of(firstCell, secondCell));

        assertThrows(BusinessException.class, () -> service.allocateLocation(3));
    }

    @Test
    void rejectsZeroAndNegativeQuantities() {
        assertThrows(BusinessException.class, () -> service.allocateLocation(0));
        assertThrows(BusinessException.class, () -> service.allocateLocation(-2));
    }

    @Test
    void reportsCapacityCorrectlyAndSelectsNextAvailableCell() {
        firstCell.addQuantity(7);
        assertFalse(service.checkCapacity(firstCell, 4));
        assertTrue(service.checkCapacity(firstCell, 3));
        assertFalse(service.isLocationAvailable(firstCell, 0));
        when(cellRepository.findAllByOrderByIdAsc()).thenReturn(List.of(firstCell, secondCell));

        assertEquals(secondCell, service.allocateLocation(4));
    }

    @Test
    void rejectsInsufficientCapacityWithoutChangingOccupancy() {
        firstCell.addQuantity(9);
        when(cellRepository.findAllByOrderByIdAsc()).thenReturn(List.of(firstCell));

        assertThrows(BusinessException.class, () -> service.addItem("Boxes", 2));
        assertEquals(9, firstCell.getOccupiedCapacity());
    }
}
