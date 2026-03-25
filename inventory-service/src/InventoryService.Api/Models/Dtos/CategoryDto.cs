namespace InventoryService.Api.Models.Dtos;

public record CategoryDto(
    int Id,
    string Name,
    string? Description,
    bool IsActive,
    int ItemCount,
    DateTime CreatedAt
);

public record CreateCategoryDto(
    string Name,
    string? Description
);

public record UpdateCategoryDto(
    string? Name,
    string? Description,
    bool? IsActive
);
