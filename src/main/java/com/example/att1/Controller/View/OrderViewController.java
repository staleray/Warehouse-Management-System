package com.example.att1.Controller.View;

import com.example.att1.DTO.CreateOrderRequest;
import com.example.att1.DTO.CreateParcelRequest;
import com.example.att1.DTO.OrderItemRequest;
import com.example.att1.Entity.Enums.OrderStatus;
import com.example.att1.Entity.Order;
import com.example.att1.Service.OrderService;
import com.example.att1.Service.ParcelService;
import com.example.att1.Service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderViewController {

    private final OrderService orderService;
    private final ProductService productService;
    private final ParcelService parcelService;

    public OrderViewController(OrderService orderService,
                               ProductService productService,
                               ParcelService parcelService) {
        this.orderService = orderService;
        this.productService = productService;
        this.parcelService = parcelService;
    }

    @GetMapping
    public String listOrders(Model model) {
        List<Order> orders = orderService.getAll();

        var stats = orderService.computeStats(orders);
        List<Order> recentOrders = orderService.getRecentOrders(5);

        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", stats.total());
        model.addAttribute("inProgressOrders", stats.inProgress());
        model.addAttribute("shippedOrders", stats.shipped());
        model.addAttribute("recentOrders", recentOrders);

        return "orders";
    }

    @GetMapping("/new")
    public String newOrderForm(Model model) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(new ArrayList<>());

        OrderItemRequest itemReq = new OrderItemRequest();
        request.getItems().add(itemReq);

        model.addAttribute("orderRequest", request);
        model.addAttribute("products", productService.findAll());
        return "order-form";
    }

    @PostMapping
    public String createOrder(@ModelAttribute("orderRequest") CreateOrderRequest request, Model model) {
        if (request.getItems() != null) {
            request.getItems().removeIf(item ->
                    item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0
            );
        }

        try {
            orderService.createOrder(request);
            return "redirect:/orders";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("products", productService.findAll());
            model.addAttribute("error", ex.getMessage());
            return "order-form";
        }
    }

    @PostMapping("/{id}/parcels")
    public String createParcel(@PathVariable Long id,
                               @RequestParam String trackingNumber,
                               @RequestParam String carrier,
                               RedirectAttributes ra) {

        try {
            CreateParcelRequest req = new CreateParcelRequest();
            req.setOrderId(id);
            req.setTrackingNumber(trackingNumber);
            req.setCarrier(carrier);

            parcelService.createParcel(req);

        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/orders/" + id;
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderService.getById(id);
        model.addAttribute("order", order);
        return "order-details";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
        return "redirect:/orders/" + id;
    }
}
