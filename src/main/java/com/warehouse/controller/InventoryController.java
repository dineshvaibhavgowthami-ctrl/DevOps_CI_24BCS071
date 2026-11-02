package com.warehouse.controller;

import com.warehouse.controller.ApiDtos.AlertResponse;
import com.warehouse.controller.ApiDtos.CellResponse;
import com.warehouse.controller.ApiDtos.DashboardResponse;
import com.warehouse.controller.ApiDtos.ExitRequest;
import com.warehouse.controller.ApiDtos.ExitResponse;
import com.warehouse.controller.ApiDtos.ItemResponse;
import com.warehouse.controller.ApiDtos.MoveRequest;
import com.warehouse.controller.ApiDtos.MovementResponse;
import com.warehouse.model.Cell;
import com.warehouse.model.ExitRecord;
import com.warehouse.model.InventoryItem;
import com.warehouse.model.MovementHistory;
import com.warehouse.repository.ExitRecordRepository;
import com.warehouse.repository.InventoryItemRepository;
import com.warehouse.repository.MovementHistoryRepository;
import com.warehouse.service.BarcodeService;
import com.warehouse.service.InventoryMovementService;
import com.warehouse.service.LocationAllocationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InventoryController {
    private final LocationAllocationService allocationService;
    private final BarcodeService barcodeService;
    private final InventoryMovementService movementService;
    private final InventoryItemRepository itemRepository;
    private final MovementHistoryRepository movementRepository;
    private final ExitRecordRepository exitRepository;

    public InventoryController(LocationAllocationService allocationService, BarcodeService barcodeService,
                               InventoryMovementService movementService, InventoryItemRepository itemRepository,
                               MovementHistoryRepository movementRepository, ExitRecordRepository exitRepository) {
        this.allocationService = allocationService;
        this.barcodeService = barcodeService;
        this.movementService = movementService;
        this.itemRepository = itemRepository;
        this.movementRepository = movementRepository;
        this.exitRepository = exitRepository;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        List<Cell> cells = allocationService.getCells();
        List<MovementResponse> recent = movementRepository.findTop10ByOrderByMovementDateDesc().stream()
                .map(MovementResponse::from).toList();
        List<AlertResponse> alerts = exitRepository.findTop10ByAuthorizedFalseOrderByVerificationDateDesc().stream()
                .map(AlertResponse::from).toList();
        long occupied = cells.stream().filter(cell -> cell.getOccupiedCapacity() > 0).count();
        return new DashboardResponse(itemRepository.countByStatus("IN_STOCK"), occupied, cells.size() - occupied, recent, alerts);
    }

    @GetMapping("/cells")
    public List<CellResponse> cells() {
        return allocationService.getCells().stream().map(CellResponse::from).toList();
    }

    @PostMapping("/items")
    public ItemResponse addItem(@RequestParam String name, @RequestParam int quantity) {
        return ItemResponse.from(allocationService.addItem(name, quantity));
    }

    @GetMapping("/items/{barcode}")
    public ItemResponse findItem(@PathVariable String barcode) {
        return ItemResponse.from(barcodeService.findItemByBarcode(barcode));
    }

    @PostMapping("/items/{barcode}/move")
    public MovementResponse moveItem(@PathVariable String barcode, @RequestBody MoveRequest request) {
        MovementHistory history = movementService.moveItem(barcode, request.cellId());
        return MovementResponse.from(history);
    }

    @GetMapping("/items/{barcode}/movements")
    public List<MovementResponse> movementHistory(@PathVariable String barcode) {
        return movementService.getMovementHistory(barcode).stream().map(MovementResponse::from).toList();
    }

    @PostMapping("/exits")
    public ExitResponse verifyExit(@RequestBody ExitRequest request) {
        ExitRecord record = movementService.verifyExit(request.barcode(), request.authorized());
        return ExitResponse.from(record);
    }
}
