package com.warehouse.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Rack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne(optional = false)
    private Warehouse warehouse;

    protected Rack() {}

    public Rack(String name, Warehouse warehouse) { this.name = name; this.warehouse = warehouse; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public Warehouse getWarehouse() { return warehouse; }
}
