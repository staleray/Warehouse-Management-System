package com.example.att1.Controller.View;

import com.example.att1.DTO.CreateParcelRequest;
import com.example.att1.Entity.Enums.ParcelStatus;
import com.example.att1.Service.ParcelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/parcels")
public class ParcelViewController {

    private final ParcelService parcelService;

    public ParcelViewController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @GetMapping
    public String listParcels(Model model) {
        List<com.example.att1.Entity.Parcel> parcels = parcelService.getAll();

        model.addAttribute("parcels", parcels);
        model.addAttribute("statuses", ParcelStatus.values());

        model.addAttribute("totalCount", parcels.size());
        model.addAttribute("readyCount", parcels.stream().filter(p -> p.getStatus() == ParcelStatus.READY).count());
        model.addAttribute("inTransitCount", parcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_TRANSIT).count());
        model.addAttribute("deliveredCount", parcels.stream().filter(p -> p.getStatus() == ParcelStatus.DELIVERED).count());

        return "parcels";
    }

    @PostMapping("/create-from-order")
    public String createFromOrder(@RequestParam Long orderId,
                                  @RequestParam String trackingNumber,
                                  @RequestParam String carrier) {

        CreateParcelRequest req = new CreateParcelRequest();
        req.setOrderId(orderId);
        req.setTrackingNumber(trackingNumber);
        req.setCarrier(carrier);

        parcelService.createParcel(req);
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/{id}/status")
    public String updateParcelStatus(@PathVariable Long id,
                                     @RequestParam String status) {
        parcelService.updateStatus(id, ParcelStatus.valueOf(status));
        return "redirect:/parcels";
    }
}
