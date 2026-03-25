namespace InventoryService.Api.Models.Dtos;

public record InventoryItemDto(
    int Id,
    string Sku,
    string Name,
    string? Description,
    int CategoryId,
    string? CategoryName,
    int QuantityOnHand,
    int ReorderLevel,
    decimal UnitPrice,
    string? Location,
    bool IsActive,
    DateTime CreatedAt,
    DateTime UpdatedAt
);

public record CreateInventoryItemDto(
    string Sku,
    string Name,
    string? Description,
    int CategoryId,
    int QuantityOnHand,
    int ReorderLevel,
    decimal UnitPrice,
    string? Location
);

public record UpdateInventoryItemDto(
    string? Name,
    string? Description,
    int? CategoryId,
    int? QuantityOnHand,
    int? ReorderLevel,
    decimal? UnitPrice,
    string? Location,
    bool? IsActive
);

public record InventoryItemPagedResponse(
    IEnumerable<InventoryItemDto> Items,
    int TotalCount,
    int Page,
    int PageSize,
    int TotalPages
);

public record AdjustQuantityDto(
    int Adjustment,
    string? Reason
);
