package com.example.att1.Service;


import com.example.att1.DTO.CreateOrderRequest;
import com.example.att1.DTO.OrderItemRequest;
import com.example.att1.Entity.Enums.OrderStatus;
import com.example.att1.Entity.Enums.StockMovementType;
import com.example.att1.Entity.Order;
import com.example.att1.Entity.OrderItem;
import com.example.att1.Entity.Product;
import com.example.att1.Repository.OrderItemRepository;
import com.example.att1.Repository.OrderRepository;
import com.example.att1.Repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        OrderItemRepository orderItemRepository,
                        ProductService productService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
    }

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public long getOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public List<Order> getRecentOrders(int limit) {
        List<Order> list = orderRepository.findTop5ByOrderByCreatedAtDesc();
        if (list.size() <= limit) return list;
        return list.subList(0, limit);
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        java.util.Map<Long, Integer> merged = new java.util.LinkedHashMap<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new IllegalArgumentException("Product must be selected for each item");
            }
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be at least 1");
            }
            merged.merge(itemReq.getProductId(), itemReq.getQuantity(), Integer::sum);
        }

        String orderNumber = request.getOrderNumber();
        if (orderNumber == null || orderNumber.isBlank()) {
            orderNumber = generateOrderNumber();
        }

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .status(OrderStatus.NEW)
                .notes(request.getNotes())
                .build();

        order = orderRepository.save(order);

        if (order.getItems() == null) {
            order.setItems(new java.util.ArrayList<>());
        }

        for (var entry : merged.entrySet()) {
            Long productId = entry.getKey();
            Integer qty = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            int available = (product.getQuantityOnHand() == null) ? 0 : product.getQuantityOnHand();

            if (available <= 0) {
                throw new IllegalArgumentException("Product is out of stock: " + product.getName());
            }
            if (qty > available) {
                throw new IllegalArgumentException(
                        "Not enough stock for: " + product.getName() +
                                ". Requested: " + qty + ", available: " + available
                );
            }
        }

        for (var entry : merged.entrySet()) {
            Long productId = entry.getKey();
            Integer qty = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            productService.adjustStock(
                    product,
                    StockMovementType.OUTBOUND,
                    qty,
                    orderNumber,
                    "Order created"
            );

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(qty)
                    .build();

            orderItemRepository.save(orderItem);
            order.getItems().add(orderItem);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus) {
        Order order = getById(id);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    private String generateOrderNumber() {
        return "S-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public record OrderStats(long total, long inProgress, long shipped) {}

    public OrderStats computeStats(List<Order> orders) {
        long total = orders.size();
        long inProgress = orders.stream().filter(o -> o.getStatus() == OrderStatus.IN_PROGRESS).count();
        long shipped = orders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
        return new OrderStats(total, inProgress, shipped);
    }

}
