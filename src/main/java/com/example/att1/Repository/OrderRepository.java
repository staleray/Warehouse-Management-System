package com.example.att1.Repository;

import com.example.att1.Entity.Enums.OrderStatus;
import com.example.att1.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByStatus(OrderStatus status);

    List<Order> findTop5ByOrderByCreatedAtDesc();
}
