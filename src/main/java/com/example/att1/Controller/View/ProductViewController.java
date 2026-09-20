package com.example.att1.Controller.View;

import com.example.att1.Entity.Product;
import com.example.att1.Entity.StockMovement;
import com.example.att1.Service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductViewController {

    private final ProductService productService;

    public ProductViewController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.findAll();
        int lowThreshold = 20;

        int totalItems = productService.getTotalItemsInStock(products);
        int itemsRunningLow = productService.getItemsRunningLow(products, lowThreshold);

        long inStockCount = productService.countInStock(products, lowThreshold);
        long lowStockCount = productService.countLowStock(products, lowThreshold);
        long outOfStockCount = productService.countOutOfStock(products);

        List<StockMovement> recentMovements = productService.getRecentMovements(5);

        model.addAttribute("products", products);
        model.addAttribute("newProduct", new Product());

        model.addAttribute("totalItems", totalItems);
        model.addAttribute("itemsRunningLow", itemsRunningLow);
        model.addAttribute("inStockCount", inStockCount);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("recentMovements", recentMovements);

        return "products";
    }

    private void fillProductsPageModel(Model model) {
        List<Product> products = productService.findAll();

        int lowThreshold = 20;

        model.addAttribute("products", products);
        model.addAttribute("totalItems", productService.getTotalItemsInStock(products));
        model.addAttribute("itemsRunningLow", productService.getItemsRunningLow(products, lowThreshold));
        model.addAttribute("inStockCount", productService.countInStock(products, lowThreshold));
        model.addAttribute("lowStockCount", productService.countLowStock(products, lowThreshold));
        model.addAttribute("outOfStockCount", productService.countOutOfStock(products));
        model.addAttribute("recentMovements", productService.getRecentMovements(5));
    }

    @PostMapping
    public String createProduct(@ModelAttribute("newProduct") Product product, Model model) {
        try {
            if (product.getQuantityOnHand() == null) product.setQuantityOnHand(0);
            if (product.getActive() == null) product.setActive(true);

            productService.create(product);
            return "redirect:/products";

        } catch (IllegalArgumentException ex) {
            fillProductsPageModel(model);
            model.addAttribute("newProduct", product);
            model.addAttribute("errorMessage", ex.getMessage());
            return "products";

        } catch (DataIntegrityViolationException ex) {
            fillProductsPageModel(model);
            model.addAttribute("newProduct", product);
            model.addAttribute("errorMessage", "Cannot save product: duplicate/invalid SKU.");
            return "products";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/products";
    }

    @GetMapping("/{id}/history")
    public String viewHistory(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        List<StockMovement> history = productService.getHistory(id);

        model.addAttribute("product", product);
        model.addAttribute("history", history);
        return "product-history";
    }

    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "product-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute("product") Product product,
                                Model model) {
        try {
            if (product.getQuantityOnHand() == null) product.setQuantityOnHand(0);
            if (product.getActive() == null) product.setActive(true);

            productService.update(id, product);
            return "redirect:/products/" + id + "/history";

        } catch (IllegalArgumentException ex) {
            product.setId(id);
            model.addAttribute("product", product);
            model.addAttribute("errorMessage", ex.getMessage());
            return "product-edit";

        } catch (DataIntegrityViolationException ex) {
            product.setId(id);
            model.addAttribute("product", product);
            model.addAttribute("errorMessage", "Product with this SKU already exists.");
            return "product-edit";
        }
    }
}
