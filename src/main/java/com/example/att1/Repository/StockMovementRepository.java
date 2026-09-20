package com.example.att1.Repository;

import com.example.att1.Entity.Product;
import com.example.att1.Entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductOrderByMovementTimeDesc(Product product);

    List<StockMovement> findTop10ByOrderByMovementTimeDesc();
}
