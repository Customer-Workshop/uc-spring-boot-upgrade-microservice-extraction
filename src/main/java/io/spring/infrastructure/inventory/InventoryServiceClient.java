package io.spring.infrastructure.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HTTP client for communicating with the Inventory microservice. This replaces the direct database
 * access that was previously embedded in the monolith, routing inventory operations to the
 * decomposed inventory-service via REST API calls.
 */
@Component
public class InventoryServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(InventoryServiceClient.class);

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String inventoryServiceBaseUrl;

  public InventoryServiceClient(
      @Value("${inventory.service.url:http://localhost:5000}") String inventoryServiceBaseUrl) {
    this.inventoryServiceBaseUrl = inventoryServiceBaseUrl;
    this.objectMapper = new ObjectMapper();
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
  }

  /**
   * Retrieves all inventory items with optional filtering and pagination.
   *
   * @param page Page number (1-based)
   * @param pageSize Number of items per page
   * @param category Optional category filter
   * @param searchTerm Optional search term
   * @return List of inventory items
   */
  public List<InventoryItem> getItems(
      int page, int pageSize, String category, String searchTerm) {
    try {
      StringBuilder urlBuilder =
          new StringBuilder(inventoryServiceBaseUrl)
              .append("/api/v1/inventory?page=")
              .append(page)
              .append("&pageSize=")
              .append(pageSize);

      if (category != null && !category.isEmpty()) {
        urlBuilder.append("&category=").append(category);
      }
      if (searchTerm != null && !searchTerm.isEmpty()) {
        urlBuilder.append("&searchTerm=").append(searchTerm);
      }

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(urlBuilder.toString()))
              .timeout(Duration.ofSeconds(30))
              .header("Accept", "application/json")
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode itemsNode = root.get("items");
        if (itemsNode != null && itemsNode.isArray()) {
          return Arrays.asList(objectMapper.treeToValue(itemsNode, InventoryItem[].class));
        }
      }

      logger.warn(
          "Inventory service returned status {}: {}", response.statusCode(), response.body());
      return Collections.emptyList();

    } catch (Exception e) {
      logger.error("Failed to fetch inventory items from inventory-service", e);
      return Collections.emptyList();
    }
  }

  /**
   * Retrieves a single inventory item by its ID.
   *
   * @param id The inventory item ID
   * @return The inventory item, or null if not found
   */
  public InventoryItem getItemById(String id) {
    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(inventoryServiceBaseUrl + "/api/v1/inventory/" + id))
              .timeout(Duration.ofSeconds(30))
              .header("Accept", "application/json")
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), InventoryItem.class);
      }

      logger.warn("Inventory item {} not found (status {})", id, response.statusCode());
      return null;

    } catch (Exception e) {
      logger.error("Failed to fetch inventory item {} from inventory-service", id, e);
      return null;
    }
  }

  /**
   * Retrieves a single inventory item by its SKU.
   *
   * @param sku The inventory item SKU
   * @return The inventory item, or null if not found
   */
  public InventoryItem getItemBySku(String sku) {
    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(inventoryServiceBaseUrl + "/api/v1/inventory/sku/" + sku))
              .timeout(Duration.ofSeconds(30))
              .header("Accept", "application/json")
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), InventoryItem.class);
      }

      logger.warn("Inventory item with SKU {} not found (status {})", sku, response.statusCode());
      return null;

    } catch (Exception e) {
      logger.error("Failed to fetch inventory item by SKU {} from inventory-service", sku, e);
      return null;
    }
  }

  /**
   * Retrieves all items that are below their reorder level.
   *
   * @return List of low-stock inventory items
   */
  public List<InventoryItem> getLowStockItems() {
    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(inventoryServiceBaseUrl + "/api/v1/inventory/low-stock"))
              .timeout(Duration.ofSeconds(30))
              .header("Accept", "application/json")
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return Arrays.asList(
            objectMapper.readValue(response.body(), InventoryItem[].class));
      }

      logger.warn(
          "Inventory service low-stock returned status {}: {}",
          response.statusCode(),
          response.body());
      return Collections.emptyList();

    } catch (Exception e) {
      logger.error("Failed to fetch low-stock items from inventory-service", e);
      return Collections.emptyList();
    }
  }

  /**
   * Adjusts stock for an inventory item.
   *
   * @param id The inventory item ID
   * @param quantity The quantity to adjust
   * @param movementType The type of movement (IN, OUT, ADJUSTMENT)
   * @param reference Optional reference
   * @param notes Optional notes
   * @return The updated inventory item, or null on failure
   */
  public InventoryItem adjustStock(
      String id, int quantity, String movementType, String reference, String notes) {
    try {
      String json =
          objectMapper.writeValueAsString(
              new StockAdjustmentRequest(quantity, movementType, reference, notes));

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(inventoryServiceBaseUrl + "/api/v1/inventory/" + id + "/stock"))
              .timeout(Duration.ofSeconds(30))
              .header("Content-Type", "application/json")
              .header("Accept", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), InventoryItem.class);
      }

      logger.warn(
          "Stock adjustment for item {} failed (status {}): {}",
          id,
          response.statusCode(),
          response.body());
      return null;

    } catch (Exception e) {
      logger.error("Failed to adjust stock for item {} in inventory-service", id, e);
      return null;
    }
  }

  /**
   * Checks if the inventory service is healthy and reachable.
   *
   * @return true if the service is healthy
   */
  public boolean isHealthy() {
    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(inventoryServiceBaseUrl + "/health"))
              .timeout(Duration.ofSeconds(5))
              .GET()
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      return response.statusCode() == 200;

    } catch (Exception e) {
      logger.warn("Inventory service health check failed", e);
      return false;
    }
  }

  /** Internal DTO for stock adjustment requests. */
  private static class StockAdjustmentRequest {
    private final int quantity;
    private final String movementType;
    private final String reference;
    private final String notes;

    StockAdjustmentRequest(int quantity, String movementType, String reference, String notes) {
      this.quantity = quantity;
      this.movementType = movementType;
      this.reference = reference;
      this.notes = notes;
    }

    public int getQuantity() {
      return quantity;
    }

    public String getMovementType() {
      return movementType;
    }

    public String getReference() {
      return reference;
    }

    public String getNotes() {
      return notes;
    }
  }
}
