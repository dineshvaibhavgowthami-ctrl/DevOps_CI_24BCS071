package com.warehouse.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;

@Entity
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int quantity;
    @Column(nullable = false, unique = true)
    private String barcode;
    private String status;
    @ManyToOne
    private Cell currentCell;

    protected InventoryItem() {}

    public InventoryItem(String name, int quantity, String barcode, Cell currentCell) {
        this.name = name;
        this.quantity = quantity;
        this.barcode = barcode;
        this.currentCell = currentCell;
        this.status = "IN_STOCK";
    }

    public void moveTo(Cell cell) { this.currentCell = cell; }
    public void markExited() { this.status = "EXITED"; this.currentCell = null; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public String getBarcode() { return barcode; }
    public String getStatus() { return status; }
    public Cell getCurrentCell() { return currentCell; }
}
