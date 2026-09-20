package com.example.att1.Controller.API;

import com.example.att1.DTO.CreateParcelRequest;
import com.example.att1.DTO.UpdateParcelStatusRequest;
import com.example.att1.Entity.Parcel;
import com.example.att1.Service.ParcelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parcels")
@CrossOrigin(origins = "*")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @GetMapping
    public List<Parcel> getAll() {
        return parcelService.getAll();
    }

    @GetMapping("/{id}")
    public Parcel getById(@PathVariable Long id) {
        return parcelService.getById(id);
    }

    @GetMapping("/order/{orderId}")
    public List<Parcel> getByOrder(@PathVariable Long orderId) {
        return parcelService.getByOrder(orderId);
    }

    @PostMapping
    public Parcel create(@RequestBody CreateParcelRequest request) {
        return parcelService.createParcel(request);
    }

    @PutMapping("/{id}/status")
    public Parcel updateStatus(@PathVariable Long id,
                               @RequestBody UpdateParcelStatusRequest request) {
        return parcelService.updateStatus(id, request.getStatus());
    }
}
