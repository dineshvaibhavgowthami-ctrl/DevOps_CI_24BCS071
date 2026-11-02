package com.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.warehouse.exception.BusinessException;
import com.warehouse.model.Cell;
import com.warehouse.model.ExitRecord;
import com.warehouse.model.InventoryItem;
import com.warehouse.model.MovementHistory;
import com.warehouse.model.Rack;
import com.warehouse.model.Shelf;
import com.warehouse.model.Warehouse;
import com.warehouse.repository.ExitRecordRepository;
import com.warehouse.repository.InventoryItemRepository;
import com.warehouse.repository.MovementHistoryRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTest {
    @Mock private BarcodeService barcodeService;
    @Mock private LocationAllocationService allocationService;
    @Mock private InventoryItemRepository itemRepository;
    @Mock private MovementHistoryRepository movementRepository;
    @Mock private ExitRecordRepository exitRepository;

    private InventoryMovementService service;
    private Cell source;
    private Cell destination;
    private InventoryItem item;

    @BeforeEach
    void setUp() {
        service = new InventoryMovementService(barcodeService, allocationService, itemRepository, movementRepository, exitRepository);
        Warehouse warehouse = new Warehouse("Test warehouse");
        Rack rack = new Rack("Rack 1", warehouse);
        Shelf shelf = new Shelf("Shelf 1", rack);
        source = new Cell("Cell 1", 10, shelf);
        destination = new Cell("Cell 2", 10, shelf);
        ReflectionTestUtils.setField(source, "id", 1L);
        ReflectionTestUtils.setField(destination, "id", 2L);
        source.addQuantity(4);
        item = new InventoryItem("Cables", 4, "WH-KEEP", source);
        when(barcodeService.findItemByBarcode("WH-KEEP")).thenReturn(item);
    }

    @Test
    void movesItemAndRecordsPreviousAndNewLocationsWithoutChangingBarcode() {
        when(allocationService.findCellById(2L)).thenReturn(destination);
        when(allocationService.checkCapacity(destination, 4)).thenReturn(true);
        when(movementRepository.save(any(MovementHistory.class))).thenAnswer(call -> call.getArgument(0));

        MovementHistory history = service.moveItem("WH-KEEP", 2L);

        assertEquals(destination, item.getCurrentCell());
        assertEquals("WH-KEEP", item.getBarcode());
        assertEquals(0, source.getOccupiedCapacity());
        assertEquals(4, destination.getOccupiedCapacity());
        assertTrue(history.getPreviousLocation().endsWith("Cell 1"));
        assertTrue(history.getNewLocation().endsWith("Cell 2"));
        assertNotNull(history.getMovementDate());
        verify(itemRepository).save(item);
    }

    @Test
    void rejectsMovementToAFullCellAndLeavesSourceUnchanged() {
        when(allocationService.findCellById(2L)).thenReturn(destination);
        when(allocationService.checkCapacity(destination, 4)).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.moveItem("WH-KEEP", 2L));
        assertEquals(source, item.getCurrentCell());
        assertEquals(4, source.getOccupiedCapacity());
    }

    @Test
    void returnsMovementHistoryForAnExistingItem() {
        MovementHistory history = new MovementHistory(item, "old", "new", java.time.LocalDateTime.now());
        when(movementRepository.findByItemBarcodeOrderByMovementDateDesc("WH-KEEP")).thenReturn(List.of(history));

        assertEquals(List.of(history), service.getMovementHistory("WH-KEEP"));
    }

    @Test
    void authorizedExitIsAcceptedAndFreesCellCapacity() {
        when(exitRepository.save(any(ExitRecord.class))).thenAnswer(call -> call.getArgument(0));

        ExitRecord record = service.verifyExit("WH-KEEP", true);

        assertTrue(record.isAccepted());
        assertTrue(record.isAuthorized());
        assertEquals("EXITED", item.getStatus());
        assertEquals(null, item.getCurrentCell());
        assertEquals(0, source.getOccupiedCapacity());
        verify(itemRepository).save(item);
    }

    @Test
    void unauthorizedExitIsRejectedRecordedAndLeavesItemInCell() {
        when(exitRepository.save(any(ExitRecord.class))).thenAnswer(call -> call.getArgument(0));

        ExitRecord record = service.verifyExit("WH-KEEP", false);

        assertFalse(record.isAccepted());
        assertFalse(record.isAuthorized());
        assertEquals("IN_STOCK", item.getStatus());
        assertEquals(source, item.getCurrentCell());
        assertEquals(4, source.getOccupiedCapacity());
        verify(exitRepository).save(any(ExitRecord.class));
    }
}
