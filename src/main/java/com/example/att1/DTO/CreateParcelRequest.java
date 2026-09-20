package com.example.att1.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateParcelRequest {
    private Long orderId;
    private String trackingNumber;
    private String carrier;
}
