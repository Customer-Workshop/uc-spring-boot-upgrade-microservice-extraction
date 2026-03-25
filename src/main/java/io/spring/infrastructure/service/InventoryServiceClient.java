package io.spring.infrastructure.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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

  // Circuit breaker state (thread-safe)
  private volatile boolean circuitOpen = false;
  private final AtomicLong lastFailureTime = new AtomicLong(0);
  private static final long CIRCUIT_RESET_TIMEOUT_MS = 30000; // 30 seconds
  private final AtomicInteger failureCount = new AtomicInteger(0);
  private static final int FAILURE_THRESHOLD = 5;

  public InventoryServiceClient(
      @Value("${inventory.service.url:http://localhost:5062}") String inventoryServiceUrl,
      @Value("${inventory.service.timeout:15}") int timeoutSeconds) {
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
    StringBuilder path = new StringBuilder("/api/inventory?page=");
    path.append(page).append("&pageSize=").append(pageSize);
    if (search != null && !search.isEmpty()) {
      path.append("&search=").append(URLEncoder.encode(search, StandardCharsets.UTF_8));
    }
    return executeGet(path.toString(), "getInventoryItems");
  }

  /**
   * Get a single inventory item by ID.
   *
   * @param id the inventory item ID
   * @return JSON response string or empty if not found or circuit is open
   */
  public Optional<String> getInventoryItem(int id) {
    return executeGet("/api/inventory/" + id, "getInventoryItem");
  }

  /**
   * Get inventory item by SKU.
   *
   * @param sku the SKU code
   * @return JSON response string or empty if not found or circuit is open
   */
  public Optional<String> getInventoryItemBySku(String sku) {
    return executeGet("/api/inventory/sku/" + URLEncoder.encode(sku, StandardCharsets.UTF_8), "getInventoryItemBySku");
  }

  /**
   * Get low stock items from the inventory service.
   *
   * @return JSON response string or empty if circuit is open
   */
  public Optional<String> getLowStockItems() {
    return executeGet("/api/inventory/low-stock", "getLowStockItems");
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

  /**
   * Shared GET request executor with circuit breaker logic. Eliminates duplicate HTTP call patterns
   * across all GET endpoints.
   *
   * @param path the API path (appended to the base inventory service URL)
   * @param operationName name of the calling operation for logging
   * @return JSON response string or empty on failure/circuit open
   */
  private Optional<String> executeGet(String path, String operationName) {
    if (isCircuitOpen()) {
      logger.warn("Circuit breaker is open, returning fallback for {}", operationName);
      return Optional.empty();
    }

    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(inventoryServiceUrl + path))
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
            "Inventory service returned status {} for {}: {}",
            response.statusCode(),
            operationName,
            response.body());
        recordFailure();
        return Optional.empty();
      }
    } catch (Exception e) {
      logger.error("Failed to execute {} on inventory service: {}", operationName, e.getMessage());
      recordFailure();
      return Optional.empty();
    }
  }

  private boolean isCircuitOpen() {
    if (!circuitOpen) {
      return false;
    }
    // Check if enough time has passed to try again (half-open state)
    if (System.currentTimeMillis() - lastFailureTime.get() > CIRCUIT_RESET_TIMEOUT_MS) {
      logger.info("Circuit breaker entering half-open state");
      return false;
    }
    return true;
  }

  private synchronized void recordFailure() {
    lastFailureTime.set(System.currentTimeMillis());
    if (failureCount.incrementAndGet() >= FAILURE_THRESHOLD) {
      circuitOpen = true;
      logger.error(
          "Circuit breaker opened after {} failures. Will retry after {}ms",
          failureCount.get(),
          CIRCUIT_RESET_TIMEOUT_MS);
    }
  }

  private synchronized void resetCircuitBreaker() {
    if (circuitOpen) {
      logger.info("Circuit breaker reset - inventory service is healthy");
    }
    circuitOpen = false;
    failureCount.set(0);
  }
}
