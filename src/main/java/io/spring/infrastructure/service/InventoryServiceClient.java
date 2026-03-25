package io.spring.infrastructure.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * HTTP client for communicating with the Inventory microservice. This replaces the direct database
 * access that was previously part of the monolith. Implements circuit breaker pattern with fallback
 * for resilience.
 */
@Service
public class InventoryServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(InventoryServiceClient.class);

  private final HttpClient httpClient;
  private final String inventoryServiceUrl;
  private final int timeoutSeconds;

  // Circuit breaker state
  private volatile boolean circuitOpen = false;
  private volatile long lastFailureTime = 0;
  private static final long CIRCUIT_RESET_TIMEOUT_MS = 30000; // 30 seconds
  private volatile int failureCount = 0;
  private static final int FAILURE_THRESHOLD = 5;

  public InventoryServiceClient(
      @Value("${inventory.service.url:http://localhost:5062}") String inventoryServiceUrl,
      @Value("${inventory.service.timeout:10}") int timeoutSeconds) {
    this.inventoryServiceUrl = inventoryServiceUrl;
    this.timeoutSeconds = timeoutSeconds;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
  }

  /**
   * Get all inventory items with optional pagination and filtering.
   *
   * @param page the page number (1-based)
   * @param pageSize the number of items per page
   * @param search optional search term
   * @return JSON response string or empty if circuit is open
   */
  public Optional<String> getInventoryItems(int page, int pageSize, String search) {
    if (isCircuitOpen()) {
      logger.warn("Circuit breaker is open, returning fallback for getInventoryItems");
      return Optional.empty();
    }

    try {
      StringBuilder url = new StringBuilder(inventoryServiceUrl);
      url.append("/api/inventory?page=").append(page).append("&pageSize=").append(pageSize);
      if (search != null && !search.isEmpty()) {
        url.append("&search=").append(search);
      }

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(url.toString()))
              .timeout(Duration.ofSeconds(timeoutSeconds))
              .header("Accept", "application/json")
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        resetCircuitBreaker();
        return Optional.of(response.body());
      } else {
        logger.error(
            "Inventory service returned status {}: {}", response.statusCode(), response.body());
        recordFailure();
        return Optional.empty();
      }
    } catch (Exception e) {
      logger.error("Failed to call inventory service: {}", e.getMessage(), e);
      recordFailure();
      return Optional.empty();
    }
  }

  /**
   * Get a single inventory item by ID.
   *
   * @param id the inventory item ID
   * @return JSON response string or empty if not found or circuit is open
   */
  public Optional<String> getInventoryItem(int id) {
    if (isCircuitOpen()) {
      logger.warn("Circuit breaker is open, returning fallback for getInventoryItem");
      return Optional.empty();
    }

    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(inventoryServiceUrl + "/api/inventory/" + id))
              .timeout(Duration.ofSeconds(timeoutSeconds))
              .header("Accept", "application/json")
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        resetCircuitBreaker();
        return Optional.of(response.body());
      } else {
        logger.error(
            "Inventory service returned status {} for item {}",
            response.statusCode(),
            id);
        recordFailure();
        return Optional.empty();
      }
    } catch (Exception e) {
      logger.error("Failed to get inventory item {}: {}", id, e.getMessage(), e);
      recordFailure();
      return Optional.empty();
    }
  }

  /**
   * Get inventory item by SKU.
   *
   * @param sku the SKU code
   * @return JSON response string or empty if not found or circuit is open
   */
  public Optional<String> getInventoryItemBySku(String sku) {
    if (isCircuitOpen()) {
      logger.warn("Circuit breaker is open, returning fallback for getInventoryItemBySku");
      return Optional.empty();
    }

    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(inventoryServiceUrl + "/api/inventory/sku/" + sku))
              .timeout(Duration.ofSeconds(timeoutSeconds))
              .header("Accept", "application/json")
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        resetCircuitBreaker();
        return Optional.of(response.body());
      } else {
        recordFailure();
        return Optional.empty();
      }
    } catch (Exception e) {
      logger.error("Failed to get inventory item by SKU {}: {}", sku, e.getMessage(), e);
      recordFailure();
      return Optional.empty();
    }
  }

  /**
   * Get low stock items from the inventory service.
   *
   * @return JSON response string or empty if circuit is open
   */
  public Optional<String> getLowStockItems() {
    if (isCircuitOpen()) {
      logger.warn("Circuit breaker is open, returning fallback for getLowStockItems");
      return Optional.empty();
    }

    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(inventoryServiceUrl + "/api/inventory/low-stock"))
              .timeout(Duration.ofSeconds(timeoutSeconds))
              .header("Accept", "application/json")
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        resetCircuitBreaker();
        return Optional.of(response.body());
      } else {
        recordFailure();
        return Optional.empty();
      }
    } catch (Exception e) {
      logger.error("Failed to get low stock items: {}", e.getMessage(), e);
      recordFailure();
      return Optional.empty();
    }
  }

  /**
   * Check if the inventory service is healthy.
   *
   * @return true if the service is reachable and healthy
   */
  public boolean isHealthy() {
    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(inventoryServiceUrl + "/health"))
              .timeout(Duration.ofSeconds(5))
              .header("Accept", "application/json")
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      return response.statusCode() == 200;
    } catch (Exception e) {
      logger.debug("Inventory service health check failed: {}", e.getMessage());
      return false;
    }
  }

  private boolean isCircuitOpen() {
    if (!circuitOpen) {
      return false;
    }
    // Check if enough time has passed to try again (half-open state)
    if (System.currentTimeMillis() - lastFailureTime > CIRCUIT_RESET_TIMEOUT_MS) {
      logger.info("Circuit breaker entering half-open state");
      return false;
    }
    return true;
  }

  private void recordFailure() {
    failureCount++;
    lastFailureTime = System.currentTimeMillis();
    if (failureCount >= FAILURE_THRESHOLD) {
      circuitOpen = true;
      logger.error(
          "Circuit breaker opened after {} failures. Will retry after {}ms",
          failureCount,
          CIRCUIT_RESET_TIMEOUT_MS);
    }
  }

  private void resetCircuitBreaker() {
    if (circuitOpen) {
      logger.info("Circuit breaker reset - inventory service is healthy");
    }
    circuitOpen = false;
    failureCount = 0;
  }
}
