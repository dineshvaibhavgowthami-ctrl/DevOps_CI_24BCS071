package com.warehouse.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Cell {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int maximumCapacity;
    private int occupiedCapacity;
    @ManyToOne(optional = false)
    private Shelf shelf;

    protected Cell() {}

    public Cell(String name, int maximumCapacity, Shelf shelf) {
        this.name = name;
        this.maximumCapacity = maximumCapacity;
        this.shelf = shelf;
    }

    public boolean hasCapacity(int quantity) {
        return quantity > 0 && maximumCapacity - occupiedCapacity >= quantity;
    }

    public void addQuantity(int quantity) {
        if (!hasCapacity(quantity)) throw new IllegalArgumentException("Insufficient cell capacity.");
        occupiedCapacity += quantity;
    }

    public void removeQuantity(int quantity) {
        if (quantity <= 0 || quantity > occupiedCapacity) throw new IllegalArgumentException("Invalid quantity to remove.");
        occupiedCapacity -= quantity;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getMaximumCapacity() { return maximumCapacity; }
    public int getOccupiedCapacity() { return occupiedCapacity; }
    public Shelf getShelf() { return shelf; }
    public String getFullLocation() {
        return shelf.getRack().getWarehouse().getName() + " / " + shelf.getRack().getName()
                + " / " + shelf.getName() + " / " + name;
    }
}
