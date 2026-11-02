package com.warehouse.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Shelf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne(optional = false)
    private Rack rack;

    protected Shelf() {}

    public Shelf(String name, Rack rack) { this.name = name; this.rack = rack; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public Rack getRack() { return rack; }
}
