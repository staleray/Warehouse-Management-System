package com.example.att1.DTO;

import com.example.att1.Entity.Enums.ParcelStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateParcelStatusRequest {
    private ParcelStatus status;
}
