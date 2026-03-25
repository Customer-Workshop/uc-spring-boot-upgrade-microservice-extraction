namespace InventoryService.Models.DTOs;

public record InventoryItemDto(
    Guid Id,
    string Sku,
    string Name,
    string? Description,
    string Category,
    int Quantity,
    decimal UnitPrice,
    int ReorderLevel,
    string? WarehouseLocation,
    string? Supplier,
    bool IsActive,
    DateTime CreatedAt,
    DateTime UpdatedAt
);

public record CreateInventoryItemDto(
    string Sku,
    string Name,
    string? Description,
    string Category,
    int Quantity,
    decimal UnitPrice,
    int ReorderLevel,
    string? WarehouseLocation,
    string? Supplier
);

public record UpdateInventoryItemDto(
    string? Name,
    string? Description,
    string? Category,
    decimal? UnitPrice,
    int? ReorderLevel,
    string? WarehouseLocation,
    string? Supplier,
    bool? IsActive
);

public record StockAdjustmentDto(
    int Quantity,
    string MovementType, // IN, OUT, ADJUSTMENT
    string? Reference,
    string? Notes
);

public record StockMovementDto(
    Guid Id,
    Guid InventoryItemId,
    string MovementType,
    int Quantity,
    int PreviousQuantity,
    int NewQuantity,
    string? Reference,
    string? Notes,
    DateTime CreatedAt,
    string? CreatedBy
);

public record PagedResult<T>(
    IEnumerable<T> Items,
    int TotalCount,
    int Page,
    int PageSize,
    int TotalPages
);

public record InventorySearchParams(
    string? SearchTerm = null,
    string? Category = null,
    bool? IsActive = null,
    bool? LowStock = null,
    int Page = 1,
    int PageSize = 20,
    string SortBy = "name",
    string SortDirection = "asc"
);
