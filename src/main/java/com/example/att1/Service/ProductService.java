package com.example.att1.Service;

import com.example.att1.Entity.Enums.StockMovementType;
import com.example.att1.Entity.Product;
import com.example.att1.Entity.StockMovement;
import com.example.att1.Repository.ProductRepository;
import com.example.att1.Repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public ProductService(ProductRepository productRepository,
                          StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<Product> findAll() {
        return productRepository.findByActiveTrue();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public int getTotalItemsInStock(List<Product> products) {
        return products.stream()
                .mapToInt(p -> p.getQuantityOnHand() != null ? p.getQuantityOnHand() : 0)
                .sum();
    }

    public int getItemsRunningLow(List<Product> products, int threshold) {
        return (int) products.stream()
                .filter(p -> {
                    Integer q = p.getQuantityOnHand();
                    return q != null && q > 0 && q <= threshold;
                })
                .count();
    }

    public long countInStock(List<Product> products, int threshold) {
        return products.stream()
                .filter(p -> {
                    Integer q = p.getQuantityOnHand();
                    return q != null && q > threshold;
                })
                .count();
    }

    public long countLowStock(List<Product> products, int threshold) {
        return products.stream()
                .filter(p -> {
                    Integer q = p.getQuantityOnHand();
                    return q != null && q > 0 && q <= threshold;
                })
                .count();
    }

    public long countOutOfStock(List<Product> products) {
        return products.stream()
                .filter(p -> p.getQuantityOnHand() == null || p.getQuantityOnHand() == 0)
                .count();
    }

    public List<StockMovement> getRecentMovements(int limit) {
        List<StockMovement> all = stockMovementRepository.findTop10ByOrderByMovementTimeDesc();
        if (all.size() <= limit) return all;
        return all.subList(0, limit);
    }

    @Transactional
    public Product create(Product product) {
        if (product.getQuantityOnHand() == null) product.setQuantityOnHand(0);
        if (product.getActive() == null) product.setActive(true);

        String sku = product.getSku();
        if (sku != null && !sku.isBlank()) {
            sku = sku.trim();

            var existingOpt = productRepository.findFirstBySkuIgnoreCase(sku);
            if (existingOpt.isPresent()) {
                Product existing = existingOpt.get();

                if (Boolean.TRUE.equals(existing.getActive())) {
                    throw new IllegalArgumentException("Product with SKU '" + sku + "' already exists.");
                }

                existing.setActive(true);
                existing.setName(product.getName());
                existing.setSku(sku);
                existing.setCategory(product.getCategory());
                existing.setLocation(product.getLocation());
                existing.setDescription(product.getDescription());

                Integer oldQty = existing.getQuantityOnHand() == null ? 0 : existing.getQuantityOnHand();
                Integer newQty = product.getQuantityOnHand() == null ? 0 : product.getQuantityOnHand();

                if (!oldQty.equals(newQty)) {
                    int diff = newQty - oldQty;
                    StockMovementType type = diff >= 0 ? StockMovementType.INBOUND : StockMovementType.OUTBOUND;
                    recordMovement(existing, type, Math.abs(diff), "REACTIVATE", "Reactivated product (quantity set)");
                    existing.setQuantityOnHand(newQty);
                }

                return productRepository.save(existing);
            }
        }

        Product saved = productRepository.save(product);

        if (saved.getQuantityOnHand() != null && saved.getQuantityOnHand() > 0) {
            recordMovement(saved, StockMovementType.INBOUND, saved.getQuantityOnHand(),
                    "INITIAL", "Initial stock");
        }

        return saved;
    }

    public Product update(Long id, Product update) {
        Product existing = findById(id);

        String newSku = update.getSku();
        String currentSku = existing.getSku();

        if (newSku != null) newSku = newSku.trim();
        if (currentSku != null) currentSku = currentSku.trim();

        boolean skuChanged =
                newSku != null && !newSku.isBlank() &&
                        (currentSku == null || !newSku.equalsIgnoreCase(currentSku));

        if (skuChanged) {
            var conflictOpt = productRepository.findFirstBySkuIgnoreCase(newSku);
            if (conflictOpt.isPresent()) {
                Product conflict = conflictOpt.get();
                if (!conflict.getId().equals(existing.getId())) {
                    throw new IllegalArgumentException("Product with SKU '" + newSku + "' already exists.");
                }
            }
        }

        Integer newQty = update.getQuantityOnHand();
        if (newQty == null) newQty = existing.getQuantityOnHand() == null ? 0 : existing.getQuantityOnHand();

        Boolean newActive = update.getActive();
        if (newActive == null) newActive = existing.getActive() == null ? true : existing.getActive();

        existing.setName(update.getName());
        existing.setSku(newSku);
        existing.setCategory(update.getCategory());
        existing.setDescription(update.getDescription());
        existing.setLocation(update.getLocation());
        existing.setActive(newActive);

        int oldQty = existing.getQuantityOnHand() == null ? 0 : existing.getQuantityOnHand();
        if (oldQty != newQty) {
            int diff = newQty - oldQty;
            StockMovementType type = diff >= 0 ? StockMovementType.INBOUND : StockMovementType.OUTBOUND;
            recordMovement(existing, type, Math.abs(diff), "ADJUSTMENT", "Manual quantity change");
            existing.setQuantityOnHand(newQty);
        }

        return productRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    public List<StockMovement> getHistory(Long productId) {
        Product product = findById(productId);
        return stockMovementRepository.findByProductOrderByMovementTimeDesc(product);
    }

    @Transactional
    public void adjustStock(Product product,
                            StockMovementType type,
                            int quantity,
                            String reference,
                            String comment) {

        int current = product.getQuantityOnHand();
        int newQty = type == StockMovementType.INBOUND
                ? current + quantity
                : current - quantity;

        if (newQty < 0) {
            throw new IllegalStateException("Not enough stock for product " + product.getId());
        }
        product.setQuantityOnHand(newQty);
        productRepository.save(product);

        recordMovement(product, type, quantity, reference, comment);
    }

    private void recordMovement(Product product,
                                StockMovementType type,
                                int quantity,
                                String reference,
                                String comment) {
        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(type)
                .quantity(quantity)
                .movementTime(LocalDateTime.now())
                .reference(reference)
                .comment(comment)
                .build();
        stockMovementRepository.save(movement);
    }
}
