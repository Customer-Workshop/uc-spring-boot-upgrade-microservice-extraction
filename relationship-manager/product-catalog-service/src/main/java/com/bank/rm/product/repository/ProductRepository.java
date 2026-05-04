package com.bank.rm.product.repository;

import com.bank.rm.common.dto.ProductCategory;
import com.bank.rm.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByActiveTrue();
    List<Product> findByCategoryAndActiveTrue(ProductCategory category);
}
