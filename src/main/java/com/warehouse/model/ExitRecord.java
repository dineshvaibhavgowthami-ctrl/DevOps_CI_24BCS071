package com.warehouse.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class ExitRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private InventoryItem item;
    private boolean authorized;
    private boolean accepted;
    private LocalDateTime verificationDate;

    protected ExitRecord() {}

    public ExitRecord(InventoryItem item, boolean authorized, boolean accepted, LocalDateTime verificationDate) {
        this.item = item;
        this.authorized = authorized;
        this.accepted = accepted;
        this.verificationDate = verificationDate;
    }

    public Long getId() { return id; }
    public InventoryItem getItem() { return item; }
    public boolean isAuthorized() { return authorized; }
    public boolean isAccepted() { return accepted; }
    public LocalDateTime getVerificationDate() { return verificationDate; }
}
