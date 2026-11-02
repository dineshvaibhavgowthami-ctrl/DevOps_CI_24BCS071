package com.warehouse.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class MovementHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private InventoryItem item;
    private String previousLocation;
    private String newLocation;
    private LocalDateTime movementDate;

    protected MovementHistory() {}

    public MovementHistory(InventoryItem item, String previousLocation, String newLocation, LocalDateTime movementDate) {
        this.item = item;
        this.previousLocation = previousLocation;
        this.newLocation = newLocation;
        this.movementDate = movementDate;
    }

    public Long getId() { return id; }
    public InventoryItem getItem() { return item; }
    public String getPreviousLocation() { return previousLocation; }
    public String getNewLocation() { return newLocation; }
    public LocalDateTime getMovementDate() { return movementDate; }
}
