package io.spring.infrastructure.inventory;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Configuration class for the inventory service HTTP client integration.
 * 
 * This configuration enables the monolith to communicate with the decomposed
 * inventory microservice via HTTP. The inventory-service URL is configurable
 * via the 'inventory.service.url' property in application.properties.
 * 
 * Default URL: http://localhost:5000
 * 
 * Usage in application.properties:
 *   inventory.service.url=http://inventory-service:80
 */
@Configuration
@ComponentScan(basePackages = "io.spring.infrastructure.inventory")
public class InventoryServiceConfig {
}
