package com.bank.rm.product;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {"com.bank.rm.product", "com.bank.rm.common"})
public class ProductCatalogApplication {
    public static void main(String[] args) { SpringApplication.run(ProductCatalogApplication.class, args); }
}
