package com.warehouse.controller;

import com.warehouse.model.Cell;
import com.warehouse.model.ExitRecord;
import com.warehouse.model.InventoryItem;
import com.warehouse.model.MovementHistory;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    public record ItemResponse(String name, int quantity, String barcode, String status,
                               String warehouse, String rack, String shelf, String cell) {
        public static ItemResponse from(InventoryItem item) {
            if (item.getCurrentCell() == null) {
                return new ItemResponse(item.getName(), item.getQuantity(), item.getBarcode(), item.getStatus(),
                        "", "", "", "");
            }
            Cell location = item.getCurrentCell();
            return new ItemResponse(item.getName(), item.getQuantity(), item.getBarcode(), item.getStatus(),
                    location.getShelf().getRack().getWarehouse().getName(), location.getShelf().getRack().getName(),
                    location.getShelf().getName(), location.getName());
        }
    }

    public record CellResponse(Long id, String location, int occupiedCapacity, int maximumCapacity) {
        public static CellResponse from(Cell cell) {
            return new CellResponse(cell.getId(), cell.getFullLocation(), cell.getOccupiedCapacity(), cell.getMaximumCapacity());
        }
    }

    public record MovementResponse(String barcode, String itemName, String previousLocation,
                                   String newLocation, LocalDateTime movementDate) {
        public static MovementResponse from(MovementHistory history) {
            return new MovementResponse(history.getItem().getBarcode(), history.getItem().getName(),
                    history.getPreviousLocation(), history.getNewLocation(), history.getMovementDate());
        }
    }

    public record AlertResponse(String barcode, String itemName, LocalDateTime verificationDate, String message) {
        public static AlertResponse from(ExitRecord record) {
            return new AlertResponse(record.getItem().getBarcode(), record.getItem().getName(),
                    record.getVerificationDate(), "Unauthorized exit attempt rejected");
        }
    }

    public record DashboardResponse(long totalItems, long occupiedCells, long availableCells,
                                   List<MovementResponse> recentMovements, List<AlertResponse> unauthorizedAlerts) {}

    public record MoveRequest(Long cellId) {}
    public record ExitRequest(String barcode, boolean authorized) {}
    public record ExitResponse(String barcode, boolean authorized, boolean accepted, LocalDateTime verificationDate,
                               String message) {
        public static ExitResponse from(ExitRecord record) {
            return new ExitResponse(record.getItem().getBarcode(), record.isAuthorized(), record.isAccepted(),
                    record.getVerificationDate(), record.isAccepted() ? "Exit authorized and recorded."
                    : "Unauthorized exit rejected and alert recorded.");
        }
    }
}
