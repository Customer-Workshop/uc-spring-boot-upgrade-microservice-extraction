package com.bank.rm.common.event;

import com.bank.rm.common.dto.ProductCategory;
import java.util.UUID;

public class ProductAddedEvent extends DomainEvent {
    private final UUID productId;
    private final String name;
    private final ProductCategory category;

    public ProductAddedEvent(UUID productId, String name, ProductCategory category) {
        super("product-catalog-service");
        this.productId = productId;
        this.name = name;
        this.category = category;
    }

    public UUID getProductId() { return productId; }
    public String getName() { return name; }
    public ProductCategory getCategory() { return category; }
}
