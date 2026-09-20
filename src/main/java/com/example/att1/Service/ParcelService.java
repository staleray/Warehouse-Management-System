package com.example.att1.Service;

import com.example.att1.DTO.CreateParcelRequest;
import com.example.att1.Entity.Enums.ParcelStatus;
import com.example.att1.Entity.Order;
import com.example.att1.Entity.Parcel;
import com.example.att1.Repository.OrderRepository;
import com.example.att1.Repository.ParcelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final OrderRepository orderRepository;

    public ParcelService(ParcelRepository parcelRepository,
                         OrderRepository orderRepository) {
        this.parcelRepository = parcelRepository;
        this.orderRepository = orderRepository;
    }

    public Parcel getById(Long id) {
        return parcelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + id));
    }

    public List<Parcel> getAll() {
        return parcelRepository.findAllWithOrder();
    }

    public List<Parcel> getByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return order.getParcels();
    }

    @Transactional
    public Parcel createParcel(CreateParcelRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("Order id is required");
        }

        String tracking = request.getTrackingNumber();
        if (tracking == null || tracking.isBlank()) {
            throw new IllegalArgumentException("Tracking number is required");
        }

        String carrier = request.getCarrier();
        if (carrier == null || carrier.isBlank()) {
            throw new IllegalArgumentException("Carrier is required");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + request.getOrderId()));

        if (order.getParcels() == null) {
            order.setParcels(new java.util.ArrayList<>());
        }

        boolean alreadyExists = order.getParcels().stream()
                .anyMatch(p -> p.getTrackingNumber() != null &&
                        p.getTrackingNumber().equalsIgnoreCase(tracking.trim()));
        if (alreadyExists) {
            throw new IllegalArgumentException("Parcel with this tracking number already exists for this order");
        }

        Parcel parcel = Parcel.builder()
                .order(order)
                .trackingNumber(tracking.trim())
                .carrier(carrier.trim())
                .status(ParcelStatus.READY)
                .build();

        parcel = parcelRepository.save(parcel);

        order.getParcels().add(parcel);

        orderRepository.save(order);

        return parcel;
    }

    @Transactional
    public Parcel updateStatus(Long id, ParcelStatus newStatus) {
        Parcel parcel = getById(id);
        parcel.setStatus(newStatus);

        if (newStatus == ParcelStatus.IN_TRANSIT && parcel.getShippedAt() == null) {
            parcel.setShippedAt(LocalDateTime.now());
        } else if (newStatus == ParcelStatus.DELIVERED && parcel.getDeliveredAt() == null) {
            parcel.setDeliveredAt(LocalDateTime.now());
        }

        return parcelRepository.save(parcel);
    }
}
