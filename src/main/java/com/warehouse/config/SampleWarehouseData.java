package com.warehouse.config;

import com.warehouse.model.Cell;
import com.warehouse.model.Rack;
import com.warehouse.model.Shelf;
import com.warehouse.model.Warehouse;
import com.warehouse.repository.CellRepository;
import com.warehouse.repository.RackRepository;
import com.warehouse.repository.ShelfRepository;
import com.warehouse.repository.WarehouseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleWarehouseData {
    @Bean
    CommandLineRunner initializeWarehouse(WarehouseRepository warehouses, RackRepository racks,
                                           ShelfRepository shelves, CellRepository cells) {
        return args -> {
            if (warehouses.count() > 0) return;
            Warehouse warehouse = warehouses.save(new Warehouse("Main Warehouse"));
            for (int rackNumber = 1; rackNumber <= 2; rackNumber++) {
                Rack rack = racks.save(new Rack("Rack " + rackNumber, warehouse));
                for (int shelfNumber = 1; shelfNumber <= 2; shelfNumber++) {
                    Shelf shelf = shelves.save(new Shelf("Shelf " + shelfNumber, rack));
                    for (int cellNumber = 1; cellNumber <= 3; cellNumber++) {
                        cells.save(new Cell("Cell " + cellNumber, 100, shelf));
                    }
                }
            }
        };
    }
}
