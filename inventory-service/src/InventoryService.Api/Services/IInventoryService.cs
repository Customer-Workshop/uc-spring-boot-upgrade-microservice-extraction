using InventoryService.Api.Models.Dtos;

namespace InventoryService.Api.Services;

public interface IInventoryService
{
    Task<InventoryItemPagedResponse> GetAllAsync(int page, int pageSize, string? search, int? categoryId, bool? isActive);
    Task<InventoryItemDto?> GetByIdAsync(int id);
    Task<InventoryItemDto?> GetBySkuAsync(string sku);
    Task<InventoryItemDto> CreateAsync(CreateInventoryItemDto dto, string? userId);
    Task<InventoryItemDto?> UpdateAsync(int id, UpdateInventoryItemDto dto, string? userId);
    Task<bool> DeleteAsync(int id);
    Task<InventoryItemDto?> AdjustQuantityAsync(int id, AdjustQuantityDto dto, string? userId);
    Task<IEnumerable<InventoryItemDto>> GetLowStockItemsAsync();
}
