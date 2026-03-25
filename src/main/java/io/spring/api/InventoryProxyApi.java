package io.spring.api;

import io.spring.infrastructure.service.InventoryServiceClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proxy API in the monolith that forwards inventory requests to the inventory microservice. This
 * enables a gradual migration where the monolith frontend can still call its own backend, which
 * transparently proxies to the new microservice.
 */
@RestController
@RequestMapping(path = "/inventory")
@AllArgsConstructor
public class InventoryProxyApi {

  private final InventoryServiceClient inventoryServiceClient;

  @GetMapping
  public ResponseEntity<?> getInventoryItems(
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
      @RequestParam(value = "search", required = false) String search) {
    Optional<String> result = inventoryServiceClient.getInventoryItems(page, pageSize, search);
    return result
        .map(body -> ResponseEntity.ok().header("Content-Type", "application/json").body(body))
        .orElse(ResponseEntity.status(503).body(createFallbackResponse("Inventory service is temporarily unavailable")));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getInventoryItem(@PathVariable int id) {
    Optional<String> result = inventoryServiceClient.getInventoryItem(id);
    return result
        .map(body -> ResponseEntity.ok().header("Content-Type", "application/json").body(body))
        .orElse(ResponseEntity.status(503).body(createFallbackResponse("Inventory item not available")));
  }

  @GetMapping("/sku/{sku}")
  public ResponseEntity<?> getInventoryItemBySku(@PathVariable String sku) {
    Optional<String> result = inventoryServiceClient.getInventoryItemBySku(sku);
    return result
        .map(body -> ResponseEntity.ok().header("Content-Type", "application/json").body(body))
        .orElse(ResponseEntity.status(503).body(createFallbackResponse("Inventory item not available")));
  }

  @GetMapping("/low-stock")
  public ResponseEntity<?> getLowStockItems() {
    Optional<String> result = inventoryServiceClient.getLowStockItems();
    return result
        .map(body -> ResponseEntity.ok().header("Content-Type", "application/json").body(body))
        .orElse(ResponseEntity.status(503).body(createFallbackResponse("Low stock data not available")));
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> checkInventoryHealth() {
    boolean healthy = inventoryServiceClient.isHealthy();
    Map<String, Object> response = new HashMap<>();
    response.put("service", "inventory-service");
    response.put("status", healthy ? "UP" : "DOWN");
    response.put("url", "configured");
    return healthy ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
  }

  private String createFallbackResponse(String message) {
    return "{\"error\": \"" + message + "\", \"fallback\": true}";
  }
}
