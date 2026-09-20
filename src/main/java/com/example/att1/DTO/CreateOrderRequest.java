package com.example.att1.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {
    private String orderNumber;
    private String notes;
    private List<OrderItemRequest> items;
}
